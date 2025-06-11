package eu.europa.ec.cc.processcentre.process.query;

import eu.europa.ec.cc.processcentre.dto.SearchProcessRequestDto;
import eu.europa.ec.cc.processcentre.mapper.QueryConverter;
import eu.europa.ec.cc.processcentre.process.query.repository.QueryMapper;
import eu.europa.ec.cc.processcentre.process.query.repository.model.SearchProcessQueryParam;
import eu.europa.ec.cc.processcentre.process.query.repository.model.SearchProcessQueryResponse;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class ProcessQueries {

  private final QueryMapper queryMapper;
  private final QueryConverter queryConverter;

  public ProcessQueries(QueryMapper queryMapper, QueryConverter queryConverter) {
    this.queryMapper = queryMapper;
    this.queryConverter = queryConverter;
  }

  public List<SearchProcessQueryResponse> searchProcesses(
      SearchProcessRequestDto searchProcessDto,
      int offset, int limit, Locale locale, String username){
    SearchProcessQueryParam queryParam = queryConverter.toQueryParam(searchProcessDto, locale,
        username, limit, offset);
    return queryMapper.search(queryParam);

  }

}
