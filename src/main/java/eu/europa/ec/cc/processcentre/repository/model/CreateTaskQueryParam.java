package eu.europa.ec.cc.processcentre.repository.model;

import java.time.Instant;

public record CreateTaskQueryParam(
        String processInstanceId,
        String taskInstanceId,
        String taskTypeKey,
        Instant createdOn
) {

}
