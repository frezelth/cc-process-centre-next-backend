package eu.europa.ec.cc.processcentre.event;

import eu.europa.ec.cc.processcentre.config.ProcessTypeConfig;

public record ProcessConfigUpdated(
    String processInstanceId,
    ProcessTypeConfig config
) {

}
