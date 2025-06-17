package eu.europa.ec.cc.processcentre.servicetask.repository.model;

import eu.europa.ec.cc.processcentre.servicetask.model.ServiceTaskStatus;
import java.time.Instant;

public record ChangeStatusServiceTaskQueryParam(
    String taskInstanceId,
    ServiceTaskStatus status,
    Instant timestamp,
    String errorMsg,
    String infoMsg
) {

}
