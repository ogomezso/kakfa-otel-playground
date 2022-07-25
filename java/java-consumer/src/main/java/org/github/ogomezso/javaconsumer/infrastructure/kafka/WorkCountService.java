package org.github.ogomezso.javaconsumer.infrastructure.kafka;

import org.github.ogomezso.javaconsumer.config.AppConfig;

public class WorkCountService implements ConsumerAdapter {

  private final WorkCountConsumer consumer;

  public WorkCountService(AppConfig appConfig) {
    this.consumer = new WorkCountConsumer(appConfig);
  }

  @Override
  public void pollMessages() {
    consumer.pollMessage();
  }

}
