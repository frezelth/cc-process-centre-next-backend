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
    List<SearchProcessQueryResponse> search = queryMapper.search(queryParam);
    return search;
//    List<String> foundProcessIds = search.stream().map(SearchProcessQueryResponse::getProcessInstanceId).toList();
//    Map<String, List<SearchProcessQueryResponseTask>> tasksByProcessId = queryMapper.searchActiveTasks(
//            foundProcessIds, locale.getLanguage()).stream()
//        .collect(Collectors.groupingBy(SearchProcessQueryResponseTask::getProcessInstanceId));
//
//    return search.stream()
//        .map(s -> new SearchProcessResponseDto(
//            s.getProcessInstanceId(), s.getTranslations().get(TranslationAttribute.PROCESS_TITLE.name()),
//            tasksByProcessId.get(s.getProcessInstanceId()).stream().map(
//                t -> new SearchProcessResponseDtoActiveTask(t.getTaskInstanceId(), t.getTranslations().get(TranslationAttribute.TASK_TITLE.name()))
//            ).toList()
//        )).toList();

  }

}
