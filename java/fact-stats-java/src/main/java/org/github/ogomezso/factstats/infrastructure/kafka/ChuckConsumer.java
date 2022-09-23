package org.github.ogomezso.factstats.infrastructure.kafka;

import java.time.Duration;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.github.ogomezso.factstats.config.AppConfig;

public class ChuckConsumer implements ConsumerAdapter {

  private final KafkaConsumer<String, String> plainConsumer;

  public ChuckConsumer(AppConfig appConfig) {
    this.plainConsumer = KafkaConfig.createChuckKafkaConsumer(appConfig);
  }

  @Override
  public ConsumerRecords<String, String> pollMessages() {
    final ConsumerRecords<String, String> consumerRecords = plainConsumer.poll(Duration.ofMillis(500));
    consumerRecords.forEach(record -> {
      System.out.printf("Consumer Record:(%d, %s, %d, %d)\n",
          record.key(), record.value(),
          record.partition(), record.offset());
    });
    return consumerRecords;
  }

  @Override
  public void close() throws Exception {
    plainConsumer.close();
  }
}
