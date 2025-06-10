package eu.europa.ec.cc.processcentre.process.command.repository.model;

import java.time.Instant;

public record CreateProcessQueryParam(
    String processInstanceId,
    Instant startedOn,
    String providerId,
    String processTypeId,
    String responsibleOrganisationId,
    String[] associatedPortfolioItemIds,
    String businessDomainId,
    String domainKey,
    String processTypeKey,
    String responsibleUserId,
    String startedBy,
    String startedByOnBehalfOf,
    String businessStatus
) {

}
