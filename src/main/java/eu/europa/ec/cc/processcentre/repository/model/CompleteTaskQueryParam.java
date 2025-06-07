package eu.europa.ec.cc.processcentre.repository.model;

import java.time.Instant;

public record CompleteTaskQueryParam(
    String taskInstanceId,
    Instant completedOn
) {

}
