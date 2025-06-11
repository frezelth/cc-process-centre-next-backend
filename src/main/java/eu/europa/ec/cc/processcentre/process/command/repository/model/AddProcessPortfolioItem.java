package eu.europa.ec.cc.processcentre.process.command.repository.model;

public record AddProcessPortfolioItem(
    String processInstanceId,
    String portfolioItemId
) {

}
