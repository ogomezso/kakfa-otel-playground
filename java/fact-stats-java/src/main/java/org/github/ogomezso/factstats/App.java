package org.github.ogomezso.factstats;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.StreamSupport;
import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.github.ogomezso.factstats.config.AppConfig;
import org.github.ogomezso.factstats.config.ConfigHandler;
import org.github.ogomezso.factstats.domain.model.ChuckFact;
import org.github.ogomezso.factstats.domain.model.ChuckFactLength;
import org.github.ogomezso.factstats.infrastructure.kafka.ChuckConsumer;
import org.github.ogomezso.factstats.infrastructure.kafka.ConsumerAdapter;
import org.github.ogomezso.factstats.infrastructure.kafka.StatsProducer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class App {

  private final static ObjectMapper MAPPER = new ObjectMapper();

  private static Function<String, ChuckFact> factFromString = new Function<String, ChuckFact>() {
    @Override
    public ChuckFact apply(String value) {
      ChuckFact fact = null;
      try {
        fact = MAPPER.readValue(value, ChuckFact.class);
      } catch (Exception e) {
        e.printStackTrace();
      }
      return fact;
    }
  };

  private static Function<ChuckFact, ChuckFactLength> chuckFactLength = new Function<ChuckFact, ChuckFactLength>() {
    @Override
    public ChuckFactLength apply(ChuckFact fact) {
      return ChuckFactLength.builder()
        .id(UUID.randomUUID().toString())
        .timestamp(Timestamp.from(Instant.now()).getTime())
        .words(fact.getFact().split(" ").length)
        .chars(fact.getFact().length())
        .build();
    }
  };

  private static Function<ChuckFactLength, Optional<String>> factLengthToString = new Function<>() {
    @Override
    public Optional<String> apply(ChuckFactLength factLength) {
      try {
        return Optional.of(MAPPER.writeValueAsString(factLength));
      } catch (JsonProcessingException e) {
        e.printStackTrace();
        return Optional.empty();
      }
    }
  };

  public static void main(String[] args) throws Exception {

    ConfigHandler configHandler = new ConfigHandler();
    AppConfig config = configHandler.getAppConfig(args[0]);

    try (ConsumerAdapter consumerAdapter = new ChuckConsumer(config);
        StatsProducer producerAdapter = new StatsProducer(config)) {

      while (true) {
        ConsumerRecords<String, String> records = consumerAdapter.pollMessages();
        StreamSupport.stream(records.spliterator(), false)
          .map(ConsumerRecord<String, String>::value)
          .map(factFromString)
          .map(chuckFactLength)
          .map(factLengthToString)
          .forEach(producerAdapter::produce);
      }
    }
  }
}
