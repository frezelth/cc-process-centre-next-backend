package eu.europa.ec.cc.processcentre.taxonomy.repository.model;

import java.util.List;

public record SaveTaxonomyProcessTypeQueryParam(
    String taxonomyPath,
    List<String> processTypeIds
) {

}
