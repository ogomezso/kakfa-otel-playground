package org.github.ogomezso.wordcountconsumer.config;

import lombok.Data;

@Data
public class AppConfig {
   private String bootstrapServers;
   private String wordCountClientId;
   private String wordCountTopic;
   private String wordCountGroupId;
}
