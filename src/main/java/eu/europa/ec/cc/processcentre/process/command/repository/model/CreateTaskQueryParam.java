package eu.europa.ec.cc.processcentre.process.command.repository.model;

import java.time.Instant;

public record CreateTaskQueryParam(
        String processInstanceId,
        String taskInstanceId,
        String taskTypeKey,
        Instant createdOn
) {

}
