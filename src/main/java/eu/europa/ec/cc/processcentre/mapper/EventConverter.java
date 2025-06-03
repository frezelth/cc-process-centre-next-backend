package eu.europa.ec.cc.processcentre.mapper;

import eu.europa.ec.cc.processcentre.repository.model.CreateProcessQueryParam;
import eu.europa.ec.cc.provider.proto.ProcessCreated;
import java.time.Instant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventConverter {

  @Mapping(source = "event.userId", target = "startedBy")
  @Mapping(source = "event.onBehalfOfUserId", target = "startedOnBehalfOf")
  @Mapping(source = "event.associatedPortfolioItemIdsList", target = "associatedPortfolioItemIds")
  CreateProcessQueryParam toCreateProcessQueryParam(ProcessCreated event, Instant startedOn);

}
