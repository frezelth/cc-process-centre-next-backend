package eu.europa.ec.cc.processcentre.process.command.repository.model;

public record UpdateProcessResponsibleUserQueryParam(
    String processInstanceId,
    String responsibleUserId
) {

}
