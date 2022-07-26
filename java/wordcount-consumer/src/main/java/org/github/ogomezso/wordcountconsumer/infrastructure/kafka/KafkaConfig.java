package org.github.ogomezso.wordcountconsumer.infrastructure.kafka;

import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.CLIENT_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.github.ogomezso.wordcountconsumer.config.AppConfig;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class KafkaConfig {

  public static final String DESERIALIZATION_STRING_DESERIALIZER = "org.apache.kafka.common.serialization.StringDeserializer";
  public static final String DESERIALIZATION_LONG_DESERIALIZER = "org.apache.kafka.common.serialization.LongDeserializer";

  static KafkaConsumer<String, Long> createWordCountConsumer(AppConfig appConfig) {

    Properties props = new Properties();
    props.put(BOOTSTRAP_SERVERS_CONFIG, appConfig.getBootstrapServers());
    props.put(CLIENT_ID_CONFIG, appConfig.getWordCountClientId());
    props.put(GROUP_ID_CONFIG, appConfig.getWordCountGroupId());
    props.put(KEY_DESERIALIZER_CLASS_CONFIG, DESERIALIZATION_STRING_DESERIALIZER);
    props.put(VALUE_DESERIALIZER_CLASS_CONFIG, DESERIALIZATION_LONG_DESERIALIZER);

    final KafkaConsumer<String, Long> consumer = new KafkaConsumer<>(props);
    consumer.subscribe(Collections.singletonList(appConfig.getWordCountTopic()));

    return consumer;
  }

}
