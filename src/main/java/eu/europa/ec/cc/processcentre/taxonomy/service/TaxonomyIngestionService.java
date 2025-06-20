package eu.europa.ec.cc.processcentre.taxonomy.service;

import eu.europa.ec.cc.processcentre.taxonomy.model.Sanitizable;
import eu.europa.ec.cc.processcentre.taxonomy.repository.TaxonomyMapper;
import eu.europa.ec.cc.processcentre.taxonomy.repository.model.SaveTaxonomyProcessTypeQueryParam;
import eu.europa.ec.cc.taxonomy.event.proto.BranchUpdated;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaxonomyIngestionService {

  private static final String TAXONOMY_REF_URI_PREFIX = "http://compass-corporate.cc.eu/process-type/";
  private final TaxonomyMapper taxonomyMapper;

  public TaxonomyIngestionService(TaxonomyMapper taxonomyMapper) {
    this.taxonomyMapper = taxonomyMapper;
  }

  @Transactional
  public void handle(BranchUpdated event){
    // update db and processes for all process types
    String newTaxonomyPath =
        Sanitizable.sanitize(event.getFullPath());

    this.taxonomyMapper.deleteTaxonomyProcessTypes(newTaxonomyPath);

    // retrieve the process types
    List<String> processTypeIds = event.getRefUrisList()
        .stream().map(TaxonomyIngestionService::extractProcessType)
        .filter(Objects::nonNull)
        .filter(t -> !t.contains("/"))
        .toList();

    this.taxonomyMapper.saveTaxonomyProcessTypes(
        new SaveTaxonomyProcessTypeQueryParam(newTaxonomyPath, processTypeIds)
    );

  }

  private static String extractProcessType(String refURI) {
    if (StringUtils.isNotEmpty(refURI) && refURI.startsWith(TAXONOMY_REF_URI_PREFIX)) {
      return refURI.substring(TAXONOMY_REF_URI_PREFIX.length());
    }
    return null;
  }
}
