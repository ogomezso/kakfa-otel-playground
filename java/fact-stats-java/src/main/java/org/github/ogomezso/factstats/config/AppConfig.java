package org.github.ogomezso.factstats.config;

import lombok.Data;

@Data
public class AppConfig {
   private String bootstrapServers;
   private String chuckClientId;
   private String chuckGroupId;
   private String chuckTopic;
   private String statsTopic;
   private String autoOffsetReset;
}
