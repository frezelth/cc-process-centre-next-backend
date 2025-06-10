package eu.europa.ec.cc.processcentre.process.query.repository;

import eu.europa.ec.cc.processcentre.process.query.repository.model.SearchProcessQueryParam;
import eu.europa.ec.cc.processcentre.process.query.repository.model.SearchProcessQueryResponse;
import eu.europa.ec.cc.processcentre.process.query.repository.model.SearchProcessQueryResponseTask;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface QueryMapper {

    List<SearchProcessQueryResponse> search(SearchProcessQueryParam param);

    List<SearchProcessQueryResponseTask> searchActiveTasks(List<String> processInstanceIds, String locale);

}
