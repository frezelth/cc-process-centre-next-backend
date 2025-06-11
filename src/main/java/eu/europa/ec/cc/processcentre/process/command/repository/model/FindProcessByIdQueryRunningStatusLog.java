package eu.europa.ec.cc.processcentre.process.command.repository.model;

import eu.europa.ec.cc.processcentre.model.ProcessRunningStatus;
import java.time.Instant;
import lombok.Data;

@Data
public class FindProcessByIdQueryRunningStatusLog {

  private String id;
  private String processInstanceId;
  private Instant timestamp;
  private ProcessRunningStatus status;

}
