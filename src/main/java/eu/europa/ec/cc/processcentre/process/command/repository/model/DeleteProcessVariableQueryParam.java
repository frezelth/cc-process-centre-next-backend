package eu.europa.ec.cc.processcentre.process.command.repository.model;

public record DeleteProcessVariableQueryParam(
    String processInstanceId,
    String name
) {

}
