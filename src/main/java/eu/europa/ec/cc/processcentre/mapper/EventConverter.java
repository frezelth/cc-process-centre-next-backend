package eu.europa.ec.cc.processcentre.mapper;

import com.google.protobuf.Timestamp;
import eu.europa.ec.cc.processcentre.repository.model.CancelProcessQueryParam;
import eu.europa.ec.cc.processcentre.repository.model.CreateProcessQueryParam;
import eu.europa.ec.cc.processcentre.util.ProtoUtils;
import eu.europa.ec.cc.provider.proto.ProcessCancelled;
import eu.europa.ec.cc.provider.proto.ProcessCreated;
import java.time.Instant;

import eu.europa.ec.cc.provider.task.event.proto.TaskCreated;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventConverter {

  @Mapping(source = "event.userId", target = "startedBy")
  @Mapping(source = "event.onBehalfOfUserId", target = "startedOnBehalfOf")
  @Mapping(source = "event.createdOn", target = "startedOn")
  @Mapping(source = "event.associatedPortfolioItemIdsList", target = "associatedPortfolioItemIds")
  CreateProcessQueryParam toCreateProcessQueryParam(ProcessCreated event);

  @Mapping(source = "event.userId", target = "cancelledBy")
  @Mapping(source = "event.onBehalfOfUserId", target = "cancelledOnBehalfOf")
  CancelProcessQueryParam toCancelProcessQueryParam(ProcessCancelled event);

  default Instant toInstant(Timestamp timestamp) {
    return ProtoUtils.timestampToInstant(timestamp);
  }
}
