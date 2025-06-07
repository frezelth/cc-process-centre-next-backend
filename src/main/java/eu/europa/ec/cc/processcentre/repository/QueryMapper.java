package eu.europa.ec.cc.processcentre.repository;

import eu.europa.ec.cc.processcentre.repository.model.FindProcessByIdQueryResponse;
import eu.europa.ec.cc.processcentre.repository.model.SearchProcessQueryParam;
import eu.europa.ec.cc.processcentre.repository.model.SearchProcessQueryResponse;
import eu.europa.ec.cc.processcentre.repository.model.SearchProcessQueryResponseTask;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface QueryMapper {

    Optional<FindProcessByIdQueryResponse> findById(String id);

    List<SearchProcessQueryResponse> search(SearchProcessQueryParam param);

    List<SearchProcessQueryResponseTask> searchActiveTasks(List<String> processInstanceIds, String locale);

}
