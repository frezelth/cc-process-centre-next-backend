package eu.europa.ec.cc.processcentre.repository.model;

import eu.europa.ec.cc.processcentre.dto.SearchProcessRequestDto.SpecificFilterValueDto;
import java.util.List;

public record SearchProcessQueryParam(
    String providerId,
    String domainKey,
    String processTypeKey,
    String searchText,
    List<String> responsibleOrganisationCodes,
    List<String> taxonomyPaths,
    String graphElementKey,
    Long situationOnDate,
    Boolean ongoing,
    Boolean completed,
    Boolean paused,
    Boolean cancelled,
    Boolean filterByUserOrganisation,
    List<String> portfolioItemIds,
    List<SpecificFilterValueDto> specificAttributes,

    List<String> dynamicFilters,
    Integer startYear,

    Boolean favourite,

    Boolean sortDescending,

    String sortProperty,

    List<String> tags,
    List<String> businessStatuses,

    String workspaceId,

    String locale,
    String username,

    Integer offset,
    Integer limit

) {

}
