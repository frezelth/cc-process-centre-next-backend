package eu.europa.ec.cc.processcentre.task.converter;

import com.google.protobuf.Timestamp;
import eu.europa.ec.cc.processcentre.task.model.TaskStatus;
import eu.europa.ec.cc.processcentre.task.repository.model.CreateTaskQueryParam;
import eu.europa.ec.cc.processcentre.task.repository.model.UpdateTaskQueryParam;
import eu.europa.ec.cc.processcentre.util.ProtoUtils;
import eu.europa.ec.cc.provider.task.event.proto.TaskCreated;
import eu.europa.ec.cc.provider.task.event.proto.TaskUpdated;
import java.time.Instant;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TaskEventConverter {

  @Mapping(source = "timestamp", target = "createdOn")
  @Mapping(target = "status", expression = "java(computeTaskStatus(event))")
  @Mapping(target = "assignedOn", source = "claimDate")
  @Mapping(target = "assignedTo", source = "claimerUserId")
  @Mapping(target = "assignedToOnBehalfOf", source = "claimerOnBehalfOfUserId")
  CreateTaskQueryParam toCreateTaskQueryParam(TaskCreated event);

  default Instant toInstant(Timestamp timestamp) {
    return ProtoUtils.timestampToInstant(timestamp);
  }

  default TaskStatus computeTaskStatus(TaskCreated event){
    if (StringUtils.isNotEmpty(event.getClaimerUserId())){
      return TaskStatus.ASSIGNED;
    }
    return TaskStatus.CREATED;
  }

}
