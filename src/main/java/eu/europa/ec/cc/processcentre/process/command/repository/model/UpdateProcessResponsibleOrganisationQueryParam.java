package eu.europa.ec.cc.processcentre.process.command.repository.model;

public record UpdateProcessResponsibleOrganisationQueryParam(
    String processInstanceId,
    String responsibleOrganisationId
) {

}
