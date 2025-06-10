package eu.europa.ec.cc.processcentre.process.command.repository.model;

import java.io.Serializable;
import java.time.Instant;
import lombok.Data;

@Data
public class FindProcessVariableQueryResponse {
  private String processInstanceId;
  private String name;
  private String valueString;
  private Integer valueInteger;
  private Long valueLong;
  private Double valueDouble;
  private Boolean valueBoolean;
  private Instant valueDate;

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
