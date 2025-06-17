package eu.europa.ec.cc.processcentre.process.command.repository.model;

public record UpdateResolvedConfigQueryParam(
    String processInstanceId,
    String applicationId,
    String secundaTask,
    String scopeTypeId,
    String scopeId,
    String organisationId,
    String resultCardConfig
) {

}
