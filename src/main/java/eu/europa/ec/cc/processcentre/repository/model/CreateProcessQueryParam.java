package eu.europa.ec.cc.processcentre.repository.model;

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
    String startedOnBehalfOf,
    String businessStatus
) {

}
