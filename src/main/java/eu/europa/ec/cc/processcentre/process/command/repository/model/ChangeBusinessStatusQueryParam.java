package eu.europa.ec.cc.processcentre.process.command.repository.model;

public record ChangeBusinessStatusQueryParam(
    String processInstanceId,
    String businessStatus
) {

}
