#!/bin/bash


# Launch Kafka Connect
/etc/confluent/docker/run &
#
# Wait for Kafka Connect listener
echo "Waiting for Kafka Connect to start listening on localhost ⏳"

until $(curl --output /dev/null --silent --head --fail http://localhost:8083/connectors); do
  printf '.'
  sleep 5
done

echo -e "\n--\n+> Creating Data Generator source"
curl -s -X PUT -H  "Content-Type:application/json" http://localhost:8083/connectors/source-datagen-01/config \
    -d '{
    "connector.class": "io.confluent.kafka.connect.datagen.DatagenConnector",
    "key.converter": "org.apache.kafka.connect.storage.StringConverter",
    "kafka.topic": "ratings",
    "max.interval":750,
    "quickstart": "ratings",
    "tasks.max": 1
}'

curl -s -X PUT -H  "Content-Type:application/json" http://localhost:8083/connectors/http-sink-01/config \
 -d '{  
    "topics": "ratings",
    "tasks.max": "1",
    "connector.class": "io.confluent.connect.http.HttpSinkConnector",
    "http.api.url": "http://http-service:8080",
    "request.method": "POST",  
    "confluent.topic.bootstrap.servers": "broker:29092",
    "confluent.topic.replication.factor": "1",
    "reporter.bootstrap.servers": "broker:29092",
    "reporter.result.topic.name": "success-responses",
    "reporter.result.topic.replication.factor": "1",
    "reporter.error.topic.name": "error-responses",
    "reporter.error.topic.replication.factor": "1"
}'
sleep infinity