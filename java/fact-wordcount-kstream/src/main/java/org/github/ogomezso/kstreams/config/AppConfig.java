package org.github.ogomezso.kstreams.config;

import lombok.Data;

@Data
public class AppConfig {
   private int appPort;
   private String bootstrapServers;
   private String inputTopic;
   private String outputTopic;
   private String tempStateDir;
   private String processingGuaranteeConfig;
}
