package eu.europa.ec.cc.processcentre.servicetask.converter;

import com.google.protobuf.Timestamp;
import eu.europa.ec.cc.processcentre.servicetask.repository.model.CreateServiceTaskQueryParam;
import eu.europa.ec.cc.processcentre.util.ProtoUtils;
import eu.europa.ec.cc.provider.servicetask.event.proto.ServiceTaskCreated;
import java.time.Instant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ServiceTaskEventConverter {

  @Mapping(source = "serviceTaskId", target = "taskInstanceId")
  @Mapping(source = "serviceTaskTypeKey", target = "taskTypeKey")
  CreateServiceTaskQueryParam toCreateServiceTaskQueryParam(ServiceTaskCreated event);

  default Instant toInstant(Timestamp timestamp) {
    return ProtoUtils.timestampToInstant(timestamp);
  }

}
