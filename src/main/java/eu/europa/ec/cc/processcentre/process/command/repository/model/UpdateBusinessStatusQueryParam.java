package eu.europa.ec.cc.processcentre.process.command.repository.model;

public record UpdateBusinessStatusQueryParam(
    String processInstanceId,
    String businessStatus
) {

}
