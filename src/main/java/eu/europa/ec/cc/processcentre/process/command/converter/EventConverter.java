package eu.europa.ec.cc.processcentre.process.command.converter;

import com.google.protobuf.Timestamp;
import eu.europa.ec.cc.processcentre.model.ProcessRunningStatus;
import eu.europa.ec.cc.processcentre.process.command.repository.model.CancelProcessQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.ChangeBusinessStatusQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.ChangeProcessStateQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.ChangeProcessStateQueryParam.Change;
import eu.europa.ec.cc.processcentre.process.command.repository.model.CreateProcessQueryParam;
import eu.europa.ec.cc.processcentre.task.repository.model.CreateTaskQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.DeleteProcessQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.InsertProcessRunningStatusLogQueryParam;
import eu.europa.ec.cc.processcentre.util.ProtoUtils;
import eu.europa.ec.cc.provider.proto.ProcessBusinessStatusChanged;
import eu.europa.ec.cc.provider.proto.ProcessCancelled;
import eu.europa.ec.cc.provider.proto.ProcessCreated;
import eu.europa.ec.cc.provider.proto.ProcessDeleted;
import eu.europa.ec.cc.provider.proto.ProcessRunningStatusChanged;
import eu.europa.ec.cc.provider.proto.ProcessRunningStatusChanged.Status;
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

  ChangeBusinessStatusQueryParam toChangeBusinessStatus(ProcessBusinessStatusChanged event);

  @Mapping(target = "status", expression =  "java(eu.europa.ec.cc.processcentre.model.ProcessRunningStatus.CANCELLED)")
  @Mapping(target = "timestamp", source = "cancelledOn")
  InsertProcessRunningStatusLogQueryParam toChangeProcessRunningStatusQueryParam(
      ProcessCancelled event);

  @Mapping(target = "timestamp", source = "changedOn")
  @Mapping(target = "status", source = "newStatus")
  InsertProcessRunningStatusLogQueryParam toChangeProcessRunningStatusQueryParam(
      ProcessRunningStatusChanged event);

  default ProcessRunningStatus map(Status proto){
    return switch (proto){
      case ONGOING -> ProcessRunningStatus.ONGOING;
      case PAUSED -> ProcessRunningStatus.PAUSED;
      case ENDED -> ProcessRunningStatus.COMPLETED;
      case UNRECOGNIZED -> null;
    };
  }
}
