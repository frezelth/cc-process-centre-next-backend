package eu.europa.ec.cc.processcentre.task.repository.model;

public record UpdateTaskQueryParam(
    String taskInstanceId,
    String providerId,
    String domainKey,
    String processTypeKey,
    String taskTypeKey,
    String taskTypeId
) {

}
