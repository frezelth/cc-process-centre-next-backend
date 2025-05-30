package eu.europa.ec.cc.processcentre.repository.model;

import java.time.Instant;

public record InsertOrUpdateProcessVariableQueryParam(
    String processInstanceId,
    String name,
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
