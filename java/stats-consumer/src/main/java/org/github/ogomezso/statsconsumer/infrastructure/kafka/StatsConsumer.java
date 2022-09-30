package org.github.ogomezso.statsconsumer.infrastructure.kafka;

import java.time.Duration;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.github.ogomezso.statsconsumer.config.AppConfig;

public class StatsConsumer implements ConsumerAdapter {

  private final KafkaConsumer<String, String> statsConsumer;

  public StatsConsumer(AppConfig appConfig) {
    this.statsConsumer = KafkaConfig.createStatsConsumer(appConfig);
  }

  @Override
  public void pollMessages() {
    while (true) {
      final ConsumerRecords<String, String> consumerRecords = statsConsumer.poll(Duration.ofMillis(500));

      consumerRecords.forEach(record -> {
        System.out.printf("Consumer Record:(%s, %s, %d, %d)\n",
            record.key(), record.value(), record.partition(), record.offset());
      });
    }
    
  }
}
