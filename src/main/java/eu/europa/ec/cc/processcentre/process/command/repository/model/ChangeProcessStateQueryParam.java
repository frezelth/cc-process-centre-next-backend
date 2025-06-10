package eu.europa.ec.cc.processcentre.process.command.repository.model;

import java.time.Instant;

public record ChangeProcessStateQueryParam(
    String processInstanceId,
    Change change,
    String state,
    Instant changedOn
) {

  public enum Change {
    ENTERING,
    LEAVING
  }

}
