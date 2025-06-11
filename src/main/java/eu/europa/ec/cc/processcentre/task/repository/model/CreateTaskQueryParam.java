package eu.europa.ec.cc.processcentre.task.repository.model;

import java.time.Instant;

public record CreateTaskQueryParam(
        String processInstanceId,
        String taskInstanceId,
        String taskTypeKey,
        Instant createdOn
) {

}
