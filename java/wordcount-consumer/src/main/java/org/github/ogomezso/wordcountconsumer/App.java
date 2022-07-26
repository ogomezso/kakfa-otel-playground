package org.github.ogomezso.wordcountconsumer;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;

import org.github.ogomezso.wordcountconsumer.config.AppConfig;
import org.github.ogomezso.wordcountconsumer.config.ConfigHandler;
import org.github.ogomezso.wordcountconsumer.infrastructure.kafka.ConsumerAdapter;
import org.github.ogomezso.wordcountconsumer.infrastructure.kafka.WorkCountService;

public class App {

  public static void main(String[] args) throws FileNotFoundException, URISyntaxException {

    ConfigHandler configHandler = new ConfigHandler();
    AppConfig config = configHandler.getAppConfig(args[0]);

    ConsumerAdapter workCountAdapter = new WorkCountService(config);
    workCountAdapter.pollMessages();
  }
}
