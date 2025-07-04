package eu.europa.ec.cc.processcentre.process.command.converter;

import com.google.protobuf.Timestamp;
import eu.europa.ec.cc.processcentre.model.ProcessAction;
import eu.europa.ec.cc.processcentre.model.ProcessStatus;
import eu.europa.ec.cc.processcentre.model.VariableType;
import eu.europa.ec.cc.processcentre.process.command.repository.model.DeleteProcessPortfolioItems;
import eu.europa.ec.cc.processcentre.process.command.repository.model.DeleteProcessQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.DeleteProcessStateQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.InsertProcessActionLogQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.InsertProcessPortfolioItems;
import eu.europa.ec.cc.processcentre.process.command.repository.model.InsertProcessQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.InsertProcessStateQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.UpdateBusinessStatusQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.UpdateProcessResponsibleOrganisationQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.UpdateProcessResponsibleUserQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.UpdateProcessStatusQueryParam;
import eu.europa.ec.cc.processcentre.util.ProtoUtils;
import eu.europa.ec.cc.provider.proto.ProcessAssociatedPortfolioItemAdded;
import eu.europa.ec.cc.provider.proto.ProcessAssociatedPortfolioItemRemoved;
import eu.europa.ec.cc.provider.proto.ProcessBusinessStatusChanged;
import eu.europa.ec.cc.provider.proto.ProcessCancelled;
import eu.europa.ec.cc.provider.proto.ProcessCreated;
import eu.europa.ec.cc.provider.proto.ProcessDeleted;
import eu.europa.ec.cc.provider.proto.ProcessResponsibleOrganisationChanged;
import eu.europa.ec.cc.provider.proto.ProcessResponsibleUserChanged;
import eu.europa.ec.cc.provider.proto.ProcessRestored;
import eu.europa.ec.cc.provider.proto.ProcessRunningStatusChanged;
import eu.europa.ec.cc.provider.proto.ProcessRunningStatusChanged.Status;
import eu.europa.ec.cc.provider.proto.ProcessStateChanged;
import eu.europa.ec.cc.variables.proto.VariableValue.KindCase;
import java.time.Instant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventConverter {

  InsertProcessQueryParam toInsertProcessQueryParam(ProcessCreated event);

  InsertProcessQueryParam toInsertProcessQueryParam(ProcessRestored event);

  DeleteProcessQueryParam toDeleteProcessQueryParam(ProcessDeleted event);

  InsertProcessStateQueryParam toInsertProcessStateQueryParam(ProcessStateChanged event);

  DeleteProcessStateQueryParam toDeleteProcessStateQueryParam(ProcessStateChanged event);

  default Instant toInstant(Timestamp timestamp) {
    return ProtoUtils.timestampToInstant(timestamp);
  }

  // protobuf never deserializes to empty strings, handle it here
  default String mapEmptyString(String string) {
    return string != null && !string.isEmpty() ? string : null;
  }

  UpdateBusinessStatusQueryParam toUpdateBusinessStatus(ProcessBusinessStatusChanged event);

  @Mapping(target = "action", expression =  "java(eu.europa.ec.cc.processcentre.model.ProcessAction.CANCEL)")
  @Mapping(target = "timestamp", source = "cancelledOn")
  InsertProcessActionLogQueryParam toInsertProcessRunningStatusQueryParam(
      ProcessCancelled event);

  @Mapping(target = "timestamp", source = "changedOn")
  @Mapping(target = "action", source = "newStatus")
  InsertProcessActionLogQueryParam toInsertProcessRunningStatusQueryParam(
      ProcessRunningStatusChanged event);

  UpdateProcessResponsibleUserQueryParam toUpdateProcessResponsibleUserQueryParam(
      ProcessResponsibleUserChanged event);

  @Mapping(source = "newStatus", target = "status")
  UpdateProcessStatusQueryParam toUpdateProcessRunningStatusQueryParam(
      ProcessRunningStatusChanged event
  );

  @Mapping(expression = "java(eu.europa.ec.cc.processcentre.model.ProcessStatus.CANCELLED)", target = "status")
  UpdateProcessStatusQueryParam toUpdateProcessRunningStatusQueryParam(
      ProcessCancelled event
  );

  default ProcessStatus mapToStatus(Status proto){
    return switch (proto){
      case ONGOING -> ProcessStatus.ONGOING;
      case PAUSED -> ProcessStatus.PAUSED;
      case ENDED -> ProcessStatus.COMPLETED;
      case UNRECOGNIZED -> null;
    };
  }

  default VariableType mapToVariableType(KindCase proto){
    return switch (proto){
      case STRINGVALUE -> VariableType.STRING;
      case BOOLEANVALUE -> VariableType.BOOLEAN;
      case INTEGERVALUE -> VariableType.INTEGER;
      case LONGVALUE -> VariableType.LONG;
      case DOUBLEVALUE -> VariableType.DOUBLE;
      case TIMEVALUE -> VariableType.DATE;
      case BYTESVALUE, DELETED, KIND_NOT_SET -> null;
    };
  }

  default ProcessAction maptoProcessAction(Status proto){
    return switch (proto){
      case ONGOING -> ProcessAction.START;
      case PAUSED -> ProcessAction.PAUSE;
      case ENDED -> ProcessAction.COMPLETE;
      case UNRECOGNIZED -> null;
    };
  }

  @Mapping(source = "portfolioItemIdsList", target = "portfolioItemIds")
  InsertProcessPortfolioItems toInsertProcessPortfolioItem(ProcessAssociatedPortfolioItemAdded event);

  @Mapping(source = "portfolioItemIdsList", target = "portfolioItemIds")
  DeleteProcessPortfolioItems toDeleteProcessPortfolioItem(ProcessAssociatedPortfolioItemRemoved event);

  UpdateProcessResponsibleOrganisationQueryParam toUpdateProcessResponsibleOrganisationQueryParam(
      ProcessResponsibleOrganisationChanged event);
}
