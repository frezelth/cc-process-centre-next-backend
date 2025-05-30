package eu.europa.ec.cc.processcentre.repository.model;

public record DeleteProcessVariableQueryParam(
    String processInstanceId,
    String name
) {

}
