package eu.europa.ec.cc.processcentre.process.query.web.dto;

import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SearchProcessRequestDto
  {
    String providerId;
    String domainKey;
    String processTypeKey;
    String searchText;
    List<String> responsibleOrganisationCodes;

    String taxonomyPath;

    String graphElementKey;
    Long situationOnDate;
    Boolean ongoing;
    Boolean completed;
    Boolean paused;
    Boolean cancelled;
    Boolean filterByUserOrganisation;
    List<String> portfolioItemIds;
    List<SpecificFilterValueDto> specificAttributes;

    List<String> dynamicFilters;
    Integer startYear;

    Boolean favourite;

    Boolean sortDescending;

    String sortProperty;

    List<String> tags;
    List<String> businessStatuses;

    String workspaceId;

    @Value
    @Builder
    public static class SpecificFilterValueDto {
        String name;
        SpecificAttributeValueType type;

        String stringValue;
        Boolean booleanValue;

        Double numericValueLte;
        Double numericValueGte;

        Instant dateValueLte;
        Instant dateValueGte;

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
