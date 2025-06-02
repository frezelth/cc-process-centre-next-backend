package eu.europa.ec.cc.processcentre.repository.model;

import java.io.Serializable;
import java.time.Instant;

public record FindProcessVariableQueryResponse(
    String processInstanceId,
    String name,
    String valueString,
    Integer valueInteger,
    Long valueLong,
    Double valueDouble,
    Boolean valueBoolean,
    Instant valueDate
) {

  public Serializable getVariableValue(){
    if (valueString != null) {return valueString;}
    if (valueInteger != null) {return valueInteger;}
    if (valueLong != null) {return valueLong;}
    if (valueDouble != null) {return valueDouble;}
    if (valueBoolean != null) {return valueBoolean;}
    if (valueDate != null) {return valueDate;}
    throw new IllegalArgumentException("Unsupported variable query response");
  }

}
