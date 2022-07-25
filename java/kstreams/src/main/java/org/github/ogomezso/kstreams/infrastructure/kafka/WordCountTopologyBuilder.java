package org.github.ogomezso.kstreams.infrastructure.kafka;

import java.util.List;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Produced;
import org.github.ogomezso.kstreams.config.AppConfig;
import org.github.ogomezso.kstreams.domain.model.ChuckFact;

import com.fasterxml.jackson.databind.ObjectMapper;

public class WordCountTopologyBuilder {

    public static StreamsBuilder createWordCountTopology(AppConfig appConfig) {
        final StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, String> textLines = builder.stream(appConfig.getInputTopic());

        KStream<String, Long> wordCount = textLines
        .flatMapValues(value -> List.of(extractFactFromValue(value).toLowerCase().split("\\W+")))
        .groupBy((key, value) -> value)
        .count(Materialized.as("WordCount"))
        .toStream();
        wordCount.print(Printed.toSysOut());
        wordCount.to(appConfig.getOutputTopic(), Produced.with(Serdes.String(), Serdes.Long()));

        return builder;
    }

    private static String extractFactFromValue(String value) {
        ObjectMapper mapper = new ObjectMapper();
        ChuckFact fact = null;
        try {
            fact = mapper.readValue(value, ChuckFact.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fact.getFact().toLowerCase();

    }

}
