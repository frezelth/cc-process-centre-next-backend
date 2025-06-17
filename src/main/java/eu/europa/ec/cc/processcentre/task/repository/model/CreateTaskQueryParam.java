package eu.europa.ec.cc.processcentre.task.repository.model;

import eu.europa.ec.cc.processcentre.task.model.TaskStatus;
import java.time.Instant;

public record CreateTaskQueryParam(
        String processInstanceId,
        String taskInstanceId,
        String taskTypeId,
        String providerId,
        String domainKey,
        String processTypeKey,
        String taskTypeKey,
        Instant createdOn,
        TaskStatus status,
        Instant assignedOn,
        String assignedTo,
        String assignedToOnBehalfOf
) {

}
