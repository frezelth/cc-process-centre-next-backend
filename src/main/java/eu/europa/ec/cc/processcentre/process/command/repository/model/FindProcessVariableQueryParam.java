package eu.europa.ec.cc.processcentre.process.command.repository.model;

public record FindProcessVariableQueryParam(
    String processInstanceId,
    String name
) {

}
