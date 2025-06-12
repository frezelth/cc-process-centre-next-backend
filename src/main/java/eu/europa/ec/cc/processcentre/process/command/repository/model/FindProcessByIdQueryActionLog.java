package eu.europa.ec.cc.processcentre.process.command.repository.model;

import eu.europa.ec.cc.processcentre.model.ProcessAction;
import java.time.Instant;
import lombok.Data;

@Data
public class FindProcessByIdQueryActionLog {

  private String id;
  private String processInstanceId;
  private Instant timestamp;
  private ProcessAction action;

}
