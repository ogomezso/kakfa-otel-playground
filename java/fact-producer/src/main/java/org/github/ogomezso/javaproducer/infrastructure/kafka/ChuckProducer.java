package org.github.ogomezso.javaproducer.infrastructure.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.github.ogomezso.javaproducer.config.AppConfig;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChuckProducer {

  private final AppConfig appConfig;

  private final KafkaProducer<String, String> plainProducer;

  public ChuckProducer(AppConfig appConfig) {
    this.appConfig = appConfig;
    this.plainProducer = KafkaConfig.createKafkaProducer(appConfig);
  }

  public void produceJsonMessage(String msg) {

    ProducerRecord<String, String> record = new ProducerRecord<>(appConfig.getChuckTopic(), msg);

    plainProducer.send(record, (recordMetadata, exception) -> {
      if (exception == null) {
        log.info("Record written to offset " +
            recordMetadata.offset() + " timestamp " +
            recordMetadata.timestamp());
      } else {
        log.error("An error occurred");
        exception.printStackTrace(System.err);
      }
    });
  }
}
