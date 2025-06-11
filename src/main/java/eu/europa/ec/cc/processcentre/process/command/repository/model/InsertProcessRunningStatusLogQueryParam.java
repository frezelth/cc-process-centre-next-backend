package eu.europa.ec.cc.processcentre.process.command.repository.model;

import eu.europa.ec.cc.processcentre.model.ProcessRunningStatus;
import java.time.Instant;

public record InsertProcessRunningStatusLogQueryParam(
    String processInstanceId,
    ProcessRunningStatus status,
    Instant timestamp,
    String userId,
    String onBehalfOfUserId
) {

}
