package eu.europa.ec.cc.processcentre.process.command.repository.model;

import java.time.Instant;

public record CompleteTaskQueryParam(
    String taskInstanceId,
    Instant completedOn
) {

}
