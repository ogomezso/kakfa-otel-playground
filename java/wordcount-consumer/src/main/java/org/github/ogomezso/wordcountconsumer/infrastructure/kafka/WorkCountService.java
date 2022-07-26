package org.github.ogomezso.wordcountconsumer.infrastructure.kafka;

import org.github.ogomezso.wordcountconsumer.config.AppConfig;

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
