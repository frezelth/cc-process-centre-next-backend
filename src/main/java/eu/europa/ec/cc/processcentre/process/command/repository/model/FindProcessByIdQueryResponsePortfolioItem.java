package eu.europa.ec.cc.processcentre.process.command.repository.model;

import lombok.Data;

@Data
public class FindProcessByIdQueryResponsePortfolioItem {

  private String processInstanceId;
  private String portfolioItemId;

}
