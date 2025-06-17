package eu.europa.ec.cc.processcentre.task.repository.model;

import eu.europa.ec.cc.processcentre.task.model.TaskStatus;
import java.time.Instant;

public record ChangeStatusQueryParam(
    String taskInstanceId,
    TaskStatus taskStatus,
    Instant timestamp,
    String actionBy,
    String actionByOnBehalfOf
) {

}
