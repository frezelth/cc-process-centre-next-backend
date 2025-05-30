package eu.europa.ec.cc.processcentre.dto;

import java.util.List;

public record SearchProcessRequestDto(
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

    String workspaceId
) {

  public record SpecificFilterValueDto (
      String name,
      SpecificAttributeValueType type,

      String stringValue,
      Boolean booleanValue,

      Double numericValueLte,
      Double numericValueGte,

      Long dateValueLte,
      Long dateValueGte
  ){
  }

  public enum SpecificAttributeValueType {

    STRING,
    BOOLEAN,
    INTEGER,
    LONG,
    DOUBLE,
    DATE;

  }


}
