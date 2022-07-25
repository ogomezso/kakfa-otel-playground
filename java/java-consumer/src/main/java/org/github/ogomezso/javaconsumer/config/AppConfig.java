package org.github.ogomezso.javaconsumer.config;

import lombok.Data;

@Data
public class AppConfig {
   private int appPort;
   private String bootstrapServers;
   private String chuckClientId;
   private String chuckGroupId;
   private String chuckTopic;
   private String wordCountClientId;
   private String wordCountTopic;
   private String wordCountGroupId;
   private String schemaRegistryUrl;
}
