package eu.europa.ec.cc.processcentre.process.query.repository;

import eu.europa.ec.cc.processcentre.process.query.repository.model.SearchProcessQueryParam;
import eu.europa.ec.cc.processcentre.process.query.repository.model.SearchProcessQueryResponse;
import eu.europa.ec.cc.processcentre.process.query.repository.model.SearchProcessQueryResponseTask;
import eu.europa.ec.cc.processcentre.taxonomy.model.Sanitizable;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import org.mapstruct.Mapping;

@Mapper
public interface QueryMapper {

    @Mapping(source = "taxonomyPath", target = "taxonomyPath", qualifiedByName = "removeTaxonomyPrefix")
    List<SearchProcessQueryResponse> search(SearchProcessQueryParam param);

    List<SearchProcessQueryResponseTask> searchActiveTasks(List<String> processInstanceIds, String locale);

    default String removeTaxonomyPrefix(String value) {
        return Sanitizable.sanitize(value);
    }
}
