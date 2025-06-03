package eu.europa.ec.cc.processcentre.repository.model;

import java.time.Instant;

public record DeleteProcessQueryParam(
        String processInstanceId,
        Instant deletedOn,
        String deletedBy,
        String deletedOnBehalfOf
) {
}
