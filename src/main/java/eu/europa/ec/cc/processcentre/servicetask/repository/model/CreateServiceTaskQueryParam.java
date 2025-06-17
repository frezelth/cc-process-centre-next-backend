package eu.europa.ec.cc.processcentre.servicetask.repository.model;

public record CreateServiceTaskQueryParam(
    String taskInstanceId,
    String processInstanceId,
    String providerId,
    String domainKey,
    String processTypeKey,
    String taskTypeKey
) {

}
