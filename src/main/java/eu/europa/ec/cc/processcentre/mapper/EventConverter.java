package eu.europa.ec.cc.processcentre.mapper;

import com.google.protobuf.Timestamp;
import eu.europa.ec.cc.processcentre.process.command.repository.model.CancelProcessQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.ChangeProcessStateQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.ChangeProcessStateQueryParam.Change;
import eu.europa.ec.cc.processcentre.process.command.repository.model.CreateProcessQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.CreateTaskQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.DeleteProcessQueryParam;
import eu.europa.ec.cc.processcentre.util.ProtoUtils;
import eu.europa.ec.cc.provider.proto.ProcessCancelled;
import eu.europa.ec.cc.provider.proto.ProcessCreated;
import eu.europa.ec.cc.provider.proto.ProcessDeleted;
import eu.europa.ec.cc.provider.proto.ProcessStateChanged;
import eu.europa.ec.cc.provider.task.event.proto.TaskCreated;
import java.time.Instant;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventConverter {

  @Mapping(source = "userId", target = "startedBy")
  @Mapping(source = "onBehalfOfUserId", target = "startedByOnBehalfOf")
  @Mapping(source = "createdOn", target = "startedOn")
  @Mapping(source = "associatedPortfolioItemIdsList", target = "associatedPortfolioItemIds")
  CreateProcessQueryParam toCreateProcessQueryParam(ProcessCreated event);

  @Mapping(source = "userId", target = "cancelledBy")
  @Mapping(source = "onBehalfOfUserId", target = "cancelledByOnBehalfOf")
  CancelProcessQueryParam toCancelProcessQueryParam(ProcessCancelled event);

  @Mapping(source = "userId", target = "deletedBy")
  @Mapping(source = "onBehalfOfUserId", target = "deletedByOnBehalfOf")
  DeleteProcessQueryParam toDeleteProcessQueryParam(ProcessDeleted event);

  ChangeProcessStateQueryParam toChangeProcessStateQueryParam(ProcessStateChanged event);

  default Instant toInstant(Timestamp timestamp) {
    return ProtoUtils.timestampToInstant(timestamp);
  }

  default Change map(ProcessStateChanged.Change proto){
    return switch (proto){
      case ENTERING -> Change.ENTERING;
      case LEAVING -> Change.LEAVING;
      case UNRECOGNIZED -> null;
    };
  }

  @Mapping(source = "timestamp", target = "createdOn")
  CreateTaskQueryParam toCreateTaskQueryParam(TaskCreated event);
}
