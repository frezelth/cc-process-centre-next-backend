package eu.europa.ec.cc.processcentre.repository.model;

import java.util.List;
import java.util.Map;

public record SearchProcessQueryResponse(
    String processInstanceId,
    Map<String, String> translations,
    List<SearchProcessQueryResponseTask> tasks
) {

}
