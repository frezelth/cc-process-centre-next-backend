package eu.europa.ec.cc.processcentre.process.command.repository.model;

public record FindProcessContextQueryResponse(
    String domainKey,
    String providerId,
    String processTypeKey
) {

}
