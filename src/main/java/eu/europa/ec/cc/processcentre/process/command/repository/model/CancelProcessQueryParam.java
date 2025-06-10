package eu.europa.ec.cc.processcentre.process.command.repository.model;

import java.time.Instant;

public record CancelProcessQueryParam(
    String processInstanceId,
    String cancelledBy,
    Instant cancelledOn,
    String cancelledByOnBehalfOf
) {
}
