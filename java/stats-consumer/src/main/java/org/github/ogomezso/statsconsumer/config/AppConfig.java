package org.github.ogomezso.statsconsumer.config;

import lombok.Data;

@Data
public class AppConfig {
   private String bootstrapServers;
   private String statsClientId;
   private String statsGroupId;
   private String statsTopic;
   private String autoOffsetReset;
}
