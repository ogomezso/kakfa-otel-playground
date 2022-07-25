# Kafka OpenTelemetry Playground

# Introduction

> The main goal of this repo is to provide real code examples for client end to end traceability for Kafka Clients.

## Environment:

### Run local env:

All that you need is under `environment` folder. Just run:

~~~shell
docker compose up --build
~~~

After run the env you will have available:

 - Broker External listener on : `localhost:9092` or `broker:29092`inside docker network
 - Connect API: http://localhost:8083
 - Schema Registry API: http://localhost:8081
 - Control Center: http://localhost:9021
 - Jaeger UI: http://localhost:16686/

### Environment Stack

#### Confluent Platform

Docker compose provides:

~~~
1 Zookeeper Node
1 Kafka Broker
1 Schema Registry
1 Kafka Connect suite with `Datagen` and `http-sink` connectors
1 Control Center  
~~~

#### Open Telemetry Collector

> We will use Collector as backbone for all Open Telemetry Signal and then route for the specific telemetry backend

[Official Docs](https://opentelemetry.io/docs/collector/)

#### Jaeger

[Jaeger](https://www.jaegertracing.io/) is backend-server for end to end distributed tracing.

## Use Cases

### Connect End to End


The Custom connect image that we build for this example includes the `Open Telemetry Java agent` for automatic instrumentation as you can see on `Dockerfile` under `connect` folder. 

Connect entrypoint script will run automatically a `Datagen Source Connector` that writes `ratings` messages on ratings topic.

Connect Container also runs a `http sink connector` that call to GOLANG  `http-service`.

On top of that `go-consumer` application is consuming also that topic as a client.

In the very moment the environment is running you will have traces available on [Jaeger UI](http://localhost:16686/)

>Go consumer and http-server instrumented manually with [Open Telemetry GO SDK](https://github.com/open-telemetry/opentelemetry-go) 

> WARNING: Is possible that go-consumer starts before broker is properly running with the consequent error on the app, if that is the case just restart the go-consumer by running `docker compose restart go-consumer`

#### Java Producer

Plain Java Kafka client application.

`java-producer` receive http request and publish a message to kafka broker with or without AVRO Schema

Apps are suited with [OpenTelemetry Java Agent](https://github.com/open-telemetry/opentelemetry-java-instrumentation) for automatic instrumentation.

You can send a request to java producer endpoint with:

```sh
curl -X POST http://localhost:38080/chuck-says
```

### Java Consumer

In this moment `java-consumer`will start to consume it and now you can go to [Jaeger UI](http://localhost:16686/) to watch your traces.
