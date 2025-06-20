package eu.europa.ec.cc.processcentre.taxonomy.repository;

import eu.europa.ec.cc.processcentre.taxonomy.repository.model.SaveTaxonomyProcessTypeQueryParam;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaxonomyMapper {

  void saveTaxonomyProcessTypes(SaveTaxonomyProcessTypeQueryParam param);

  void deleteTaxonomyProcessTypes(String taxonomyPath);

}
