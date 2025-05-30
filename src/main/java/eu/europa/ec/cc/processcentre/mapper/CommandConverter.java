package eu.europa.ec.cc.processcentre.mapper;

import eu.europa.ec.cc.processcentre.proto.UpdateProcess;
import eu.europa.ec.cc.processcentre.repository.model.CreateProcessQueryParam;
import java.time.Instant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommandConverter {

  @Mapping(source = "command.userId", target = "startedBy")
  @Mapping(source = "command.onBehalfOfUserId", target = "startedOnBehalfOf")
  @Mapping(source = "command.associatedPortfolioItemIdsList", target = "associatedPortfolioItemIds")
  CreateProcessQueryParam toQueryParam(UpdateProcess command, Instant startedOn);

}
