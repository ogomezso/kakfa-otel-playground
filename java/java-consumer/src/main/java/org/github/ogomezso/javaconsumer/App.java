package org.github.ogomezso.javaconsumer;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;

import org.github.ogomezso.javaconsumer.config.AppConfig;
import org.github.ogomezso.javaconsumer.config.ConfigHandler;
import org.github.ogomezso.javaconsumer.infrastructure.kafka.ChuckService;
import org.github.ogomezso.javaconsumer.infrastructure.kafka.ConsumerAdapter;

public class App {

  public static void main(String[] args) throws FileNotFoundException, URISyntaxException {

    ConfigHandler configHandler = new ConfigHandler();
    AppConfig config = configHandler.getAppConfig(args[0]);

    ConsumerAdapter chuckAdapter = new ChuckService(config);
    chuckAdapter.pollMessages();
  }
}
