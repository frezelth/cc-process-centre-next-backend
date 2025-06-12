package eu.europa.ec.cc.processcentre.process.command.repository.model;

public record DeleteProcessStateQueryParam(
    String processInstanceId,
    String state
) {

}
