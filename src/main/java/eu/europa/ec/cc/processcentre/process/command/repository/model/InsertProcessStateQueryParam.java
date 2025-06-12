package eu.europa.ec.cc.processcentre.process.command.repository.model;

import java.time.Instant;

public record InsertProcessStateQueryParam(
    String processInstanceId,
    String state,
    Instant changedOn
) {

}
