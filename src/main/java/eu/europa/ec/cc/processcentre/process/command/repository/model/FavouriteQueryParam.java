package eu.europa.ec.cc.processcentre.process.command.repository.model;

public record FavouriteQueryParam(
    String processInstanceId,
    String userId
) {

}
