package org.github.ogomezso.statsconsumer.infrastructure.kafka;

import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.CLIENT_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;

import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.github.ogomezso.statsconsumer.config.AppConfig;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class KafkaConfig {

  public static final String DESERIALIZATION_STRING_DESERIALIZER = "org.apache.kafka.common.serialization.StringDeserializer";

  static KafkaConsumer<String, String> createStatsConsumer(AppConfig appConfig) {

    Properties props = new Properties();
    props.put(BOOTSTRAP_SERVERS_CONFIG, appConfig.getBootstrapServers());
    props.put(CLIENT_ID_CONFIG, appConfig.getStatsClientId());
    props.put(GROUP_ID_CONFIG, appConfig.getStatsGroupId());
    props.put(AUTO_OFFSET_RESET_CONFIG, appConfig.getAutoOffsetReset());
    props.put(KEY_DESERIALIZER_CLASS_CONFIG, DESERIALIZATION_STRING_DESERIALIZER);
    props.put(VALUE_DESERIALIZER_CLASS_CONFIG, DESERIALIZATION_STRING_DESERIALIZER);

    final KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
    consumer.subscribe(Collections.singletonList(appConfig.getStatsTopic()));

    return consumer;
  }

}
