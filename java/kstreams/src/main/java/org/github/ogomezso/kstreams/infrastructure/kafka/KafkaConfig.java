package org.github.ogomezso.kstreams.infrastructure.kafka;

import java.util.Properties;

import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.github.ogomezso.kstreams.config.AppConfig;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class KafkaConfig {

    public static Properties createStreamsConfigProperties(String applicationId, AppConfig appConfig) {

        Properties streamsConfiguration = new Properties();
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, appConfig.getBootstrapServers());
        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        streamsConfiguration.put(
            StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,
            Serdes.String().getClass().getName());
        streamsConfiguration.put(
            StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
            Serdes.String().getClass().getName());
        streamsConfiguration.put(
            StreamsConfig.STATE_DIR_CONFIG, appConfig.getProcessingGuaranteeConfig());
        streamsConfiguration.put(
            StreamsConfig.PROCESSING_GUARANTEE_CONFIG, appConfig.getProcessingGuaranteeConfig());
    
        return streamsConfiguration;
      }
}
