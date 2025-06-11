package eu.europa.ec.cc.processcentre.task.repository.model;

import java.time.Instant;

public record ChangeTaskStatusQueryParam(
    String taskInstanceId,
    Instant timestamp,
    String userId,
    String onBehalfOfUserId
) {

}
