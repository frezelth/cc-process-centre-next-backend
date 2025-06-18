package eu.europa.ec.cc.processcentre.process.query.web.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;
import eu.europa.ec.cc.processcentre.model.ProcessStatus;
import java.util.List;

public record SearchProcessResponseDto(
    long totalElements,
    List<SearchProcessResponseDtoProcess> processes,
    List<SortableField> sortableFields,
    List<String> commonColumns
) {

  public record SearchProcessResponseDtoProcess(
      String processId,
      String processTitle,
      ProcessStatus status,
      @JsonRawValue String cardLayout,
      boolean favourite,
      @JsonRawValue String activeTasks
  ){}

  public record SortableField(

  ){}

  public record SearchProcessResponseDtoActiveTask(
      String taskInstanceId,
      String title
  ){}

}
