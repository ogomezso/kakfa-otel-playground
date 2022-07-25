package org.github.ogomezso.javaproducer;

import static spark.Spark.port;
import static spark.Spark.post;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;

import org.github.ogomezso.javaproducer.config.AppConfig;
import org.github.ogomezso.javaproducer.config.ConfigHandler;
import org.github.ogomezso.javaproducer.infrastructure.rest.ChuckController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App {

  public static void main(String[] args) throws FileNotFoundException, URISyntaxException {

    ConfigHandler configHandler = new ConfigHandler();
    AppConfig config = configHandler.getAppConfig(args[0]);
    ChuckController controller = new ChuckController(config);
    port(config.getAppPort());
    post("/chuck-says", (req, res) -> {
      log.info("Plain Json request received");
      res.header("Content-Type", "application/json");
      return controller.sendFact();
    });
    post("/chuck-says/avro", (req, res) -> {
      log.info("Avro request received");
      return controller.sendAvroFact();
    });

  }
}
