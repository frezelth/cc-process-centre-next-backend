package eu.europa.ec.cc.processcentre.event;

import java.time.Instant;

public record ProcessRegistered(
    String processInstanceId,
    String providerId,
    String domainKey,
    String processTypeKey,
    String userId,
    String onBehalfOfUserId,
    Instant createdOn
) {

}
