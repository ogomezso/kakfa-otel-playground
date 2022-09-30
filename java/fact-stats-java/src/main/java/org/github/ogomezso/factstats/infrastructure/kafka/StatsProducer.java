package org.github.ogomezso.factstats.infrastructure.kafka;

import java.util.Optional;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.github.ogomezso.factstats.config.AppConfig;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StatsProducer implements AutoCloseable {

    private final AppConfig appConfig;

    private final KafkaProducer<String, String> plainProducer;

    public StatsProducer(AppConfig appConfig) {
        this.appConfig = appConfig;
        this.plainProducer = KafkaConfig.createLineLengthProducer(appConfig);
    }

    public void produce(Optional<String> message) {
        message.ifPresent(msg -> {
            ProducerRecord<String, String> outputRecord = new ProducerRecord<>(appConfig.getStatsTopic(), msg);
            plainProducer.send(outputRecord, (recordMetadata, exception) -> {
                if (exception == null) {
                    log.info("Record written to offset " +
                            recordMetadata.offset() + " timestamp " +
                            recordMetadata.timestamp());
                } else {
                    log.error("An error occurred");
                    exception.printStackTrace(System.err);
                }
            });
        });
    }

    @Override
    public void close() {
        plainProducer.close();
    }

}