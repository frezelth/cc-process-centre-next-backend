package eu.europa.ec.cc.processcentre.process.command.repository.model;

import eu.europa.ec.cc.processcentre.model.ProcessStatus;

public record UpdateProcessStatusQueryParam(
    String processInstanceId,
    ProcessStatus status
) {

}
