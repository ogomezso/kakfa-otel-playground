package main

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"log"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/confluentinc/confluent-kafka-go/kafka"

	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"

	"go.opentelemetry.io/otel"
	"go.opentelemetry.io/otel/exporters/otlp/otlptrace/otlptracegrpc"
	"go.opentelemetry.io/otel/propagation"
	"go.opentelemetry.io/otel/sdk/resource"
	sdktrace "go.opentelemetry.io/otel/sdk/trace"
	semconv "go.opentelemetry.io/otel/semconv/v1.10.0"
	"go.opentelemetry.io/otel/trace"
)

func handleErr(err error, message string) {
	if err != nil {
		log.Fatalf("%s: %v", message, err)
	}
}

// Initializes an OTLP exporter, and configures the corresponding trace and
// metric providers.
func initProvider() func() {
	ctx := context.Background()

	res, err := resource.New(ctx,
		resource.WithAttributes(
			// the service name used to display traces in backends
			semconv.ServiceNameKey.String("go-consumer"),
		),
	)
	handleErr(err, "failed to create resource")

	conn, err := grpc.DialContext(ctx, "collector:4317", grpc.WithTransportCredentials(insecure.NewCredentials()), grpc.WithBlock())
	handleErr(err, "failed to create gRPC connection to collector")

	// Set up a trace exporter
	traceExporter, err := otlptracegrpc.New(ctx, otlptracegrpc.WithGRPCConn(conn))
	handleErr(err, "failed to create trace exporter")

	// Register the trace exporter with a TracerProvider, using a batch
	// span processor to aggregate spans before export.
	bsp := sdktrace.NewBatchSpanProcessor(traceExporter)
	tracerProvider := sdktrace.NewTracerProvider(
		sdktrace.WithSampler(sdktrace.AlwaysSample()),
		sdktrace.WithResource(res),
		sdktrace.WithSpanProcessor(bsp),
	)

	otel.SetTracerProvider(tracerProvider)

	// set global propagator to tracecontext (the default is no-op).
	otel.SetTextMapPropagator(propagation.TraceContext{})

	return func() {
		// Shutdown will flush any remaining spans and shut down the exporter.
		handleErr(tracerProvider.Shutdown(ctx), "failed to shutdown TracerProvider")
	}
}

func main() {

	shutdown := initProvider()
	defer shutdown()

	sigchan := make(chan os.Signal, 1)
	signal.Notify(sigchan, syscall.SIGINT, syscall.SIGTERM)

	log.Printf("Waiting Kafka to be ready...\n")
	time.Sleep(3 * time.Second) // wait for the broker to be ready
	log.Printf("trying to connect.\n")
	c, err := kafka.NewConsumer(&kafka.ConfigMap{
		"bootstrap.servers":     "broker:29092",
		"group.id":              "go-consumer",
		"auto.offset.reset":     "latest",
		"broker.address.family": "v4",
		"session.timeout.ms":    6000,
	})

	if err != nil {
		panic(err)
	}

	c.SubscribeTopics([]string{"ratings", "^aRegex.*[Tt]opic"}, nil)

	fmt.Printf("Created Consumer %v\n", c)

	run := true
	for run {
		select {
		case sig := <-sigchan:
			fmt.Printf("Caught signal %v: terminating\n", sig)
			run = false
		default:
			ev := c.Poll(100)
			if ev == nil {
				continue
			}

			switch e := ev.(type) {
			case *kafka.Message:
				fmt.Printf("## Message on %s:\n", e.TopicPartition)
				fmt.Printf("#### Key %s:\n", string(e.Key))
				fmt.Printf("#### Value %s:\n", string(e.Value[:]))
				str1 := bytes.NewBuffer(e.Value).String()
				fmt.Println("String =", str1)
				if e.Headers != nil {
					fmt.Printf("### Headers: %v\n", e.Headers)
				}
				log.Printf("JSON parsing...")
				var msg interface{}
				json.Unmarshal(e.Value, &msg)
				// log the message
				log.Printf("JSON --> %v", msg)

				// Extract tracing info from message
				ctx := otel.GetTextMapPropagator().Extract(context.Background(), NewConsumerMessageCarrier(e))
				tr := otel.Tracer("consumer")
				_, span := tr.Start(ctx, "consume message", trace.WithAttributes(
					semconv.MessagingOperationProcess,
				))

				_, err := c.StoreMessage(e)
				if err != nil {
					fmt.Fprintf(os.Stderr, "%% Error storing offset after message %s:\n", e.TopicPartition)
				}
				span.End()
			case kafka.Error:
				fmt.Fprintf(os.Stderr, "%% Error: %v: %v\n", e.Code(), e)
				if e.Code() == kafka.ErrAllBrokersDown {
					run = false
				}
			default:
				fmt.Printf("Ignored %v\n", e)
			}
		}
	}

	fmt.Printf("Closing consumer\n")
	c.Close()

}
