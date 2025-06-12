package eu.europa.ec.cc.processcentre.process.command.repository.model;

import java.util.List;

public record InsertProcessPortfolioItems(
    String processInstanceId,
    List<String> portfolioItemIds
) {

}
