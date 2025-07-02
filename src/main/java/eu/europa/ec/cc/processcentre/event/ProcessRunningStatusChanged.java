package eu.europa.ec.cc.processcentre.event;

import eu.europa.ec.cc.processcentre.model.ProcessStatus;

public record ProcessRunningStatusChanged(
    String processInstanceId,
    ProcessStatus processStatus
) {

}
