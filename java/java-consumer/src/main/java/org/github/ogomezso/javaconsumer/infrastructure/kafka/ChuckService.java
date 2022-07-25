package org.github.ogomezso.javaconsumer.infrastructure.kafka;

import org.github.ogomezso.javaconsumer.config.AppConfig;

public class ChuckService implements ChuckAdapter {

  private final ChuckConsumer consumer;

  public ChuckService(AppConfig appConfig) {
    this.consumer = new ChuckConsumer(appConfig);
  }

  @Override
  public void pollMessages() {
    consumer.pollMessage();
  }

}
