package org.github.ogomezso.factstats.domain.model;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
@Value
public class ChuckFact {
  String id;
  Long timestamp;
  String fact;
}
