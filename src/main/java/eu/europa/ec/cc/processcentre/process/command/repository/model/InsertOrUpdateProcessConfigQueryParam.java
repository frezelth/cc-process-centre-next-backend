package eu.europa.ec.cc.processcentre.process.command.repository.model;

public record InsertOrUpdateProcessConfigQueryParam(
    String processInstanceId,
    String processTypeConfig,
    String processResultCardConfig
) {

}
