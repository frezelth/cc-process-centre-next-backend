package eu.europa.ec.cc.processcentre.task.converter;

import com.google.protobuf.Timestamp;
import eu.europa.ec.cc.processcentre.task.repository.model.CreateTaskQueryParam;
import eu.europa.ec.cc.processcentre.util.ProtoUtils;
import eu.europa.ec.cc.provider.task.event.proto.TaskCreated;
import java.time.Instant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TaskEventConverter {

  @Mapping(source = "timestamp", target = "createdOn")
  CreateTaskQueryParam toCreateTaskQueryParam(TaskCreated event);

  default Instant toInstant(Timestamp timestamp) {
    return ProtoUtils.timestampToInstant(timestamp);
  }

}
