package eu.europa.ec.cc.processcentre.mapper;

import com.google.protobuf.Timestamp;
import eu.europa.ec.cc.processcentre.repository.model.CancelProcessQueryParam;
import eu.europa.ec.cc.processcentre.repository.model.ChangeProcessStateQueryParam;
import eu.europa.ec.cc.processcentre.repository.model.ChangeProcessStateQueryParam.Change;
import eu.europa.ec.cc.processcentre.repository.model.CreateProcessQueryParam;
import eu.europa.ec.cc.processcentre.repository.model.DeleteProcessQueryParam;
import eu.europa.ec.cc.processcentre.util.ProtoUtils;
import eu.europa.ec.cc.provider.proto.ProcessCancelled;
import eu.europa.ec.cc.provider.proto.ProcessCreated;
import eu.europa.ec.cc.provider.proto.ProcessDeleted;
import eu.europa.ec.cc.provider.proto.ProcessStateChanged;
import java.time.Instant;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventConverter {

  @Mapping(source = "event.userId", target = "startedBy")
  @Mapping(source = "event.onBehalfOfUserId", target = "startedByOnBehalfOf")
  @Mapping(source = "event.createdOn", target = "startedOn")
  @Mapping(source = "event.associatedPortfolioItemIdsList", target = "associatedPortfolioItemIds")
  CreateProcessQueryParam toCreateProcessQueryParam(ProcessCreated event);

  @Mapping(source = "event.userId", target = "cancelledBy")
  @Mapping(source = "event.onBehalfOfUserId", target = "cancelledByOnBehalfOf")
  CancelProcessQueryParam toCancelProcessQueryParam(ProcessCancelled event);

  @Mapping(source = "event.userId", target = "deletedBy")
  @Mapping(source = "event.onBehalfOfUserId", target = "deletedByOnBehalfOf")
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
}
