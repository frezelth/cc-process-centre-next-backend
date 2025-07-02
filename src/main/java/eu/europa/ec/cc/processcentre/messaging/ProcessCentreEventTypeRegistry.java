package eu.europa.ec.cc.processcentre.messaging;

import com.google.common.collect.Maps;
import com.google.protobuf.Descriptors;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import eu.europa.ec.cc.babel.proto.TranslationCreated;
import eu.europa.ec.cc.babel.proto.TranslationUpdated;
import eu.europa.ec.cc.intercomm.event.proto.MessageAdded;
import eu.europa.ec.cc.intercomm.event.proto.MessageDeleted;
import eu.europa.ec.cc.intercomm.event.proto.MessageUpdated;
import eu.europa.ec.cc.message.proto.CCMessage;
import eu.europa.ec.cc.participant.event.proto.ParticipantUpdated;
import eu.europa.ec.cc.processcentre.proto.AddComment;
import eu.europa.ec.cc.processcentre.proto.AddProcessFavourite;
import eu.europa.ec.cc.processcentre.proto.AddProcessTaxonomy;
import eu.europa.ec.cc.processcentre.proto.AssignTask;
import eu.europa.ec.cc.processcentre.proto.AssociateProcessPortfolioItem;
import eu.europa.ec.cc.processcentre.proto.CancelProcess;
import eu.europa.ec.cc.processcentre.proto.CancelServiceTask;
import eu.europa.ec.cc.processcentre.proto.CancelTask;
import eu.europa.ec.cc.processcentre.proto.ClaimTask;
import eu.europa.ec.cc.processcentre.proto.CompleteProcess;
import eu.europa.ec.cc.processcentre.proto.CompleteServiceTask;
import eu.europa.ec.cc.processcentre.proto.CompleteTask;
import eu.europa.ec.cc.processcentre.proto.CreateProcess;
import eu.europa.ec.cc.processcentre.proto.CreateProcessType;
import eu.europa.ec.cc.processcentre.proto.CreateServiceTask;
import eu.europa.ec.cc.processcentre.proto.CreateTask;
import eu.europa.ec.cc.processcentre.proto.DeleteComment;
import eu.europa.ec.cc.processcentre.proto.DeleteProcess;
import eu.europa.ec.cc.processcentre.proto.DeleteProcessPhysically;
import eu.europa.ec.cc.processcentre.proto.DeleteTask;
import eu.europa.ec.cc.processcentre.proto.DisassociateProcessPortfolioItem;
import eu.europa.ec.cc.processcentre.proto.FailServiceTask;
import eu.europa.ec.cc.processcentre.proto.LinkProcess;
import eu.europa.ec.cc.processcentre.proto.LinkProcessTypeProcessState;
import eu.europa.ec.cc.processcentre.proto.LinkProcessTypeTaskType;
import eu.europa.ec.cc.processcentre.proto.LockServiceTask;
import eu.europa.ec.cc.processcentre.proto.PauseProcess;
import eu.europa.ec.cc.processcentre.proto.RegisterProcessStateChange;
import eu.europa.ec.cc.processcentre.proto.RegisterRetryServiceTaskResponse;
import eu.europa.ec.cc.processcentre.proto.RegisterSkipServiceTaskResponse;
import eu.europa.ec.cc.processcentre.proto.RegisterTask;
import eu.europa.ec.cc.processcentre.proto.RemoveProcessFavourite;
import eu.europa.ec.cc.processcentre.proto.RemoveProcessSpecificAttribute;
import eu.europa.ec.cc.processcentre.proto.RemoveProcessTaxonomy;
import eu.europa.ec.cc.processcentre.proto.RestoreTask;
import eu.europa.ec.cc.processcentre.proto.StartProcess;
import eu.europa.ec.cc.processcentre.proto.UnclaimTask;
import eu.europa.ec.cc.processcentre.proto.UnlinkProcess;
import eu.europa.ec.cc.processcentre.proto.UnlockServiceTask;
import eu.europa.ec.cc.processcentre.proto.UpdateComment;
import eu.europa.ec.cc.processcentre.proto.UpdateDefaultBusinessDomainId;
import eu.europa.ec.cc.processcentre.proto.UpdateOrganisationInfo;
import eu.europa.ec.cc.processcentre.proto.UpdatePortfolioData;
import eu.europa.ec.cc.processcentre.proto.UpdatePortfolioTag;
import eu.europa.ec.cc.processcentre.proto.UpdateProcess;
import eu.europa.ec.cc.processcentre.proto.UpdateProcessBabelText;
import eu.europa.ec.cc.processcentre.proto.UpdateProcessBabelTranslation;
import eu.europa.ec.cc.processcentre.proto.UpdateProcessBusinessDomainId;
import eu.europa.ec.cc.processcentre.proto.UpdateProcessBusinessStatus;
import eu.europa.ec.cc.processcentre.proto.UpdateProcessContext;
import eu.europa.ec.cc.processcentre.proto.UpdateProcessDescription;
import eu.europa.ec.cc.processcentre.proto.UpdateProcessResponsibleOrganisation;
import eu.europa.ec.cc.processcentre.proto.UpdateProcessResultCardConfiguration;
import eu.europa.ec.cc.processcentre.proto.UpdateProcessSecurity;
import eu.europa.ec.cc.processcentre.proto.UpdateProcessSpecificAttribute;
import eu.europa.ec.cc.processcentre.proto.UpdateProcessTaxonomy;
import eu.europa.ec.cc.processcentre.proto.UpdateProcessTaxonomyPaths;
import eu.europa.ec.cc.processcentre.proto.UpdateProcessType;
import eu.europa.ec.cc.processcentre.proto.UpdateProcessTypeBabelTranslation;
import eu.europa.ec.cc.processcentre.proto.UpdateProcessTypeConfiguration;
import eu.europa.ec.cc.processcentre.proto.UpdateProcessVariables;
import eu.europa.ec.cc.processcentre.proto.UpdateTask;
import eu.europa.ec.cc.processcentre.proto.UpdateUserInfo;
import eu.europa.ec.cc.processcentre.proto.UpdateUserInfo4ActionLog;
import eu.europa.ec.cc.provider.proto.PortfolioItemCreated;
import eu.europa.ec.cc.provider.proto.PortfolioItemDescriptionChanged;
import eu.europa.ec.cc.provider.proto.PortfolioItemTypeChanged;
import eu.europa.ec.cc.provider.proto.ProcessAssociatedPortfolioItemAdded;
import eu.europa.ec.cc.provider.proto.ProcessAssociatedPortfolioItemRemoved;
import eu.europa.ec.cc.provider.proto.ProcessBusinessDomainIdUpdated;
import eu.europa.ec.cc.provider.proto.ProcessBusinessStatusChanged;
import eu.europa.ec.cc.provider.proto.ProcessCancelled;
import eu.europa.ec.cc.provider.proto.ProcessCreated;
import eu.europa.ec.cc.provider.proto.ProcessDeleted;
import eu.europa.ec.cc.provider.proto.ProcessDescriptionChanged;
import eu.europa.ec.cc.provider.proto.ProcessLinkAdded;
import eu.europa.ec.cc.provider.proto.ProcessLinkRemoved;
import eu.europa.ec.cc.provider.proto.ProcessResponsibleOrganisationChanged;
import eu.europa.ec.cc.provider.proto.ProcessResponsibleUserChanged;
import eu.europa.ec.cc.provider.proto.ProcessRestored;
import eu.europa.ec.cc.provider.proto.ProcessRunningStatusChanged;
import eu.europa.ec.cc.provider.proto.ProcessSecurityUpdated;
import eu.europa.ec.cc.provider.proto.ProcessSpecificAttributeRemoved;
import eu.europa.ec.cc.provider.proto.ProcessSpecificAttributeUpdated;
import eu.europa.ec.cc.provider.proto.ProcessStateChanged;
import eu.europa.ec.cc.provider.proto.ProcessTaxonomyUpdated;
import eu.europa.ec.cc.provider.proto.ProcessTypeCreated;
import eu.europa.ec.cc.provider.proto.ProcessTypeDefaultBusinessDomainIdUpdated;
import eu.europa.ec.cc.provider.proto.ProcessVariableUpdated;
import eu.europa.ec.cc.provider.proto.ProcessVariablesUpdated;
import eu.europa.ec.cc.provider.servicetask.event.proto.RetryServiceTaskResponse;
import eu.europa.ec.cc.provider.servicetask.event.proto.ServiceTaskCancelled;
import eu.europa.ec.cc.provider.servicetask.event.proto.ServiceTaskCompleteFailed;
import eu.europa.ec.cc.provider.servicetask.event.proto.ServiceTaskCompleteSucceeded;
import eu.europa.ec.cc.provider.servicetask.event.proto.ServiceTaskCreated;
import eu.europa.ec.cc.provider.servicetask.event.proto.ServiceTaskLocked;
import eu.europa.ec.cc.provider.servicetask.event.proto.ServiceTaskUnlocked;
import eu.europa.ec.cc.provider.servicetask.event.proto.SkipServiceTaskResponse;
import eu.europa.ec.cc.provider.task.event.proto.TaskAssigned;
import eu.europa.ec.cc.provider.task.event.proto.TaskCancelled;
import eu.europa.ec.cc.provider.task.event.proto.TaskClaimed;
import eu.europa.ec.cc.provider.task.event.proto.TaskCompleted;
import eu.europa.ec.cc.provider.task.event.proto.TaskCreated;
import eu.europa.ec.cc.provider.task.event.proto.TaskDeleted;
import eu.europa.ec.cc.provider.task.event.proto.TaskEntered;
import eu.europa.ec.cc.provider.task.event.proto.TaskRestored;
import eu.europa.ec.cc.provider.task.event.proto.TaskTypeCreated;
import eu.europa.ec.cc.provider.task.event.proto.TaskUnclaimed;
import eu.europa.ec.cc.provider.task.event.proto.TaskUpdated;
import eu.europa.ec.cc.tagservice.event.proto.ObjectTagged;
import eu.europa.ec.cc.tagservice.event.proto.ObjectUntagged;
import eu.europa.ec.cc.taskcenter.event.proto.TaskRegistered;
import eu.europa.ec.cc.taxonomy.event.proto.BranchDeleted;
import eu.europa.ec.cc.taxonomy.event.proto.BranchUpdated;
import eu.europa.ec.cc.userprofile.proto.OrganisationChanged;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

@Slf4j
public class ProcessCentreEventTypeRegistry {
  private static final Set<Class<? extends Message>> TYPES = Set.of(
      ProcessTypeCreated.class,
      ProcessTypeDefaultBusinessDomainIdUpdated.class,
      ProcessCreated.class,
      ProcessRunningStatusChanged.class,
      ProcessStateChanged.class,
      ProcessTaxonomyUpdated.class,
      ProcessSecurityUpdated.class,
      ProcessSpecificAttributeUpdated.class,
      ProcessSpecificAttributeRemoved.class,
      ProcessVariableUpdated.class,
      ProcessVariablesUpdated.class,
      ProcessLinkAdded.class,
      ProcessLinkRemoved.class,
      ProcessResponsibleUserChanged.class,
      ProcessResponsibleOrganisationChanged.class,
      ProcessRestored.class,
      ProcessDescriptionChanged.class,
      ProcessBusinessDomainIdUpdated.class,
      ProcessBusinessStatusChanged.class,
      ProcessAssociatedPortfolioItemAdded.class,
      ProcessAssociatedPortfolioItemRemoved.class,
      UpdatePortfolioData.class,
      UpdatePortfolioTag.class,
      ProcessCancelled.class,
      ProcessDeleted.class,
      PortfolioItemCreated.class,
      PortfolioItemTypeChanged.class,
      PortfolioItemDescriptionChanged.class,
      TranslationCreated.class,
      TranslationUpdated.class,
      BranchUpdated.class,
      BranchDeleted.class,
      ObjectTagged.class,
      ObjectUntagged.class,
      TaskTypeCreated.class,
      TaskCreated.class,
      TaskDeleted.class,
      TaskEntered.class,
      TaskCompleted.class,
      TaskAssigned.class,
      TaskClaimed.class,
      TaskUnclaimed.class,
      TaskCancelled.class,
      TaskUpdated.class,
      TaskRestored.class,
      ServiceTaskCreated.class,
      ServiceTaskCompleteSucceeded.class,
      ServiceTaskCompleteFailed.class,
      ServiceTaskLocked.class,
      ServiceTaskUnlocked.class,
      RetryServiceTaskResponse.class,
      ServiceTaskCancelled.class,
      SkipServiceTaskResponse.class,
      TaskRegistered.class,
      ParticipantUpdated.class,
      OrganisationChanged.class,
      MessageAdded.class,
      MessageDeleted.class,
      MessageUpdated.class,
      AddComment.class,
      UpdateComment.class,
      DeleteComment.class,
      CreateProcess.class,
      UpdateProcess.class,
      UpdateProcessContext.class,
      UpdateProcessTypeConfiguration.class,
      UpdateProcessResultCardConfiguration.class,
      LinkProcess.class,
      UnlinkProcess.class,
      UpdateProcessResponsibleOrganisation.class,
      UpdateProcessDescription.class,
      AssociateProcessPortfolioItem.class,
      DisassociateProcessPortfolioItem.class,
      UpdateProcessSpecificAttribute.class,
      RemoveProcessSpecificAttribute.class,
      StartProcess.class,
      PauseProcess.class,
      CompleteProcess.class,
      RegisterProcessStateChange.class,
      CancelProcess.class,
      DeleteProcess.class,
      UpdateProcessVariables.class,
      UpdateProcessBusinessDomainId.class,
      CreateProcessType.class,
      UpdateProcessType.class,
      UpdateProcessSecurity.class,
      AddProcessTaxonomy.class,
      RemoveProcessTaxonomy.class,
      UpdateProcessTaxonomy.class,
      LinkProcessTypeTaskType.class,
      LinkProcessTypeProcessState.class,
      UpdateDefaultBusinessDomainId.class,
      CreateTask.class,
      CompleteTask.class,
      AssignTask.class,
      ClaimTask.class,
      UnclaimTask.class,
      CancelTask.class,
      DeleteTask.class,
      UpdateTask.class,
      RestoreTask.class,
      CreateServiceTask.class,
      CompleteServiceTask.class,
      FailServiceTask.class,
      LockServiceTask.class,
      UnlockServiceTask.class,
      RegisterRetryServiceTaskResponse.class,
      RegisterSkipServiceTaskResponse.class,
      CancelServiceTask.class,
      RegisterTask.class,
      UpdateOrganisationInfo.class,
      UpdateUserInfo.class,
      UpdateUserInfo4ActionLog.class,
      UpdateProcessBabelTranslation.class,
      UpdateProcessTypeBabelTranslation.class,
      UpdateProcessBabelText.class,
      DeleteProcessPhysically.class,
      UpdateProcessBusinessStatus.class,
      UpdateProcessTaxonomyPaths.class,
      AddProcessFavourite.class,
      RemoveProcessFavourite.class
  );

  private static final Map<String, Class<? extends Message>> TYPES_BY_FULL_NAME = Maps.newHashMap();

  /*
   * Initialize the TYPES_BY_FULL_NAME map using the TYPES map and reflections
   */
  static {
    TYPES.forEach(
        aClass -> {
          Method m;
          try {
            m = aClass.getMethod("getDescriptor");
            Descriptors.Descriptor descriptor = (Descriptors.Descriptor)m.invoke(null);
            TYPES_BY_FULL_NAME.put(descriptor.getFullName(), aClass);
          } catch (NoSuchMethodException | IllegalAccessException |
                   InvocationTargetException e) {
            LOG.error("Cannot read descriptor for type: {}", aClass, e);
            throw new RuntimeException(e);
          }
        }
    );
  }

  /**
   * Checks if the given CCMessage can be handled by portfolio
   * @param message
   * @return
   */
  public static boolean isProcessCentreMessage(CCMessage message){
    if (message == null) {
      return false;
    }

    // first check if process centre can handle or not the message type
    if (!ProcessCentreEventTypeRegistry.isInMessageList(message)){
      if (LOG.isDebugEnabled()){
        LOG.debug("Skipping message {}", message.getPayload().getTypeUrl());
      }
      return false;
    }

    // for taxonomy messages, check that the root node is portfolio
    if (message.getPayload().is(BranchUpdated.class)){
      try {
        BranchUpdated branchUpdated = message.getPayload().unpack(BranchUpdated.class);
        if (!isProcessCentreTaxonomy(branchUpdated.getTaxonomyKey())) {
          // taxonomy is not process centre, we skip the message
          return false;
        }
      } catch (InvalidProtocolBufferException e){
        // the protobuf message is invalid, what do we do with it?!
        LOG.error("Invalid protobuf message", e);
      }
    }

    if (message.getPayload().is(BranchDeleted.class)){
      try {
        BranchDeleted branchDeleted = message.getPayload().unpack(BranchDeleted.class);
        if (!isProcessCentreTaxonomy(branchDeleted.getTaxonomyKey())) {
          // taxonomy is not process centre, we skip the message
          return false;
        }
      } catch (InvalidProtocolBufferException e){
        LOG.error("Invalid protobuf message", e);
        // the protobuf message is invalid, we'll not store it and we skip it!
        return false;
      }
    }

    return true;

  }

  public static Message unpackMessage(CCMessage message){
    return TYPES.stream()
        .filter(message.getPayload()::is)
        .map(
        t -> {
          try {
            return message.getPayload().unpack(t);
          } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
          }
        }).findFirst().orElseThrow();
  }

  /**
   * Check if the message is part of the list of known process centre messages
   * @param message
   * @return
   */
  private static boolean isInMessageList(CCMessage message){
    return TYPES
        .stream()
        .anyMatch(aClass -> message.getPayload().is(aClass));
  }

  /**
   * Check if the taxonomy is handled by process centre
   * @param taxonomyKey
   * @return
   */
  private static boolean isProcessCentreTaxonomy(String taxonomyKey){
    return taxonomyKey != null && taxonomyKey.equals("ProcessCentre");
  }

  /**
   * Gets the protobuf class from its typeUrl in the proto Any
   * @param typeUrl
   * @return
   */
  public static Class<? extends Message> findClassByTypeUrl(@NonNull String typeUrl){
    String typeName = typeUrl.substring(typeUrl.lastIndexOf('/') + 1);
    return TYPES_BY_FULL_NAME.get(typeName);
  }
}
