package eu.europa.ec.cc.processcentre.process.command.repository.model;

import eu.europa.ec.cc.processcentre.model.ProcessRunningStatus;
import java.time.Instant;

public record ChangeProcessRunningStatusQueryParam(
    String processInstanceId,
    ProcessRunningStatus status,
    Instant changedOn,
    String userId,
    String onBehalfOfUserId
) {

}
