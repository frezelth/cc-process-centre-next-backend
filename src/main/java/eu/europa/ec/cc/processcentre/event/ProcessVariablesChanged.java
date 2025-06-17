package eu.europa.ec.cc.processcentre.event;

import java.util.Set;

public record ProcessVariablesChanged(
    String processInstanceId,
    Set<String> names
) {

}
