package eu.europa.ec.cc.processcentre.event;

import java.util.Set;

public record ProcessVariablesUpdated(
    String processInstanceId,
    Set<String> names
) {

}
