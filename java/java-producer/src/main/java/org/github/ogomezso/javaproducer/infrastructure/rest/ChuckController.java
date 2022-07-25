package org.github.ogomezso.javaproducer.infrastructure.rest;

import org.github.ogomezso.javaproducer.config.AppConfig;
import org.github.ogomezso.javaproducer.infrastructure.kafka.ChuckAdapter;
import org.github.ogomezso.javaproducer.infrastructure.kafka.ChuckService;
import org.github.ogomezso.javaproducer.infrastructure.rest.model.ChuckFactResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ChuckController {

  private final AppConfig appConfig;
  private final FactResponseMapper mapper = new FactResponseMapper();
  private final ChuckAdapter adapter;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public ChuckController(AppConfig appConfig) {
    this.appConfig = appConfig;
    this.adapter = new ChuckService(appConfig);
  }

  public String sendFact() throws JsonProcessingException {
    return objectMapper.writeValueAsString(mapper.toResponse(adapter.sendFact()));

  }

  public ChuckFactResponse sendAvroFact() {
    return mapper.toResponse(adapter.SendAvroFact());
  }
}