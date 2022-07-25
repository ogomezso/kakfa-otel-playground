FROM confluentinc/cp-server-connect:7.1.2 
USER root
# Install connectors 
RUN confluent-hub install --no-prompt confluentinc/kafka-connect-datagen:0.5.2
RUN confluent-hub install --no-prompt confluentinc/kafka-connect-http:1.5.4  
 
ARG OTEL_VERSION=1.15.0 
ARG PROMETHEUS_AGENT_VERSION=0.16.1

COPY deploy.sh deploy.sh
RUN chmod +x deploy.sh

ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OTEL_VERSION}/opentelemetry-javaagent.jar opentelemetry-javaagent.jar
ADD https://repo1.maven.org/maven2/io/prometheus/jmx/jmx_prometheus_javaagent/${PROMETHEUS_AGENT_VERSION}/jmx_prometheus_javaagent-${PROMETHEUS_AGENT_VERSION}.jar jmx_prometheus_javaagent.jar

COPY kafka_connect.yml /usr/share/jmx-exporter/kafka_connect.yml

ARG APP_NAME="kafka-connect"  
ARG COLLECTOR_ENDPOINT="http://collector:4317"

ENV JAVA_TOOL_OPTIONS "-Dcom.sun.management.jmxremote.ssl=false \
  -Dcom.sun.management.jmxremote.authenticate=false \
  -Dcom.sun.management.jmxremote.port=5000 \
  -Dcom.sun.management.jmxremote.rmi.port=5000 \
  -Dcom.sun.management.jmxremote.host=0.0.0.0 \
  -Djava.rmi.server.hostname=0.0.0.0 \
  -Dspring.profiles.active=docker \
  -javaagent:jmx_prometheus_javaagent.jar=1234:/usr/share/jmx-exporter/kafka_connect.yml \
  -javaagent:opentelemetry-javaagent.jar \
  -Dotel.traces.exporter=otlp \
  -Dotel.metrics.exporter=otlp \
  -Dotel.exporter.otlp.endpoint=${COLLECTOR_ENDPOINT} \
  -Dotel.resource.attributes=service.name=${APP_NAME}"
 