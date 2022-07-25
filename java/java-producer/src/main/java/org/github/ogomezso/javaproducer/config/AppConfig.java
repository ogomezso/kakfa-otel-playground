package org.github.ogomezso.javaproducer.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;

@Data
public class AppConfig {
   private int appPort;
   private String bootstrapServers;
   private String chuckClientId;
   private String chuckTopic;
   private String chuckAvroClientId;
   private String chuckAvroTopic;
   private String schemaRegistryUrl;
   private final ObjectMapper objectMapper = new ObjectMapper();
}
