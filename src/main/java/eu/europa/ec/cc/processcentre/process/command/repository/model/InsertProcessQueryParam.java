package eu.europa.ec.cc.processcentre.process.command.repository.model;

public record InsertProcessQueryParam(
    String processInstanceId,
    String providerId,
    String processTypeId,
    String responsibleOrganisationId,
    String businessDomainId,
    String domainKey,
    String processTypeKey,
    String responsibleUserId,
    String businessStatus,
    String parentProcessInstanceId,
    String securityApplicationId,
    String securitySecundaTask,
    String securityScopeTypeId,
    String securityScopeId,
    String securityOrganisationId
) {

}
