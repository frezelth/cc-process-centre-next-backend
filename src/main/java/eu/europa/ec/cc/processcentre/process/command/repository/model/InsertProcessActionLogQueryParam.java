package eu.europa.ec.cc.processcentre.process.command.repository.model;

import eu.europa.ec.cc.processcentre.model.ProcessAction;
import java.time.Instant;

public record InsertProcessActionLogQueryParam(
    String processInstanceId,
    ProcessAction action,
    Instant timestamp,
    String userId,
    String onBehalfOfUserId
) {

}
