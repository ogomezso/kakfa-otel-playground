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
 - KsqlDB: via Control Center
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
1 Ksql
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

#### Java Producer/Consumer

Plain Java Kafka client application.

`java-producer` receive http request and publish a message to kafka broker with or without AVRO Schema

Apps are suited with [OpenTelemetry Java Agent](https://github.com/open-telemetry/opentelemetry-java-instrumentation) for automatic instrumentation.

You can send a request to java producer endpoint with:

```sh
curl -X POST http://localhost:38080/chuck-says
```
`java-consumer` will consume it immediately so you can start to watch traces on [Jaeger UI](http://localhost:16686/)  under `java-consumer` and `java-producer` services. 
#### Kstream Wordcount

Kstream Application that consumes `chuck-java-topic`extract the fact from the message and count words.

As always the app is being instrumented with the OpenTelemetry Java Agent

### Word Count Consumer

Plain Java Kafka Consumer App that consume from both `chuck-java-topic` and `word-count` topics.

On the  [Jaeger UI](http://localhost:16686/) you will see that `wordcount-consumer` will contain the consume spams and `java-producer` the production ones (see disclaimers)


> DISCLAIMER: Due the fact that neither Connect or KStreams doesn't store the headers you will see that internal spams will be not correlated as we wished, so we will have splitted traces for production and consume. 

### KsqlDB Queries (Example)

```sql
CREATE STREAM CHUCK_FACTS_TRACED (
  id String, timestamp String, 
  fact String, 
  traceparent BYTES HEADER('traceparent')
) WITH (KAFKA_TOPIC='chuck-fact-topic', VALUE_FORMAT='JSON');

SELECT FROM_BYTES(traceparent, 'ascii') as traceId, * FROM CHUCK_DATA_TRACED;

CREATE STREAM CHUCK_STATS_TRACED (
  id String, 
  timestamp String, 
  words int, 
  chars int,
  traceparent BYTES HEADER('traceparent')
) WITH (KAFKA_TOPIC='chuck-stats-topic', VALUE_FORMAT='JSON');

SELECT FROM_BYTES(traceparent, 'ascii') as traceId, * FROM CHUCK_LENGTH_TRACED;


CREATE STREAM CHUCK_TRACING (
	FACT_ID string,
	TIMESTAMP string,
	TRACE_ID string,
	SPAN_ID string,
	APP string
) WITH (KAFKA_TOPIC='chuck-trace-topic', VALUE_FORMAT='JSON');

CREATE STREAM CHUCK_FACTS_TRACING WITH (KAFKA_TOPIC='chuck-trace-topic', VALUE_FORMAT='JSON') AS
SELECT id as FACT_ID,
	timestamp,
	SPLIT(FROM_BYTES(traceparent, 'ascii'), '-')[2] as TRACE_ID,
    SPLIT(FROM_BYTES(traceparent, 'ascii'), '-')[3] as SPAN_ID,
    'FACTS' as APP
    FROM CHUCK_FACTS_TRACED;

CREATE STREAM CHUCK_STATS_TRACING WITH (KAFKA_TOPIC='chuck-trace-topic', VALUE_FORMAT='JSON') AS
SELECT id as FACT_ID,
	timestamp,
	SPLIT(FROM_BYTES(traceparent, 'ascii'), '-')[2] as TRACE_ID,
    SPLIT(FROM_BYTES(traceparent, 'ascii'), '-')[3] as SPAN_ID,
    'STATS' as APP
    FROM CHUCK_STATS_TRACED;



SELECT TRACE_ID, COLLECT_LIST(AS_MAP(ARRAY['ID','APP','SPAN_ID'], ARRAY[FACT_ID, APP, SPAN_ID])) AS TRACE FROM CHUCK_TRACING GROUP BY TRACE_ID EMIT CHANGES;

CREATE TABLE CHUCK_TRACE_TABLE WITH (KAFKA_TOPIC='chuck-trace-table', VALUE_FORMAT='JSON') AS
SELECT TRACE_ID, COLLECT_LIST(AS_MAP(ARRAY['ID','APP','SPAN_ID'], ARRAY[FACT_ID, APP, SPAN_ID])) AS TRACE FROM CHUCK_TRACING GROUP BY TRACE_ID EMIT CHANGES;

```


### Next Steps

As automatic instrumentation does not work with KStreams and connect, we will try with manual instrumentation. 

Add more signals (metrics and logs) to the OTEL Collector.

Try Elastic Stack as backend of all the signals.

Try another backends and implementations (Pixie,...)