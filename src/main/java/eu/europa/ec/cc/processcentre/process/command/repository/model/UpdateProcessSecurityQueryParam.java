package eu.europa.ec.cc.processcentre.process.command.repository.model;

public record UpdateProcessSecurityQueryParam(
    String processInstanceId,
    String applicationId,
    String secundaTask,
    String scopeTypeId,
    String scopeId,
    String scopeIdVal
) {

}
