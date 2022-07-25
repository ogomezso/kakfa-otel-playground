package main

import (
	"bytes"
	"context"
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"time"

	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"

	"go.opentelemetry.io/contrib/instrumentation/net/http/otelhttp"
	"go.opentelemetry.io/otel"
	"go.opentelemetry.io/otel/exporters/otlp/otlptrace/otlptracegrpc"
	"go.opentelemetry.io/otel/propagation"
	"go.opentelemetry.io/otel/sdk/resource"
	sdktrace "go.opentelemetry.io/otel/sdk/trace"
	semconv "go.opentelemetry.io/otel/semconv/v1.10.0"
)

func handlerLoggger(w http.ResponseWriter, r *http.Request) {
	html := "done"

	start := time.Now()

	// log request by who(IP address)
	requesterIP := r.RemoteAddr
	log.Println("---------------------REQUEST-----------------------------")
	log.Printf(
		"METHOD:%s\t\t%s\t\t%s\t\t%v",
		r.Method,
		r.RequestURI,
		requesterIP,
		time.Since(start),
	)

	log.Printf("HEADERS")
	for name, headers := range r.Header {
		for _, h := range headers {
			log.Printf("%v: %v", name, h)
		}
	}

	buf, bodyErr := ioutil.ReadAll(r.Body) // read request body
	if bodyErr != nil {
		log.Print("bodyErr ", bodyErr.Error())
		http.Error(w, bodyErr.Error(), http.StatusInternalServerError)
		return
	}

	rdr1 := ioutil.NopCloser(bytes.NewBuffer(buf))
	rdr2 := ioutil.NopCloser(bytes.NewBuffer(buf))
	log.Printf("BODY: %q", rdr1)
	r.Body = rdr2
	log.Println("-------------------------------------------------------")

	w.Write([]byte(html))

}

// Initializes an OTLP exporter, and configures the corresponding trace and
// metric providers.
func initProvider() func() {
	ctx := context.Background()

	res, err := resource.New(ctx,
		resource.WithAttributes(
			// the service name used to display traces in backends
			semconv.ServiceNameKey.String("http-service-sink"),
		),
	)
	handleErr(err, "failed to create resource")

	// If the OpenTelemetry Collector is running on a local cluster (minikube or
	// microk8s), it should be accessible through the NodePort service at the
	// `localhost:30080` endpoint. Otherwise, replace `localhost` with the
	// endpoint of your cluster. If you run the app inside k8s, then you can
	// probably connect directly to the service through dns
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
func handleErr(err error, message string) {
	if err != nil {
		log.Fatalf("%s: %v", message, err)
	}
}

type TraceparentHandler struct {
	next  http.Handler
	props propagation.TextMapPropagator
}

func NewTraceparentHandler(next http.Handler) *TraceparentHandler {
	return &TraceparentHandler{
		next:  next,
		props: otel.GetTextMapPropagator(),
	}
}

func (h *TraceparentHandler) ServeHTTP(w http.ResponseWriter, req *http.Request) {
	h.props.Inject(req.Context(), propagation.HeaderCarrier(w.Header()))
	h.next.ServeHTTP(w, req)
}

func main() {
	shutdown := initProvider()
	defer shutdown()

	var handler http.Handler

	handler = http.HandlerFunc(handlerLoggger)
	handler = otelhttp.WithRouteTag("/", handler)
	handler = otelhttp.NewHandler(handler, "http-sink-service")
	handler = NewTraceparentHandler(handler)

	fmt.Println("listening on :8080")
	// Finally, serve requests.
	http.ListenAndServe(":8080", handler)

}
