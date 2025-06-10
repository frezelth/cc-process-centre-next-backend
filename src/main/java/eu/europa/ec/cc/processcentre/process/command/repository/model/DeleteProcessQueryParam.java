package eu.europa.ec.cc.processcentre.process.command.repository.model;

import java.time.Instant;

public record DeleteProcessQueryParam(
    String processInstanceId,
    Instant deletedOn,
    String deletedBy,
    String deletedByOnBehalfOf
) {

}
