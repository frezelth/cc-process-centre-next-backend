package eu.europa.ec.cc.processcentre.process.command.repository.model;

import eu.europa.ec.cc.processcentre.model.VariableType;
import java.time.Instant;

public record InsertOrUpdateProcessVariableQueryParam(
    String processInstanceId,
    String name,
    VariableType valueType,
    Integer valueInteger,
    String valueString,
    Long valueLong,
    Double valueDouble,
    Instant valueDate,
    Boolean valueBoolean,
    String valueJson,
    String mimeType,
    byte[] valueByte
) {

}
