package org.github.ogomezso.statsconsumer;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;

import org.github.ogomezso.statsconsumer.config.AppConfig;
import org.github.ogomezso.statsconsumer.config.ConfigHandler;
import org.github.ogomezso.statsconsumer.infrastructure.kafka.ConsumerAdapter;
import org.github.ogomezso.statsconsumer.infrastructure.kafka.StatsConsumer;

public class App {

  public static void main(String[] args) throws FileNotFoundException, URISyntaxException {

    ConfigHandler configHandler = new ConfigHandler();
    AppConfig config = configHandler.getAppConfig(args[0]);

    ConsumerAdapter workCountAdapter = new StatsConsumer(config);
    workCountAdapter.pollMessages();
  }
}
