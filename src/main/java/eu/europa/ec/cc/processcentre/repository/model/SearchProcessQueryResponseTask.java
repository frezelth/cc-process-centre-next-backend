package eu.europa.ec.cc.processcentre.repository.model;

import java.util.Map;

public record SearchProcessQueryResponseTask(
    String taskInstanceId,
    Map<String, String> translations
){

}
