package eu.europa.ec.cc.processcentre.dto;

import java.util.List;

public record SearchProcessResponseDto(
    String processInstanceId,
    String title,
    List<SearchProcessResponseDtoActiveTask> tasks
) {

  public record SearchProcessResponseDtoActiveTask(
      String taskInstanceId,
      String title
  ){}

}
