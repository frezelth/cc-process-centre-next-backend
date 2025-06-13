package eu.europa.ec.cc.processcentre.process.command.repository.model;

public record FindProcessConfigByIdQueryResponse(
    String processInstanceId,
    String type,
    String resultCard
) {

}
