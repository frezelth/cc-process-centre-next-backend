package eu.europa.ec.cc.processcentre.repository.model;

import java.time.Instant;

public record CancelProcessQueryParam(
        String processInstanceId,
        String cancelledBy,
        Instant cancelledOn,
        String cancelledOnBehalfOf
) {
}
