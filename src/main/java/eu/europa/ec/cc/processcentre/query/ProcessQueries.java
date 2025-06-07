package eu.europa.ec.cc.processcentre.query;

import eu.europa.ec.cc.processcentre.dto.SearchProcessRequestDto;
import eu.europa.ec.cc.processcentre.dto.SearchProcessResponseDto;
import eu.europa.ec.cc.processcentre.dto.SearchProcessResponseDto.SearchProcessResponseDtoActiveTask;
import eu.europa.ec.cc.processcentre.mapper.QueryConverter;
import eu.europa.ec.cc.processcentre.repository.ProcessMapper;
import eu.europa.ec.cc.processcentre.repository.QueryMapper;
import eu.europa.ec.cc.processcentre.repository.model.FindProcessByIdQueryResponse;
import eu.europa.ec.cc.processcentre.repository.model.SearchProcessQueryParam;
import eu.europa.ec.cc.processcentre.repository.model.SearchProcessQueryResponse;
import eu.europa.ec.cc.processcentre.repository.model.SearchProcessQueryResponseTask;
import eu.europa.ec.cc.processcentre.translation.TranslationAttribute;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ProcessQueries {

  private final QueryMapper queryMapper;
  private final QueryConverter queryConverter;

  public ProcessQueries(QueryMapper queryMapper, QueryConverter queryConverter) {
    this.queryMapper = queryMapper;
    this.queryConverter = queryConverter;
  }

  public Optional<FindProcessByIdQueryResponse> findById(String id){
    return queryMapper.findById(id);
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
