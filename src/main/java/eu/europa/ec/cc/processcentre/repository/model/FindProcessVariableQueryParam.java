package eu.europa.ec.cc.processcentre.repository.model;

public record FindProcessVariableQueryParam(
    String processInstanceId,
    String name
) {

}
