package eu.europa.ec.cc.processcentre.process.query.repository.model;

import eu.europa.ec.cc.processcentre.process.query.web.dto.SearchProcessRequestDto.SpecificFilterValueDto;
import eu.europa.ec.cc.processcentre.model.ProcessStatus;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public record SearchProcessQueryParam(

    String providerId,
    String domainKey,
    String processTypeKey,
    String searchText,
    List<String> responsibleOrganisationCodes,

    String taxonomyPath,

    String graphElementKey,

    Set<ProcessStatus> statuses,

    Boolean filterByUserOrganisation,
    Set<String> portfolioItemIds,
    List<SpecificFilterValueDto> specificAttributes,

    List<String> dynamicFilters,
    Integer startYear,

    Boolean favourite,

    Boolean sortDescending,

    String sortProperty,

    List<String> tags,
    List<String> businessStatuses,

    String locale,
    String username,

    Integer offset,
    Integer limit,

    Set<SecurityFilter> securityFilters

) {

  public SearchProcessQueryParam(){
    this(null, null, null, null, Collections.emptyList(), null, null, Collections.emptySet(),
        null, Collections.emptySet(), Collections.emptyList(), Collections.emptyList(), null, null, null,
        null, Collections.emptyList(), Collections.emptyList(), null, null, 0, 1, Collections.emptySet());
  }

  public record SecurityFilter (
      String secundaTask,
      Set<SecurityFilterScope> secundaScopes
  ){}

  public record SecurityFilterScope(
      String scopeTypeId,
      Set<String> scopeIds
  ){}

}
