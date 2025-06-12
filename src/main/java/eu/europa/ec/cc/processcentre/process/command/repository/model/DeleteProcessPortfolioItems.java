package eu.europa.ec.cc.processcentre.process.command.repository.model;

import java.util.List;

public record DeleteProcessPortfolioItems(
    String processInstanceId,
    List<String> portfolioItemIds
) {

}
