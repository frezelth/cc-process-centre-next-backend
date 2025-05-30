package eu.europa.ec.cc.processcentre.messaging;

import static eu.europa.ec.cc.processcentre.util.Context.context;
import static eu.europa.ec.cc.processcentre.util.Context.isValidContext;
import static java.util.Collections.emptyList;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.ProtocolStringList;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.JsonFormat;
import eu.europa.ec.cc.babel.proto.BabelText;
import eu.europa.ec.cc.babel.proto.BabelText.Literal.LiteralValue;
import eu.europa.ec.cc.babel.proto.ISO6391LanguageCode;
import eu.europa.ec.cc.babel.proto.TranslationCreated;
import eu.europa.ec.cc.babel.proto.TranslationUpdated;
import eu.europa.ec.cc.intercomm.event.proto.MessageAdded;
import eu.europa.ec.cc.intercomm.event.proto.MessageDeleted;
import eu.europa.ec.cc.intercomm.event.proto.MessageUpdated;
import eu.europa.ec.cc.message.proto.CCMessage;
import eu.europa.ec.cc.message.proto.CCMessageFactory;
import eu.europa.ec.cc.message.proto.CCMessageHeader.Annotation;
import eu.europa.ec.cc.participant.event.proto.ParticipantUpdated;
import eu.europa.ec.cc.processcentre.proto.AddComment;
import eu.europa.ec.cc.processcentre.proto.AssignTask;
import eu.europa.ec.cc.processcentre.proto.AssociateProcessPortfolioItem;
import eu.europa.ec.cc.processcentre.proto.BabelTextField;
import eu.europa.ec.cc.processcentre.proto.CancelProcess;
import eu.europa.ec.cc.processcentre.proto.CancelServiceTask;
import eu.europa.ec.cc.processcentre.proto.CancelTask;
import eu.europa.ec.cc.processcentre.proto.ClaimTask;
import eu.europa.ec.cc.processcentre.proto.CompleteProcess;
import eu.europa.ec.cc.processcentre.proto.CompleteServiceTask;
import eu.europa.ec.cc.processcentre.proto.CompleteTask;
import eu.europa.ec.cc.processcentre.proto.CreateServiceTask;
import eu.europa.ec.cc.processcentre.proto.CreateTask;
import eu.europa.ec.cc.processcentre.proto.DeleteComment;
import eu.europa.ec.cc.processcentre.proto.DeleteProcess;
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
import eu.europa.ec.cc.processcentre.proto.RemoveProcessSpecificAttribute;
import eu.europa.ec.cc.processcentre.proto.RestoreTask;
import eu.europa.ec.cc.processcentre.proto.RestoreTask.Builder;
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
import eu.europa.ec.cc.processcentre.proto.UpdateProcessBabelTranslation;
import eu.europa.ec.cc.processcentre.proto.UpdateProcessBusinessDomainId;
import eu.europa.ec.cc.processcentre.proto.UpdateProcessBusinessStatus;
import eu.europa.ec.cc.processcentre.proto.UpdateProcessDescription;
import eu.europa.ec.cc.processcentre.proto.UpdateProcessResponsibleOrganisation;
import eu.europa.ec.cc.processcentre.proto.UpdateProcessResponsibleUser;
import eu.europa.ec.cc.processcentre.proto.UpdateProcessResultCardConfiguration;
import eu.europa.ec.cc.processcentre.proto.UpdateProcessSecurity;
import eu.europa.ec.cc.processcentre.proto.UpdateProcessSpecificAttribute;
import eu.europa.ec.cc.processcentre.proto.UpdateProcessTaxonomy;
import eu.europa.ec.cc.processcentre.proto.UpdateProcessTaxonomyPaths;
import eu.europa.ec.cc.processcentre.proto.UpdateProcessType;
import eu.europa.ec.cc.processcentre.proto.UpdateProcessTypeConfiguration;
import eu.europa.ec.cc.processcentre.proto.UpdateProcessVariables;
import eu.europa.ec.cc.processcentre.proto.UpdateTask;
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
import eu.europa.ec.cc.tagservice.model.proto.TagValueList;
import eu.europa.ec.cc.taskcenter.event.proto.TaskRegistered;
import eu.europa.ec.cc.taxonomy.event.proto.BranchDeleted;
import eu.europa.ec.cc.taxonomy.event.proto.BranchUpdated;
import eu.europa.ec.cc.userprofile.proto.OrganisationChanged;
import eu.europa.ec.cc.variables.proto.VariableValue;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.core.log.LogAccessor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.BatchListenerFailedException;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.kafka.support.serializer.SerializationUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
@Slf4j
public class EventListener {

//  private final KafkaSender kafkaSender;
//  private final TracingHelper tracing;
//
//  public EventListener(KafkaSender kafkaSender,
//      TracingHelper tracing) {
//    this.kafkaSender = kafkaSender;
//    this.tracing = tracing;
//  }
//
//  @KafkaListener(topics = "${kafka.event-topic.name}",
//      id = "processcentre.ccevent.listener",
//      idIsGroup = false,
//      batch = "true"
//  )
//  public void on(List<ConsumerRecord<String, CCMessage>> records) {
//    if (records != null) {
//      List<Long> offsets = records.stream()
//          .filter(Objects::nonNull)
//          .map(ConsumerRecord::offset).toList();
//      List<Integer> partitions = records.stream()
//          .filter(Objects::nonNull)
//          .map(ConsumerRecord::partition).toList();
//      List<String> messageKeys = records.stream()
//          .filter(Objects::nonNull)
//          .map(ConsumerRecord::key).toList();
//      for (int i = 0; i < records.size(); i++) {
//        ConsumerRecord<String, CCMessage> consumerRecord = records.get(i);
//
//        if (consumerRecord == null) {
//          // should not happen, but who knows?
//          continue;
//        }
//
//        final CCMessage message = records.get(i).value();
//
//        // message is null with no deserialization exception, dont process it, normally should never happen
//        // but who knows
//        if (message == null) {
//          handleDeserializationException(consumerRecord, i);
//        }
//
//        if (ProcessCentreEventTypeRegistry.isProcessCentreMessage(message) &&
//            isEndUser(message)) {
//
//          handleRecord(message, i);
//        }
//
//      }
//    }
//  }
//
//  private void handleRecord(CCMessage message,
//      int idx) {
//    // send all converted messages to the command topic
//    try {
//      List<Message<CCMessage>> converted = convert(message);
//      converted.forEach(kafkaSender::sendToCommandTopic);
//    } catch (Exception e) {
//      throw new BatchListenerFailedException("Error processing kafka message", e, idx);
//    }
//  }
//
//  private static void handleDeserializationException(
//      ConsumerRecord<String, CCMessage> consumerRecord, int i) {
//    DeserializationException deserEx = SerializationUtils.getExceptionFromHeader(consumerRecord,
//        SerializationUtils.VALUE_DESERIALIZER_EXCEPTION_HEADER,
//        new LogAccessor(EventListener.class));
//    if (deserEx != null) {
//      LOG.error("Record at offset {} could not be deserialized", consumerRecord.offset(),
//          deserEx);
//      throw new BatchListenerFailedException("Deserialization", deserEx, i);
//    }
//  }
//
//  /**
//   * Returns true if the message header is not flagged with NON_END_USER annotation
//   *
//   * @param message the message to analyze
//   * @return true if the message has no NON_END_USER annotation, false otherwise
//   */
//  private boolean isEndUser(CCMessage message) {
//    return message.getHeader()
//        .getAnnotationList()
//        .stream()
//        .noneMatch(annotation -> annotation == Annotation.NON_END_USER);
//  }
//
//  public List<Message<CCMessage>> convert(CCMessage message)
//      throws InvalidProtocolBufferException {
//
//    List<Message<CCMessage>> list = new ArrayList<>();
//    boolean added = addProcessTypeMessages(list, message)
//        || addProcessMessages(list, message)
//        || addPortfolioMessages(list, message)
//        || addProcessVariableMessages(list, message)
//        || addTagMessages(list, message)
//        || addBabelMessages(list, message)
//        || addTaxonomyMessages(list, message)
//        || addTaskMessage(list, message)
//        || addServiceTaskMessages(list, message)
//        || addParticipantMessages(list, message)
//        || addUserMessages(list, message)
//        || addCommentsMessage(list, message);
//
//    if (!added) {
//      LOG.warn("Message {} is ignored; id:{}, value:\n{}",
//          message.getPayload().getTypeUrl(), message.getHeader().getId(),
//          message.getPayload().getValue());
//    }
//
//    return list;
//  }
//
//  private boolean addProcessMessages(List<Message<CCMessage>> list, CCMessage message) throws InvalidProtocolBufferException {
//    if (message.getPayload().is(ProcessCreated.class)) {
//      list.addAll(
//          on(message.getPayload().unpack(ProcessCreated.class), message.getHeader().getCreatedOn()));
//    } else if (message.getPayload().is(ProcessRunningStatusChanged.class)) {
//      list.addAll(on(message.getPayload().unpack(ProcessRunningStatusChanged.class),
//          message.getHeader().getCreatedOn()));
//    } else if (message.getPayload().is(ProcessStateChanged.class)) {
//      list.addAll(on(message.getPayload().unpack(ProcessStateChanged.class),
//          message.getHeader().getCreatedOn()));
//    } else if (message.getPayload().is(ProcessSecurityUpdated.class)) {
//      list.addAll(on(message.getPayload().unpack(ProcessSecurityUpdated.class)));
//    } else if (message.getPayload().is(ProcessLinkAdded.class)) {
//      list.addAll(on(message.getPayload().unpack(ProcessLinkAdded.class)));
//    } else if (message.getPayload().is(ProcessLinkRemoved.class)) {
//      list.addAll(on(message.getPayload().unpack(ProcessLinkRemoved.class)));
//    } else if (message.getPayload().is(ProcessResponsibleUserChanged.class)) {
//      list.addAll(on(message.getPayload().unpack(ProcessResponsibleUserChanged.class)));
//    } else if (message.getPayload().is(ProcessResponsibleOrganisationChanged.class)) {
//      list.addAll(
//          on(message.getPayload().unpack(ProcessResponsibleOrganisationChanged.class)));
//    } else if (message.getPayload().is(ProcessDescriptionChanged.class)) {
//      list.addAll(on(message.getPayload().unpack(ProcessDescriptionChanged.class)));
//    } else if (message.getPayload().is(ProcessBusinessDomainIdUpdated.class)) {
//      list.addAll(on(message.getPayload().unpack(ProcessBusinessDomainIdUpdated.class)));
//    } else if (message.getPayload().is(ProcessCancelled.class)) {
//      list.addAll(on(message.getPayload().unpack(ProcessCancelled.class)));
//    } else if (message.getPayload().is(ProcessDeleted.class)) {
//      list.addAll(on(message.getPayload().unpack(ProcessDeleted.class)));
//    } else if (message.getPayload().is(ProcessBusinessStatusChanged.class)) {
//      list.addAll(on(message.getPayload().unpack(ProcessBusinessStatusChanged.class)));
//    } else if (message.getPayload().is(ProcessRestored.class)) {
//      list.addAll(on(message.getPayload().unpack(ProcessRestored.class)));
//    } else {
//      return false;
//    }
//    return true;
//  }
//
//  private boolean addTaxonomyMessages(List<Message<CCMessage>> list, CCMessage message) throws InvalidProtocolBufferException {
//    /* Listen on the events defined by the Process Centre */
//    if (message.getPayload().is(ProcessTaxonomyUpdated.class)) {
//      list.addAll(on(message.getPayload().unpack(ProcessTaxonomyUpdated.class)));
//    }
//    /* Listen on the events defined by the taxonomy service*/
//    else if (message.getPayload().is(BranchUpdated.class)) {
//      list.addAll(on(message.getPayload().unpack(BranchUpdated.class)));
//    } else if (message.getPayload().is(BranchDeleted.class)) {
//      list.addAll(on(message.getPayload().unpack(BranchDeleted.class)));
//    } else {
//      return false;
//    }
//    return true;
//  }
//
//
//  private boolean addParticipantMessages(List<Message<CCMessage>> list, CCMessage message) throws InvalidProtocolBufferException {
//    /* Listen on the events defined by the participant container */
//    if (message.getPayload().is(ParticipantUpdated.class)) {
//      list.addAll(on(message.getPayload().unpack(ParticipantUpdated.class)));
//    } else {
//      return false;
//    }
//    return true;
//  }
//
//
//  private boolean addUserMessages(List<Message<CCMessage>> list, CCMessage message) throws InvalidProtocolBufferException {
//    /* Listen on the events defined by the user profile */
//    if (message.getPayload().is(OrganisationChanged.class)) {
//      list.addAll(on(message.getPayload().unpack(OrganisationChanged.class)));
//    } else {
//      return false;
//    }
//    return true;
//  }
//
//  private boolean addProcessVariableMessages(List<Message<CCMessage>> list, CCMessage message) throws InvalidProtocolBufferException {
//    if (message.getPayload().is(ProcessSpecificAttributeUpdated.class)) {
//      list.addAll(on(message.getPayload().unpack(ProcessSpecificAttributeUpdated.class)));
//    } else if (message.getPayload().is(ProcessSpecificAttributeRemoved.class)) {
//      list.addAll(on(message.getPayload().unpack(ProcessSpecificAttributeRemoved.class)));
//    } else if (message.getPayload().is(ProcessVariableUpdated.class)) {
//      list.addAll(on(message.getPayload().unpack(ProcessVariableUpdated.class)));
//    } else if (message.getPayload().is(ProcessVariablesUpdated.class)) {
//      list.addAll(on(message.getPayload().unpack(ProcessVariablesUpdated.class)));
//    } else {
//      return false;
//    }
//    return true;
//  }
//
//  private boolean addPortfolioMessages(List<Message<CCMessage>> list, CCMessage message) throws InvalidProtocolBufferException {
//    // Listen on the events defined by the Process Centre
//    if (message.getPayload().is(ProcessAssociatedPortfolioItemAdded.class)) {
//      list.addAll(
//          on(message.getPayload().unpack(ProcessAssociatedPortfolioItemAdded.class)));
//    } else if (message.getPayload().is(ProcessAssociatedPortfolioItemRemoved.class)) {
//      list.addAll(
//          on(message.getPayload().unpack(ProcessAssociatedPortfolioItemRemoved.class)));
//    } else
//      // Listen on the events defined by the portfolio Manager
//      if (message.getPayload().is(PortfolioItemCreated.class)) {
//        list.addAll(on(message.getPayload().unpack(PortfolioItemCreated.class)));
//      } else if (message.getPayload().is(PortfolioItemTypeChanged.class)) {
//        list.addAll(on(message.getPayload().unpack(PortfolioItemTypeChanged.class)));
//      } else if (message.getPayload().is(PortfolioItemDescriptionChanged.class)) {
//        list.addAll(
//            on(message.getPayload().unpack(PortfolioItemDescriptionChanged.class)));
//      } else {
//        return false;
//      }
//    return true;
//  }
//
//  private boolean addTagMessages(List<Message<CCMessage>> list, CCMessage message)
//      throws InvalidProtocolBufferException {
//
//    if (message.getPayload().is(ObjectTagged.class)) {
//      list.addAll(on(message.getPayload().unpack(ObjectTagged.class)));
//    } else if (message.getPayload().is(ObjectUntagged.class)) {
//      list.addAll(on(message.getPayload().unpack(ObjectUntagged.class)));
//    } else {
//      return false;
//    }
//    return true;
//  }
//
//  private boolean addProcessTypeMessages(List<Message<CCMessage>> list, CCMessage message) throws InvalidProtocolBufferException {
//    /* Listen on the events defined by the Process Centre */
//    if (message.getPayload().is(ProcessTypeCreated.class)) {
//      list.addAll(on(message.getPayload().unpack(ProcessTypeCreated.class)));
//    } else if (message.getPayload().is(ProcessTypeDefaultBusinessDomainIdUpdated.class)) {
//      list.addAll(on(message.getPayload().unpack(ProcessTypeDefaultBusinessDomainIdUpdated.class)));
//    } else {
//      return false;
//    }
//    return true;
//  }
//
//
//  private boolean addBabelMessages(List<Message<CCMessage>> list, CCMessage message) throws InvalidProtocolBufferException {
//    /* Listen on the events defined by the translation service Babel */
//    if (message.getPayload().is(TranslationCreated.class)) {
//      list.addAll(on(message.getPayload().unpack(TranslationCreated.class)));
//    } else if (message.getPayload().is(TranslationUpdated.class)) {
//      list.addAll(on(message.getPayload().unpack(TranslationUpdated.class)));
//    } else {
//      return false;
//    }
//    return true;
//  }
//
//  private boolean addServiceTaskMessages(List<Message<CCMessage>> list, CCMessage message) throws InvalidProtocolBufferException {
//    if (message.getPayload().is(ServiceTaskCreated.class)) {
//      list.addAll(on(message.getPayload().unpack(ServiceTaskCreated.class),
//          message.getHeader().getCreatedOn()));
//    } else if (message.getPayload().is(ServiceTaskCompleteSucceeded.class)) {
//      list.addAll(on(message.getPayload().unpack(ServiceTaskCompleteSucceeded.class),
//          message.getHeader().getCreatedOn()));
//    } else if (message.getPayload().is(ServiceTaskCompleteFailed.class)) {
//      list.addAll(on(message.getPayload().unpack(ServiceTaskCompleteFailed.class),
//          message.getHeader().getCreatedOn()));
//    } else if (message.getPayload().is(ServiceTaskLocked.class)) {
//      list.addAll(on(message.getPayload().unpack(ServiceTaskLocked.class)));
//    } else if (message.getPayload().is(ServiceTaskUnlocked.class)) {
//      list.addAll(on(message.getPayload().unpack(ServiceTaskUnlocked.class)));
//    } else if (message.getPayload().is(RetryServiceTaskResponse.class)) {
//      list.addAll(on(message.getPayload().unpack(RetryServiceTaskResponse.class)));
//    } else if (message.getPayload().is(ServiceTaskCancelled.class)) {
//      list.addAll(on(message.getPayload().unpack(ServiceTaskCancelled.class)));
//    } else if (message.getPayload().is(SkipServiceTaskResponse.class)) {
//      list.addAll(on(message.getPayload().unpack(SkipServiceTaskResponse.class), message.getHeader().getCreatedOn()));
//    } else {
//      return false;
//    }
//    return true;
//  }
//
//  public boolean addTaskMessage(List<Message<CCMessage>> list, CCMessage message)
//      throws InvalidProtocolBufferException {
//    /* Listen on the events defined by the Task-center */
//
//    if (message.getPayload().is(TaskCreated.class)) {
//      list.addAll(
//          on(message.getPayload().unpack(TaskCreated.class), message.getHeader().getCreatedOn()));
//    } else if (message.getPayload().is(TaskEntered.class)) {
//      list.addAll(on(message.getPayload().unpack(TaskEntered.class)));
//    } else if (message.getPayload().is(TaskCompleted.class)) {
//      list.addAll(
//          on(message.getPayload().unpack(TaskCompleted.class), message.getHeader().getCreatedOn()));
//    } else if (message.getPayload().is(TaskAssigned.class)) {
//      list.addAll(
//          on(message.getPayload().unpack(TaskAssigned.class), message.getHeader().getCreatedOn()));
//    } else if (message.getPayload().is(TaskClaimed.class)) {
//      list.addAll(
//          on(message.getPayload().unpack(TaskClaimed.class), message.getHeader().getCreatedOn()));
//    } else if (message.getPayload().is(TaskUnclaimed.class)) {
//      list.addAll(
//          on(message.getPayload().unpack(TaskUnclaimed.class), message.getHeader().getCreatedOn()));
//    } else if (message.getPayload().is(TaskCancelled.class)) {
//      list.addAll(
//          on(message.getPayload().unpack(TaskCancelled.class), message.getHeader().getCreatedOn()));
//    } else if (message.getPayload().is(TaskDeleted.class)) {
//      list.addAll(
//          on(message.getPayload().unpack(TaskDeleted.class), message.getHeader().getCreatedOn()));
//    } else if (message.getPayload().is(TaskUpdated.class)) {
//      list.addAll(on(message.getPayload().unpack(TaskUpdated.class)));
//    } else if (message.getPayload().is(TaskRestored.class)) {
//      list.addAll(on(message.getPayload().unpack(TaskRestored.class)));
//    } else if (message.getPayload().is(TaskRegistered.class)) {
//      list.addAll(on(message.getPayload().unpack(TaskRegistered.class)));
//    } else if (message.getPayload().is(TaskTypeCreated.class)) {
//      list.addAll(on(message.getPayload().unpack(TaskTypeCreated.class)));
//    } else {
//      return false;
//    }
//    return true;
//  }
//
//  public boolean addCommentsMessage(List<Message<CCMessage>> list, CCMessage message)
//      throws InvalidProtocolBufferException {
//    /* Listen on the events defined by the Task-center */
//
//    if (message.getPayload().is(MessageAdded.class)) {
//      list.addAll(
//          on(message.getPayload().unpack(MessageAdded.class), message.getHeader().getCreatedOn()));
//    } else if (message.getPayload().is(MessageDeleted.class)) {
//      list.addAll(
//          on(message.getPayload().unpack(MessageDeleted.class), message.getHeader().getCreatedOn()));
//    } else if (message.getPayload().is(MessageUpdated.class)) {
//      list.addAll(
//          on(message.getPayload().unpack(MessageUpdated.class), message.getHeader().getCreatedOn()));
//    } else {
//      return false;
//    }
//    return true;
//  }
//
//  public List<Message<CCMessage>> on(ProcessRestored processRestored) {
//    trace(processRestored);
//
//    List<Message<CCMessage>> result = Lists.newArrayList();
//
//    result.add(
//        buildMessage(processRestored.getProcessInstanceId(),
//            UpdateProcess.newBuilder()
//                .setProcessInstanceId(processRestored.getProcessInstanceId())
//                .setProcessTypeId(processRestored.getProcessTypeId())
//                .setProviderId(processRestored.getProviderId())
//                .setDomainKey(processRestored.getDomainKey())
//                .setProcessTypeKey(processRestored.getProcessTypeKey())
//                .setDescription(processRestored.getDescription())
//                .setCreatedOn(processRestored.getCreatedOn())
//                .addAllAssociatedPortfolioItemIds(
//                    processRestored.getAssociatedPortfolioItemIdsList())
//                .setResponsibleOrganisationId(processRestored.getResponsibleOrganisationId())
//                .setResponsibleUserId(processRestored.getResponsibleUserId())
//                .setBusinessDomainId(processRestored.getBusinessDomainId())
//                .setUserId(processRestored.getUserId())
//                .setOnBehalfOfUserId(processRestored.getOnBehalfOfUserId())
//                .build())
//    );
//
//    addMessagesLinkProcess(processRestored.getProcessInstanceId(),
//        processRestored.getParentProcessInstanceId(), result);
//
//    if (processRestored.getProcessVariablesMap() != null
//        && !processRestored.getProcessVariablesMap().isEmpty()) {
//
//      result.add(
//          buildMessage(processRestored.getProcessInstanceId(),
//              UpdateProcessVariables.newBuilder()
//                  .setProcessInstanceId(processRestored.getProcessInstanceId())
//                  .addAllOrClearProcessVariables(processRestored.getProcessVariablesMap())
//                  .build())
//      );
//    }
//
//    if (!StringUtils.isEmpty(processRestored.getBusinessStatus())) {
//      result.add(buildMessage(processRestored.getProcessInstanceId(),
//          UpdateProcessBusinessStatus.newBuilder()
//              .setProcessInstanceId(processRestored.getProcessInstanceId())
//              .setBusinessStatus(processRestored.getBusinessStatus())
//              .build()));
//
//    }
//
//    // fetch and update process type information
//    final Map<String, String> context = context(
//        processRestored.getProviderId(),
//        processRestored.getDomainKey(),
//        processRestored.getProcessTypeKey()
//    );
//    if (isValidContext(context)) {
//      addMessagesProcessTypeConf4ProcessInstance(processRestored.getProcessInstanceId(),
//          processRestored.getProcessTypeId(), context, result);
//    }
//
//    // create process detail container
//    result.add(buildMessage(
//        processRestored.getProcessInstanceId(),
//        CreateOrUpdateContainer.newBuilder()
//            .setContainerId(processRestored.getProcessInstanceId())
//            .putAllVariables(ProcessDetailHelper.getProcessDetailVariables(processRestored))
//            .putAllConfigMetadata(ProcessDetailHelper.getProcessDetailContext(processRestored))
//            .build()
//    ));
//
//    if (processRestored.hasPausedOn()) {
//      result.add(buildMessage(processRestored.getProcessInstanceId(),
//          PauseProcess.newBuilder()
//              .setProcessInstanceId(processRestored.getProcessInstanceId())
//              .setPausedOn(processRestored.getPausedOn())
//              //.setReason(message.getReason())
//              .setUserId(processRestored.getUserId())
//              .setOnBehalfOfUserId(processRestored.getOnBehalfOfUserId())
//              .build()));
//    }
//
//    if (processRestored.hasRestartedOn()) {
//      result.add(buildMessage(processRestored.getProcessInstanceId(),
//          StartProcess.newBuilder()
//              .setProcessInstanceId(processRestored.getProcessInstanceId())
//              .setStartedOn(processRestored.getRestartedOn())
////            .setReason(message.getReason())
//              .setUserId(processRestored.getUserId())
//              .setOnBehalfOfUserId(processRestored.getOnBehalfOfUserId())
//              .build()));
//    }
//
//    if (processRestored.hasCompletedOn()) {
//      result.add(buildMessage(processRestored.getProcessInstanceId(),
//          CompleteProcess.newBuilder()
//              .setProcessInstanceId(processRestored.getProcessInstanceId())
//              .setCompletedOn(processRestored.getCompletedOn())
////              .setReason(message.getReason())
//              .setUserId(processRestored.getUserId())
//              .setOnBehalfOfUserId(processRestored.getOnBehalfOfUserId())
//              .build()));
//
//      result.add(buildMessage(processRestored.getProcessInstanceId(),
//          CompleteContainer.newBuilder()
//              .setContainerId(processRestored.getProcessInstanceId())
//              .build())
//      );
//
//    }
//
//    if (processRestored.hasCancelledOn()) {
//      result
//          .add(
//              buildMessage(processRestored.getProcessInstanceId(),
//                  CancelProcess
//                      .newBuilder()
//                      .setProcessInstanceId(processRestored.getProcessInstanceId())
//                      .setCancelledOn(processRestored.getCancelledOn())
//                      .setUserId(processRestored.getUserId())
//                      .setOnBehalfOfUserId(processRestored.getOnBehalfOfUserId())
//                      .build()));
//      result.add(buildMessage(processRestored.getProcessInstanceId(),
//          CompleteContainer.newBuilder()
//              .setContainerId(processRestored.getProcessInstanceId())
//              .build())
//      );
//    }
//
//    if (processRestored.hasCompletedOn() || processRestored.hasCancelledOn()) {
//      // save snapshot of the card layout configuration for the completed process
//      addMsgUpdateProcessResultCardConfiguration(processRestored.getProcessInstanceId(), context,
//          result);
//    }
//
//    return result;
//  }
//
//  public List<Message<CCMessage>> on(ProcessBusinessStatusChanged message) {
//    trace(message);
//
//    return Collections.singletonList(buildMessage(message.getProcessInstanceId(),
//        UpdateProcessBusinessStatus.newBuilder()
//            .setProcessInstanceId(message.getProcessInstanceId())
//            .setBusinessStatus(message.getBusinessStatus())
//            .build()));
//  }
//
//  public List<Message<CCMessage>> on(ObjectTagged message) {
//    trace(message);
//
//    if (message == null) {
//      return emptyList();
//    }
//
//    switch (message.getObjectType()) {
//      case PROCESS -> {
//        asyncListener.handle(tagEventHandler::handle, message);
//        return emptyList();
//      }
//      case PORTFOLIOITEM -> {
//        String portfolioId = message.getObjectId();
//        return updatePortfolioTagMessages(parentId, message.getTagsMap(), null, portfolioId);
//      }
//      default -> {
//        // We don't react to other object types.
//      }
//    }
//
//    return emptyList();
//  }
//
//  @NotNull
//  List<Message<CCMessage>> updatePortfolioTagMessages(
//      Map<String, TagValueList> tagsAdded,
//      Map<String, TagValueList> tagsDeleted, String portfolioId) {
//    Tags tags = tagService.updatedPortfolioTags(portfolioId, tagsAdded,
//        tagsDeleted);
//
//    // save in DB
//    tagService.savePortfolioTags(portfolioId, tags);
//
//    List<Message<CCMessage>> result = Lists.newArrayList();
//    Map<String, TagValueList> tagMap = TagConverters.toTagService(tags);
//
//    processDocumentCustomRepository
//        .findProcessIdsByPortfolioId(portfolioId)
//        .forEach(id -> result
//            .add(buildMessage((id),
//                UpdatePortfolioTag
//                    .newBuilder()
//                    .setProcessInstanceId(id)
//                    .setPortfolioItemId(portfolioId)
//                    .addAllOrClearTags(tagMap)
//                    .build())));
//
//    return result;
//  }
//
//  public List<Message<CCMessage>> on(ObjectUntagged message) {
//    trace(message);
//
//    if (message == null) {
//      return emptyList();
//    }
//    switch (message.getObjectType()) {
//      case PROCESS -> {
//        asyncListener.handle(tagEventHandler::handle, message);
//        return emptyList();
//      }
//      case PORTFOLIOITEM -> {
//        String portfolioId = message.getObjectId();
//        return updatePortfolioTagMessages(null, message.getTagsMap(), portfolioId);
//      }
//      default -> {
//        // We don't react to other object types.
//      }
//    }
//
//    return emptyList();
//  }
//
//  /**
//   * Some messages don't need to be stored, typically we don't store the ParticipantUpdated
//   * messages. We also don't store the TranslationUpdated message.
//   */
//  protected boolean shouldStoreMessage(CCMessage event) {
//    return !event.getPayload().is(ParticipantUpdated.class) &&
//        !event.getPayload().is(TranslationUpdated.class) &&
//        !event.getPayload().is(TranslationCreated.class);
//  }
//
//  /* Listen on the events defined by the Process Centre */
//
//  public List<Message<CCMessage>> on(ProcessBusinessDomainIdUpdated message) {
//    trace(message);
//
//    return Collections.singletonList(buildMessage(message.getProcessInstanceId(),
//        UpdateProcessBusinessDomainId.newBuilder()
//            .setProcessInstanceId(message.getProcessInstanceId())
//            .setBusinessDomainId(message.getBusinessDomainId())
//            .build()));
//
//  }
//
//  public List<Message<CCMessage>> on(ProcessTypeCreated message) {
//    trace(message);
//
//    List<Message<CCMessage>> result = Lists.newArrayList();
//
//    result.add(
//        buildMessage(message.getProcessTypeId(),
//            UpdateProcessType.newBuilder()
//                .setProcessTypeId(message.getProcessTypeId())
//                .setProviderId(message.getProviderId())
//                .setName(message.getName())
//                .putAllConfigMetadata(message.getConfigMetadataMap())
//                .setDefaultBusinessDomainId(message.getDefaultBusinessDomainId())
//                .build())
//    );
//
//    List<String> newTaxonomyPaths = message.getTaxonomyPathList();
//
//    if (!CollectionUtils.isEmpty(newTaxonomyPaths)) {
//      result.add(buildMessage(
//              message.getProcessTypeId(),
//              UpdateProcessTaxonomy.newBuilder()
//                  .setProcessTypeId(message.getProcessTypeId())
//                  .addAllNewTaxonomyPaths(newTaxonomyPaths)
//                  .build()
//          )
//      );
//    }
//
//    return result;
//  }
//
//  public List<Message<CCMessage>> on(ProcessTypeDefaultBusinessDomainIdUpdated message) {
//    trace(message);
//
//    return Collections.singletonList(
//        buildMessage(message.getProcessTypeId(),
//            UpdateDefaultBusinessDomainId.newBuilder()
//                .setProcessTypeId(message.getProcessTypeId())
//                .setDefaultBusinessDomainId(message.getDefaultBusinessDomainId())
//                .build())
//    );
//
//  }
//
//  public List<Message<CCMessage>> on(ProcessCreated message, Timestamp messageDate) {
//    trace(message);
//
//    List<Message<CCMessage>> result = Lists.newArrayList();
//
//    result.add(
//        buildMessage(message.getProcessInstanceId(),
//            UpdateProcess.newBuilder()
//                .setProcessInstanceId(message.getProcessInstanceId())
//                .setProcessTypeId(message.getProcessTypeId())
//                .setProviderId(message.getProviderId())
//                .setDomainKey(message.getDomainKey())
//                .setProcessTypeKey(message.getProcessTypeKey())
//                .setDescription(message.getDescription())
//                .setCreatedOn(orElse(message.getCreatedOn(), messageDate))
//                .addAllAssociatedPortfolioItemIds(message.getAssociatedPortfolioItemIdsList())
//                .setResponsibleOrganisationId(message.getResponsibleOrganisationId())
//                .setBusinessDomainId(message.getBusinessDomainId())
//                .setUserId(message.getUserId())
//                .setOnBehalfOfUserId(message.getOnBehalfOfUserId())
//                .putAllProcessVariables(message.getProcessVariablesMap())
//                .setBusinessStatus(message.getBusinessStatus())
//                .build())
//    );
//
//    addMessagesLinkProcess(message.getProcessInstanceId(), message.getParentProcessInstanceId(),
//        result);
//
//    // fetch and update process type information
//    final Map<String, String> context = context(
//        message.getProviderId(),
//        message.getDomainKey(),
//        message.getProcessTypeKey()
//    );
//    if (isValidContext(context)) {
//      addMessagesProcessTypeConf4ProcessInstance(message.getProcessInstanceId(),
//          message.getProcessTypeId(), context, result);
//    }
//
//    // create process detail container
//    result.add(buildMessage(
//        message.getProcessInstanceId(),
//        CreateOrUpdateContainer.newBuilder()
//            .setContainerId(message.getProcessInstanceId())
//            .putAllVariables(ProcessDetailHelper.getProcessDetailVariables(message))
//            .putAllConfigMetadata(ProcessDetailHelper.getProcessDetailContext(message))
//            .build()
//    ));
//
//    return result;
//  }
//
//  private void addMessagesLinkProcess(String processInstanceId, String parentProcessInstanceId,
//      List<Message<CCMessage>> result) {
//    if (StringUtils.isNotEmpty(parentProcessInstanceId)) {
//      // update the root aggregate 1 (process 1 is the parent of process 2)
//
//      result.add(
//          buildMessage(parentProcessInstanceId,
//              LinkProcess.newBuilder()
//                  .setProcessInstance1Id(parentProcessInstanceId)
//                  .setProcessInstance2Id(processInstanceId)
//                  .setRelationship(ProcessRelationship.PARENT.name())
//                  .build())
//      );
//
//      // update the root aggregate 2 (process 1 is the child of process 2)
//      result.add(
//          buildMessage(processInstanceId,
//              LinkProcess.newBuilder()
//                  .setProcessInstance1Id(processInstanceId)
//                  .setProcessInstance2Id(parentProcessInstanceId)
//                  .setRelationship(ProcessRelationship.CHILD.name())
//                  .build())
//      );
//    }
//  }
//
//  private void addMessagesProcessTypeConf4ProcessInstance(String processInstanceId,
//      String processTypeId,
//      final Map<String, String> context, List<Message<CCMessage>> result) {
//    try {
//      configService
//          .fetchProcessType(context)
//          .ifPresent(processType -> {
//
//            final UpdateProcessTypeConfiguration.Builder updateProcessTypeConfiguration =
//                UpdateProcessTypeConfiguration
//                    .newBuilder()
//                    .setProcessInstanceId(processInstanceId)
//                    .setName(eu.europa.ec.processcentre.babel.model.BabelText.convert(
//                        processType.getName()))
//                    .setTitleTemplate(eu.europa.ec.processcentre.babel.model.BabelText.convert(
//                        processType.getTitleTemplate()));
//
//            if (isNotBlank(processType.getSecundaTask())) {
//              updateProcessTypeConfiguration.setSecundaTask(processType.getSecundaTask());
//            } else {
//              // Secunda task can be null
//              updateProcessTypeConfiguration.clearSecundaTask();
//            }
//
//            if (isNotBlank(processType.getDefaultBusinessDomainId())) {
//              updateProcessTypeConfiguration.setDefaultBusinessDomainId(
//                  processType.getDefaultBusinessDomainId());
//            }
//
//            updateProcessTypeConfiguration.setHideSubprocessOfTheSameType(
//                Optional.ofNullable(processType.getHideSubprocessesOfTheSameType()).orElse(false));
//
//            List<eu.europa.ec.cc.processcentre.proto.AccessRight> accessRights = AccessRightsConverter.domainToProto(
//                processType.getAccessRights());
//
//            if (CollectionUtils.isNotEmpty(accessRights)) {
//              // send the command to update the access rights at the process type level
//              result.add(buildMessage(
//                  processTypeId,
//                  eu.europa.ec.cc.processcentre.proto.UpdateProcessSecurity.newBuilder()
//                      .setProcessTypeId(processTypeId)
//                      .addAllOrClearAccessRights(accessRights)
//                      .build()
//              ));
//
//              // update the access rights at the process instance level
//              updateProcessTypeConfiguration.addAllOrClearAccessRights(accessRights);
//            }
//
//            result.add(buildMessage(processInstanceId,
//                updateProcessTypeConfiguration.build()
//            ));
//
//            // Update processType in Oracle DB for a hierarchy
//              /* PROCESS-1123, re-applied
//              UpdateProcessType.Builder updateProcessTypeBuilder =
//                  UpdateProcessType
//                      .newBuilder()
//                      .setProcessTypeId(processTypeId)
//                      .setProviderId(context.get("providerId"))
//                      .setName(convert(processType.getName()))
//                      .putAllConfigMetadata(context);
//
//              ofNullable(processType.getDefaultBusinessDomainId()).ifPresent(o -> updateProcessTypeBuilder
//                  .setDefaultBusinessDomainId(o));
//
//              result.add(buildMessage(processTypeId,
//                  updateProcessTypeBuilder.build()
//                ));
//              */
//
//          });
//    } catch (ConfigException configException) {
//      LOG.error("Cannot fetch process type configuration from the repository; ignoring...",
//          configException);
//    }
//  }
//
//  private void addMsgUpdateProcessResultCardConfiguration(String processInstanceId, Map<String, String> context,
//      List<Message<CCMessage>> result) {
//    if (!isValidContext(context)) {
//      LOG.warn("Invalid context for the processId: {}", processInstanceId);
//      return;
//    }
//    try {
//      configService
//          .fetchConfig(ConfigType.PROCESS_RESULT, context, JSON_MAPPER::writeValueAsString)
//          .ifPresent(processResultCardConfiguration -> result.add(buildMessage(processInstanceId,
//              UpdateProcessResultCardConfiguration
//                  .newBuilder()
//                  .setProcessInstanceId(processInstanceId)
//                  .setResultCardLayout(processResultCardConfiguration)
//                  .build()
//          )));
//    } catch (ConfigException configException) {
//      LOG.error("Cannot fetch process result card configuration from the repository; ignoring...",
//          configException
//      );
//    }
//  }
//
//  public List<Message<CCMessage>> on(ProcessRunningStatusChanged message, Timestamp messageDate) {
//    trace(message);
//
//    List<Message<CCMessage>> result = Lists.newArrayList();
//
//    switch (message.getNewStatus()) {
//      case ONGOING:
//        result.add(buildMessage(message.getProcessInstanceId(),
//            StartProcess.newBuilder()
//                .setProcessInstanceId(message.getProcessInstanceId())
//                .setStartedOn(orElse(message.getChangedOn(), messageDate))
//                .setReason(message.getReason())
//                .setUserId(message.getUserId())
//                .setOnBehalfOfUserId(message.getOnBehalfOfUserId())
//                .build())
//        );
//        result.add(buildMessage(message.getProcessInstanceId(),
//            ReopenContainer.newBuilder()
//                .setContainerId(message.getProcessInstanceId())
//                .build())
//        );
//        return result;
//      case PAUSED:
//        result.add(buildMessage(message.getProcessInstanceId(),
//            PauseProcess.newBuilder()
//                .setProcessInstanceId(message.getProcessInstanceId())
//                .setPausedOn(orElse(message.getChangedOn(), messageDate))
//                .setReason(message.getReason())
//                .setUserId(message.getUserId())
//                .setOnBehalfOfUserId(message.getOnBehalfOfUserId())
//                .build())
//        );
//        return result;
//      case ENDED:
//        result.add(
//            buildMessage(message.getProcessInstanceId(),
//                CompleteProcess.newBuilder()
//                    .setProcessInstanceId(message.getProcessInstanceId())
//                    .setCompletedOn(orElse(message.getChangedOn(), messageDate))
//                    .setReason(message.getReason())
//                    .setUserId(message.getUserId())
//                    .setOnBehalfOfUserId(message.getOnBehalfOfUserId())
//                    .build())
//        );
//        result.add(buildMessage(message.getProcessInstanceId(),
//            CompleteContainer.newBuilder()
//                .setContainerId(message.getProcessInstanceId())
//                .build())
//        );
//
//        final Map<String, String> context = context(
//            message.getProviderId(),
//            message.getDomainKey(),
//            message.getProcessTypeKey()
//        );
//        // save snapshot of the card layout configuration for the completed process
//        addMsgUpdateProcessResultCardConfiguration(message.getProcessInstanceId(), context, result);
//        return result;
//      case UNRECOGNIZED:
//        break;
//    }
//    return emptyList();
//  }
//
//  Optional<String> findProcessTypeId(String processInstanceId) {
//    Optional<ProcessEntity> processEntity = processRepository.findById(processInstanceId);
//    if (processEntity.isPresent()
//        && processEntity.get().getType() != null
//        && StringUtils.isNotEmpty(processEntity.get().getType().getId())
//    ) {
//      return Optional.of(processEntity.get().getType().getId());
//    }
//    return Optional.empty();
//  }
//
//
//  public List<Message<CCMessage>> on(ProcessStateChanged message, Timestamp messageDate) {
//    trace(message);
//
//    List<Message<CCMessage>> result = Lists.newArrayList();
//
//    result.add(
//        buildMessage(message.getProcessInstanceId(),
//            RegisterProcessStateChange.newBuilder()
//                .setProcessInstanceId(message.getProcessInstanceId())
//                .setState(message.getState())
//                .setChange(RegisterProcessStateChange.Change.valueOf(message.getChange().name()))
//                .setChangedOn(orElse(message.getChangedOn(), messageDate))
//                .build())
//    );
//
//    // save the link between the process type and process state
//    Optional<String> processTypeId = findProcessTypeId(message.getProcessInstanceId());
//    processTypeId.ifPresent(
//        o ->
//            result.add(
//                buildMessage(message.getProcessInstanceId(),
//                    LinkProcessTypeProcessState.newBuilder()
//                        .setProcessTypeId(o)
//                        .setProcessState(message.getState())
//                        .build()))
//    );
//
//    return result;
//  }
//
//
//  public List<Message<CCMessage>> on(ProcessTaxonomyUpdated message) {
//    trace(message);
//
//    Collection<String> newTaxonomyPaths = Sanitizable.sanitize(message.getNewTaxonomyPathList());
//
//    taxonomyService.updateTaxonomyPaths(message.getProcessTypeId(), newTaxonomyPaths);
//
//    List<Message<CCMessage>> result = Lists.newArrayList();
//
//    @NotNull
//    Collection<String> processIds =
//        processDocumentCustomRepository.findProcessIdsByProcessTypeId(message.getProcessTypeId());
//
//    processIds
//        .stream()
//        .forEach(id -> result
//            .add(buildMessage(id, UpdateProcessTaxonomyPaths
//                .newBuilder()
//                .setProcessInstanceId(id)
//                .addAllTaxonomyPaths(newTaxonomyPaths)
//                .build())));
//
//    return result;
//  }
//
//  private Message<CCMessage> buildMessage(String key, com.google.protobuf.Message payload) {
//    CCMessage ccMessage = CCMessageFactory.newMessage(payload);
//    return MessageBuilder
//        .withPayload(ccMessage)
//        .setHeader(KafkaHeaders.KEY, key)
//        .setHeader(KafkaHeaders.TOPIC, replyTopic)
//        .build();
//  }
//
//
//  public List<Message<CCMessage>> on(ProcessSecurityUpdated message) {
//    trace(message);
//
//    return Collections.singletonList(
//        buildMessage(message.getProcessTypeId(),
//            UpdateProcessSecurity.newBuilder()
//                .setProcessTypeId(message.getProcessTypeId())
//                .setSecundaTask(message.getSecundaTask())
//                .build())
//    );
//  }
//
//  public List<Message<CCMessage>> on(ProcessSpecificAttributeUpdated message) {
//    trace(message);
//
//    List<Message<CCMessage>> result = Lists.newArrayList();
//
//    result.add(
//        buildMessage(message.getProcessId(),
//            UpdateProcessSpecificAttribute.newBuilder()
//                .setProcessInstanceId(message.getProcessId())
//                .setName(message.getName())
//                .setValue(message.getValue())
//                .build())
//    );
//
//    result.add(
//        buildMessage(message.getProcessId(),
//            UpdateVariables.newBuilder()
//                .setContainerId(message.getProcessId())
//                .putVariables(message.getName(), message.getValue())
//                .build())
//    );
//
//    return result;
//  }
//
//  public List<Message<CCMessage>> on(ProcessSpecificAttributeRemoved message) {
//    trace(message);
//
//    List<Message<CCMessage>> result = Lists.newArrayList();
//
//    result.add(
//        buildMessage(message.getProcessId(),
//            RemoveProcessSpecificAttribute.newBuilder()
//                .setProcessInstanceId(message.getProcessId())
//                .setName(message.getName())
//                .build())
//    );
//
//    // update process detail container
//    result.add(
//        buildMessage(message.getProcessId(),
//            UpdateVariables.newBuilder()
//                .setContainerId(message.getProcessId())
//                .putVariables(message.getName(), VariableValue.newBuilder()
//                    .setDeleted(true)
//                    .build())
//                .build())
//    );
//
//    return result;
//  }
//
//  public List<Message<CCMessage>> on(ProcessVariableUpdated message) {
//    trace(message);
//
//    Map<String, VariableValue> variables = Maps.newHashMap();
//    variables.put(message.getName(), message.getValue());
//
//    List<Message<CCMessage>> result = Lists.newArrayList();
//
//    result.add(
//        buildMessage(message.getProcessId(),
//            UpdateProcessVariables.newBuilder()
//                .setProcessInstanceId(message.getProcessId())
//                .putAllProcessVariables(variables)
//                .build())
//    );
//
//    // update process detail container
//    result.add(
//        buildMessage(message.getProcessId(),
//            UpdateVariables.newBuilder()
//                .setContainerId(message.getProcessId())
//                .putAllVariables(variables)
//                .build())
//    );
//
//    return result;
//  }
//
//
//  public List<Message<CCMessage>> on(ProcessVariablesUpdated message) {
//    trace(message);
//
//    List<Message<CCMessage>> result = Lists.newArrayList();
//
//    result.add(
//        buildMessage(message.getProcessId(),
//            UpdateProcessVariables.newBuilder()
//                .setProcessInstanceId(message.getProcessId())
//                .putAllProcessVariables(message.getProcessVariablesMap())
//                .build())
//    );
//
//    // update process detail container
//    result.add(
//        buildMessage(message.getProcessId(),
//            UpdateVariables.newBuilder()
//                .setContainerId(message.getProcessId())
//                .putAllVariables(message.getProcessVariablesMap())
//                .build())
//    );
//
//    return result;
//  }
//
//
//  public List<Message<CCMessage>> on(ProcessLinkAdded message) {
//    trace(message);
//
//    List<Message<CCMessage>> result = Lists.newArrayList();
//
//    ProcessRelationship relationship = ProcessRelationship.valueOf(
//        message.getRelationship().name());
//
//    // update the root aggregate 1
//    result.add(
//        buildMessage(message.getProcessInstance1Id(),
//            LinkProcess.newBuilder()
//                .setProcessInstance1Id(message.getProcessInstance1Id())
//                .setProcessInstance2Id(message.getProcessInstance2Id())
//                .setRelationship(relationship.name())
//                .build())
//    );
//
//    // update the root aggregate 2
//    result.add(
//        buildMessage(message.getProcessInstance2Id(),
//            LinkProcess.newBuilder()
//                .setProcessInstance1Id(message.getProcessInstance2Id())
//                .setProcessInstance2Id(message.getProcessInstance1Id())
//                .setRelationship(relationship.opposite().name())
//                .build())
//    );
//
//    return result;
//  }
//
//  public List<Message<CCMessage>> on(ProcessLinkRemoved message) {
//    trace(message);
//
//    List<Message<CCMessage>> result = Lists.newArrayList();
//
//    ProcessRelationship relationship = ProcessRelationship.valueOf(
//        message.getRelationship().name());
//
//    // update the root aggregate 1
//    result.add(
//        buildMessage(message.getProcessInstance1Id(),
//            UnlinkProcess.newBuilder()
//                .setProcessInstance1Id(message.getProcessInstance1Id())
//                .setProcessInstance2Id(message.getProcessInstance2Id())
//                .setRelationship(relationship.name())
//                .build())
//    );
//    // update the root aggregate 2
//    result.add(
//        buildMessage(message.getProcessInstance2Id(),
//            UnlinkProcess.newBuilder()
//                .setProcessInstance1Id(message.getProcessInstance2Id())
//                .setProcessInstance2Id(message.getProcessInstance1Id())
//                .setRelationship(relationship.opposite().name())
//                .build())
//    );
//    return result;
//  }
//
//  public List<Message<CCMessage>> on(ProcessResponsibleOrganisationChanged message) {
//    trace(message);
//
//    if (StringUtils.isNotEmpty(message.getResponsibleOrganisationId())) {
//      return Collections.singletonList(
//          buildMessage(message.getProcessInstanceId(),
//              UpdateProcessResponsibleOrganisation.newBuilder()
//                  .setProcessInstanceId(message.getProcessInstanceId())
//                  .setResponsibleOrganisationId(message.getResponsibleOrganisationId())
//                  .build())
//      );
//    }
//
//    return emptyList();
//  }
//
//  public List<Message<CCMessage>> on(ProcessResponsibleUserChanged message) {
//    trace(message);
//
//    if (StringUtils.isNotEmpty(message.getResponsibleUserId())) {
//      return Collections.singletonList(
//          buildMessage(message.getProcessInstanceId(),
//              UpdateProcessResponsibleUser.newBuilder()
//                  .setProcessInstanceId(message.getProcessInstanceId())
//                  .setResponsibleUserId(message.getResponsibleUserId())
//                  .build())
//      );
//    }
//
//    return emptyList();
//  }
//
//  public List<Message<CCMessage>> on(ProcessDescriptionChanged message) {
//    trace(message);
//
//    return Collections.singletonList(
//        buildMessage(message.getProcessInstanceId(),
//            UpdateProcessDescription.newBuilder()
//                .setProcessInstanceId(message.getProcessInstanceId())
//                .setDescription(message.getDescription())
//                .build())
//    );
//  }
//
//  public List<Message<CCMessage>> on(ProcessAssociatedPortfolioItemAdded message) {
//    trace(message);
//
//    return Collections.singletonList(
//        buildMessage(message.getProcessInstanceId(),
//            AssociateProcessPortfolioItem.newBuilder()
//                .setProcessInstanceId(message.getProcessInstanceId())
//                .addAllPortfolioItemIds(message.getPortfolioItemIdsList())
//                .build())
//    );
//  }
//
//  public List<Message<CCMessage>> on(ProcessAssociatedPortfolioItemRemoved message) {
//    trace(message);
//
//    return Collections.singletonList(
//        buildMessage(message.getProcessInstanceId(),
//            DisassociateProcessPortfolioItem.newBuilder()
//                .setProcessInstanceId(message.getProcessInstanceId())
//                .addAllPortfolioItemIds(message.getPortfolioItemIdsList())
//                .build())
//    );
//  }
//
//  public List<Message<CCMessage>> on(ProcessCancelled message) {
//    trace(message);
//
//    List<Message<CCMessage>> result = Lists.newArrayList();
//
//    result.add(buildMessage(message.getProcessInstanceId(),
//        CancelProcess.newBuilder()
//            .setProcessInstanceId(message.getProcessInstanceId())
//            .setCancelledOn(message.getCancelledOn())
//            .setUserId(message.getUserId())
//            .setOnBehalfOfUserId(message.getOnBehalfOfUserId())
//            .build())
//    );
//
//    result.add(buildMessage(message.getProcessInstanceId(),
//        CompleteContainer.newBuilder()
//            .setContainerId(message.getProcessInstanceId())
//            .build())
//    );
//
//    final Map<String, String> context = context(
//        message.getProviderId(),
//        message.getDomainKey(),
//        message.getProcessTypeKey()
//    );
//    // save snapshot of the card layout configuration for the completed process
//    addMsgUpdateProcessResultCardConfiguration(message.getProcessInstanceId(), context, result);
//
//    return result;
//
//  }
//
//
//  public List<Message<CCMessage>> on(ProcessDeleted message) {
//    trace(message);
//
//    List<Message<CCMessage>> result = Lists.newArrayList();
//
//    result.add(
//        buildMessage(message.getProcessInstanceId(),
//            DeleteProcess.newBuilder()
//                .setProcessInstanceId(message.getProcessInstanceId())
//                .setDeletedOn(message.getDeletedOn())
//                .setUserId(message.getUserId())
//                .setOnBehalfOfUserId(message.getOnBehalfOfUserId())
//                .build())
//    );
//
//    result.add(
//        buildMessage(message.getProcessInstanceId(),
//            DeleteContainer.newBuilder()
//                .setContainerId(message.getProcessInstanceId())
//                .build())
//    );
//
//    return result;
//  }
//
//  /* Listen on the events defined by the taxonomy service */
//
//  private static String extractProcessType(String refURI) {
//    if (StringUtils.isNotEmpty(refURI) && refURI.startsWith(TAXONOMY_REF_URI_PREFIX)) {
//      return refURI.substring(TAXONOMY_REF_URI_PREFIX.length());
//    }
//    return null;
//  }
//
//  public List<Message<CCMessage>> on(BranchUpdated message) {
//    trace(message);
//
//    List<Message<CCMessage>> result = Lists.newArrayList();
//
//    // retrieve the process types
//    List<String> processTypeIds = Lists.newArrayList();
//    if (CollectionUtils.isNotEmpty(message.getRefUrisList())) {
//      for (String refURI : message.getRefUrisList()) {
//        String processTypeId = extractProcessType(refURI);
//        if (StringUtils.isNotEmpty(processTypeId)) {
//          processTypeIds.add(processTypeId);
//        }
//      }
//    }
//    tracing.tag("processTypeIds", processTypeIds);
//
//    // update db and processes for all process types
//    List<String> newTaxonomyPaths =
//        Sanitizable.sanitize(Lists.newArrayList(message.getFullPath()));
//
//    for (String processTypeId : processTypeIds) {
//      tracing.tag("processTypeId", processTypeId);
//      if (processTypeId.contains("/")) {
//        // it means the process type id is incorrect. Probably the taxonomyPath was copied in the
//        // refURI
//        tracing
//            .error(new IllegalArgumentException(
//                "the process type provided in the refURI(s) is invalid"));
//      } else {
//        List<String> addedTaxonomyPaths = new ArrayList<>(newTaxonomyPaths);
//        List<String> oldTaxonomyPaths = taxonomyService.getTaxonomyPaths(processTypeId);
//        addedTaxonomyPaths.removeAll(oldTaxonomyPaths);
//
//        if (!addedTaxonomyPaths.isEmpty()) {
//          taxonomyService.addTaxonomyPaths(processTypeId, addedTaxonomyPaths);
//
//          // UpdateProcessTaxonomyPaths requires the complete list of paths
//          List<String> allTaxonomyPaths = new ArrayList<>(addedTaxonomyPaths);
//          allTaxonomyPaths.addAll(oldTaxonomyPaths);
//
//          @NotNull
//          Collection<String> processIds =
//              processDocumentCustomRepository.findProcessIdsByProcessTypeId(processTypeId);
//
//          processIds
//              .stream()
//              .forEach(id -> result
//                  .add(buildMessage(id, UpdateProcessTaxonomyPaths
//                      .newBuilder()
//                      .setProcessInstanceId(id)
//                      .addAllTaxonomyPaths(allTaxonomyPaths)
//                      .build())));
//        }
//      }
//    }
//
//    return result;
//  }
//
//  public List<Message<CCMessage>> on(BranchDeleted message) {
//    trace(message);
//
//    List<Message<CCMessage>> result = Lists.newArrayList();
//
//    // retrieve the process types
//    List<String> processTypeIds = Lists.newArrayList();
//    // uncomment when the refUri will be included in the BranchDeleted event
//
//    if (CollectionUtils.isNotEmpty(message.getRefUrisList())) {
//      for (String refURI : message.getRefUrisList()) {
//        String processTypeId = extractProcessType(refURI);
//        if (StringUtils.isNotEmpty(processTypeId)) {
//          processTypeIds.add(processTypeId);
//        }
//      }
//    }
//
//    tracing.tag("processTypeIds", processTypeIds);
//
//    // update db and processes for all process types
//    List<String> newTaxonomyPaths =
//        Sanitizable.sanitize(Lists.newArrayList(message.getFullPath()));
//
//    for (String processTypeId : processTypeIds) {
//      tracing.tag("processTypeId", processTypeId);
//      if (processTypeId.contains("/")) {
//        // it means the process type id is incorrect. Probably the taxonomyPath was copied in the
//        // refURI
//        tracing
//            .error(new IllegalArgumentException(
//                "the process type provided in the refURI(s) is invalid"));
//      } else {
//        List<String> removedTaxonomyPaths = new ArrayList<>(newTaxonomyPaths);
//        List<String> oldTaxonomyPaths = taxonomyService.getTaxonomyPaths(processTypeId);
//        removedTaxonomyPaths.retainAll(oldTaxonomyPaths);
//
//        if (!removedTaxonomyPaths.isEmpty()) {
//          taxonomyService.removeTaxonomyPaths(processTypeId, removedTaxonomyPaths);
//
//          // UpdateProcessTaxonomyPaths requires the complete list of paths
//          List<String> allTaxonomyPaths = new ArrayList<>(oldTaxonomyPaths);
//          allTaxonomyPaths.removeAll(removedTaxonomyPaths);
//
//          @NotNull
//          Collection<String> processIds =
//              processDocumentCustomRepository.findProcessIdsByProcessTypeId(processTypeId);
//
//          processIds
//              .stream()
//              .forEach(id -> result
//                  .add(buildMessage(id, UpdateProcessTaxonomyPaths
//                      .newBuilder()
//                      .setProcessInstanceId(id)
//                      .addAllTaxonomyPaths(allTaxonomyPaths)
//                      .build())));
//        }
//      }
//    }
//
//    return result;
//  }
//
//
//  /* Listen on the events defined by the Task-center */
//
//  public List<Message<CCMessage>> on(TaskTypeCreated message) {
//    List<Message<CCMessage>> result = Lists.newArrayList();
//
//    trace(message);
//    tracing.tag("processTypeIds", message.getProcessTypeIdList());
//
//    if (CollectionUtils.isNotEmpty(message.getProcessTypeIdList())) {
//      for (String processTypeId : message.getProcessTypeIdList()) {
//        if (StringUtils.isNotEmpty(processTypeId)) {
//          result.add(
//              buildMessage(processTypeId,
//                  LinkProcessTypeTaskType.newBuilder()
//                      .setProcessTypeId(processTypeId)
//                      .setTaskTypeId(message.getTaskTypeId())
//                      .setTaskTypeName(message.getName())
//                      .build())
//          );
//        }
//      }
//    }
//
//    return result;
//  }
//
//  private List<Message<CCMessage>> createTask(
//      String processInstanceId,
//      String taskInstanceId,
//      String providerId,
//      String taskTypeId,
//      Timestamp timestamp,
//      ProtocolStringList candidateUserIdList,
//      ProtocolStringList candidateGroupIdList,
//      String claimerUserId,
//      String onBehalfOfClaimerUserId,
//      BabelText title,
//      BabelText description
//  ) {
//
//    if (StringUtils.isNotEmpty(processInstanceId)) {
//
//      List<Message<CCMessage>> commands = Lists.newArrayList();
//
//      commands.add(
//          buildMessage(processInstanceId,
//              CreateTask.newBuilder()
//                  .setProcessInstanceId(processInstanceId)
//                  .setTaskInstanceId(taskInstanceId)
//                  .setProviderId(providerId)
//                  .setTaskTypeId(taskTypeId)
//                  .setTaskTitle(title)
//                  .setTaskDescription(description)
//                  //.processTypeId(message.getTaskType().getProcessType() == null ? null : message.getTaskType().getProcessType().getId())
//                  .setCreatedOn(timestamp)
//                  .build())
//      );
//
//      if (CollectionUtils.isNotEmpty(candidateUserIdList) || CollectionUtils.isNotEmpty(
//          candidateGroupIdList)) {
//        commands.add(
//            buildMessage(processInstanceId,
//                AssignTask.newBuilder()
//                    .setProcessInstanceId(processInstanceId)
//                    .setTaskInstanceId(taskInstanceId)
//                    .addAllCandidateUserIds(candidateUserIdList)
//                    .addAllCandidateGroupIds(candidateGroupIdList)
//                    .build())
//        );
//      }
//
//      if (StringUtils.isNotEmpty(claimerUserId)) {
//        commands.add(
//            buildMessage(processInstanceId,
//                ClaimTask.newBuilder()
//                    .setProcessInstanceId(processInstanceId)
//                    .setTaskInstanceId(taskInstanceId)
//                    .setClaimedOn(timestamp)
//                    .setClaimedBy(claimerUserId)
//                    .setClaimedOnBehalfOf(onBehalfOfClaimerUserId)
//                    .build())
//        );
//      }
//
//      // save the link between the process type and task type
//      Optional<String> processTypeId = findProcessTypeId(processInstanceId);
//      processTypeId.ifPresent(
//          o ->
//              commands.add(
//                  buildMessage(o,
//                      LinkProcessTypeTaskType.newBuilder()
//                          .setProcessTypeId(o)
//                          .setTaskTypeId(taskTypeId)
//                          .build()))
//      );
//
//      return commands;
//    }
//    return emptyList();
//  }
//
//
//  public List<Message<CCMessage>> on(TaskCreated message, Timestamp messageDate) {
//    trace(message);
//
//    return createTask(
//        message.getProcessInstanceId(),
//        message.getTaskInstanceId(),
//        message.getProviderId(),
//        message.getTaskTypeId(),
//        orElse(message.getTimestamp(), messageDate),
//        message.getCandidateUserIdList(),
//        message.getCandidateGroupIdList(),
//        message.getClaimerUserId(),
//        message.getClaimerOnBehalfOfUserId(),
//        message.getTitle(),
//        message.getDescription()
//    );
//  }
//
//
//  public List<Message<CCMessage>> on(TaskEntered message) {
//    trace(message);
//    return emptyList();
//  }
//
//  public List<Message<CCMessage>> on(TaskRegistered message) {
//    trace(message);
//
//    String processInstanceId = findProcessIdFromTaskMessage(message.getProcessInstanceId(),
//        message.getTaskInstanceId());
//
//    if (StringUtils.isNotEmpty(processInstanceId)) {
//      return Collections.singletonList(buildMessage(processInstanceId,
//          RegisterTask.newBuilder()
//              .setProcessInstanceId(processInstanceId)
//              .setTaskInstanceId(message.getTaskInstanceId())
//              .setTimestamp(message.getTimestamp())
//              .setTitle(message.getTitle())
//              .setTitleWithoutProcessReferences(message.getTitleWithoutProcessReferences())
//              .build(),
//          parentId));
//    }
//    return emptyList();
//  }
//
//  public List<Message<CCMessage>> on(MessageAdded message, Timestamp messageDate) {
//    trace(message);
//
//    String processInstanceId = CommentUtil.getProcessId(message.getMessage());
//    if (processInstanceId != null) {
//      return Collections.singletonList(buildMessage(processInstanceId,
//          AddComment
//              .newBuilder()
//              .setProcessInstanceId(processInstanceId)
//              .setTaskInstanceId(CommentUtil.getTaskId(message.getMessage()))
//              .setCommentId(message.getMessageId())
//              .setTimestamp(message.getMessage().getTimestamp())
//              .setComment(message.getMessage().getBody())
//              .setUsername(message.getMessage().getUserFullName())
//              .build(),
//          parentId));
//    }
//    return emptyList();
//  }
//
//
//
//  public List<Message<CCMessage>> on(MessageDeleted message, Timestamp messageDate) {
//    trace(message);
//
//    String processInstanceId = CommentUtil.getProcessId(message.getMessage());
//    if (processInstanceId != null) {
//      return Collections.singletonList(buildMessage(processInstanceId,
//          DeleteComment
//              .newBuilder()
//              .setProcessInstanceId(processInstanceId)
//              .setTaskInstanceId(CommentUtil.getTaskId(message.getMessage()))
//              .setCommentId(message.getMessageId())
//              .build(),
//          parentId));
//    }
//
//    return emptyList();
//  }
//
//  public List<Message<CCMessage>> on(MessageUpdated message, Timestamp messageDate) {
//    trace(message);
//
//    String processInstanceId = CommentUtil.getProcessId(message.getMessage());
//    if (processInstanceId != null) {
//      return Collections.singletonList(buildMessage(processInstanceId,
//          UpdateComment
//              .newBuilder()
//              .setProcessInstanceId(processInstanceId)
//              .setTaskInstanceId(CommentUtil.getTaskId(message.getMessage()))
//              .setCommentId(message.getMessage().getMessageId())
//              .setTimestamp(message.getMessage().getTimestamp())
//              .setComment(message.getMessage().getBody())
//              .setUsername(message.getMessage().getLastUpdatedByFullName())
//              .build(),
//          parentId));
//    }
//
//    return emptyList();
//  }
//
//  String findProcessIdFromTaskMessage(String processInstanceId, String taskInstanceId) {
//    if (StringUtils.isEmpty(processInstanceId)) {
//      processInstanceId = taskService.findProcessId(taskInstanceId);
//      LOG.info("process instance: {} found for task instance:{}", processInstanceId,
//          taskInstanceId);
//
//      if (StringUtils.isEmpty(processInstanceId)) {
//        tracing.error(
//            new ProcessCentreException("No Process found for task Instance Id " + taskInstanceId));
//      } else {
//        tracing.tag("processInstanceId", processInstanceId);
//      }
//      tracing.tag("processIdSetInMessage", "false");
//    } else {
//      tracing.tag("processIdSetInMessage", "true");
//    }
//    return processInstanceId;
//  }
//
//  public List<Message<CCMessage>> on(TaskCompleted message, Timestamp messageDate) {
//    trace(message);
//
//    String processInstanceId = findProcessIdFromTaskMessage(message.getProcessInstanceId(),
//        message.getTaskInstanceId());
//
//    List<Message<CCMessage>> result = Lists.newArrayList();
//
//    if (StringUtils.isNotEmpty(processInstanceId)) {
//      result.add(
//          buildMessage(processInstanceId,
//              CompleteTask.newBuilder()
//                  .setProcessInstanceId(processInstanceId)
//                  .setTaskInstanceId(message.getTaskInstanceId())
//                  .setCompletedOn(orElse(message.getTimestamp(), messageDate))
//                  .setCompletedBy(message.getUserId())
//                  .setCompletedOnBehalfOf(message.getOnBehalfOfUserId())
//                  .setOutcome(message.getOutcome())
//                  .setComment(message.getComment())
//                  .build())
//      );
//
//    }
//
//    return result;
//  }
//
//  public List<Message<CCMessage>> on(TaskAssigned message, Timestamp messageDate) {
//    trace(message);
//
//    String processInstanceId = findProcessIdFromTaskMessage(message.getProcessInstanceId(),
//        message.getTaskInstanceId());
//
//    if (StringUtils.isNotEmpty(processInstanceId)) {
//      List<Message<CCMessage>> commands = Lists.newArrayList();
//
//      commands.add(buildMessage(processInstanceId,
//          AssignTask.newBuilder()
//              .setProcessInstanceId(processInstanceId)
//              .setTaskInstanceId(message.getTaskInstanceId())
//              .addAllCandidateUserIds(message.getCandidateUserIdList())
//              .addAllCandidateGroupIds(message.getCandidateGroupIdList())
//              .build(),
//          parentId));
//
//      if (StringUtils.isNotEmpty(message.getClaimerUserId())) {
//        commands.add(buildMessage(processInstanceId,
//            ClaimTask.newBuilder()
//                .setProcessInstanceId(processInstanceId)
//                .setTaskInstanceId(message.getTaskInstanceId())
//                .setClaimedOn(orElse(message.getTimestamp(), messageDate))
//                .setClaimedBy(message.getClaimerUserId())
//                .setClaimedOnBehalfOf(message.getClaimerOnBehalfOfUserId())
//                .build(),
//            parentId));
//      }
//
//      return commands;
//    }
//
//    return emptyList();
//  }
//
//  public List<Message<CCMessage>> on(TaskClaimed message, Timestamp messageDate) {
//    trace(message);
//
//    String processInstanceId = findProcessIdFromTaskMessage(message.getProcessInstanceId(),
//        message.getTaskInstanceId());
//
//    if (StringUtils.isNotEmpty(processInstanceId)) {
//      return Collections.singletonList(
//          buildMessage(processInstanceId,
//              ClaimTask.newBuilder()
//                  .setProcessInstanceId(processInstanceId)
//                  .setTaskInstanceId(message.getTaskInstanceId())
//                  .setClaimedOn(orElse(message.getTimestamp(), messageDate))
//                  .setClaimedBy(message.getUserId())
//                  .setClaimedOnBehalfOf(message.getOnBehalfOfUserId())
//                  .build())
//      );
//    }
//
//    return emptyList();
//  }
//
//
//  public List<Message<CCMessage>> on(TaskUnclaimed message, Timestamp messageDate) {
//    trace(message);
//
//    String processInstanceId = findProcessIdFromTaskMessage(message.getProcessInstanceId(),
//        message.getTaskInstanceId());
//
//    if (StringUtils.isNotEmpty(processInstanceId)) {
//      return Collections.singletonList(
//          buildMessage(processInstanceId,
//              UnclaimTask.newBuilder()
//                  .setProcessInstanceId(processInstanceId)
//                  .setTaskInstanceId(message.getTaskInstanceId())
//                  .setUnclaimedOn(orElse(message.getTimestamp(), messageDate))
//                  .build())
//      );
//    }
//    return emptyList();
//  }
//
//
//  public List<Message<CCMessage>> on(TaskCancelled message, Timestamp messageDate) {
//    trace(message);
//
//    String processInstanceId = findProcessIdFromTaskMessage(message.getProcessInstanceId(),
//        message.getTaskInstanceId());
//
//    if (StringUtils.isNotEmpty(processInstanceId)) {
//      return Collections.singletonList(
//          buildMessage(processInstanceId,
//              CancelTask.newBuilder()
//                  .setProcessInstanceId(processInstanceId)
//                  .setTaskInstanceId(message.getTaskInstanceId())
//                  .setCancelledOn(orElse(message.getTimestamp(), messageDate))
//                  .build())
//      );
//    }
//    return emptyList();
//  }
//
//
//  public List<Message<CCMessage>> on(TaskDeleted message, Timestamp messageDate) {
//    trace(message);
//
//    String processInstanceId = findProcessIdFromTaskMessage(message.getProcessInstanceId(),
//        message.getTaskInstanceId());
//
//    if (StringUtils.isNotEmpty(processInstanceId)) {
//      return Collections.singletonList(
//          buildMessage(processInstanceId,
//              DeleteTask.newBuilder()
//                  .setProcessInstanceId(processInstanceId)
//                  .setTaskInstanceId(message.getTaskInstanceId())
//                  .setDeletedOn(orElse(message.getDeletedOn(), messageDate))
//                  .build())
//      );
//    }
//    return emptyList();
//  }
//
//
//  public List<Message<CCMessage>> on(TaskUpdated message) {
//    trace(message);
//
//    List<Message<CCMessage>> result = Lists.newArrayList();
//
//    updateTask(message.getProcessInstanceId(), message.getTaskInstanceId())
//        .map(updateTask -> {
//
//          final TaskUpdated defaults = TaskUpdated.getDefaultInstance();
//
//          if (!defaults.getTitle().equals(message.getTitle())) {
//            updateTask.setTaskTitle(message.getTitle());
//          }
//          if (!defaults.getDescription().equals(message.getDescription())) {
//            updateTask.setTaskDescription(message.getDescription());
//          }
//          if (!defaults.getClaimerUserId().equals(message.getClaimerUserId())) {
//            updateTask.setClaimedBy(message.getClaimerUserId());
//          }
//          if (!defaults.getClaimerOnBehalfOfUserId().equals(message.getClaimerOnBehalfOfUserId())) {
//            updateTask.setClaimedOnBehalfOf(message.getClaimerUserId());
//          }
//          // Currently, TaskUpdated does not allow specifying (and thus, modifying)
//          // the id of the user who completed the task...
//          //if (!defaults.get??().equals(message.get??())) {
//          //  updateTask.setCompletedBy(message.get??());
//          //}
//
//          return updateTask;
//        })
//        .ifPresent(updateTask -> result.add(
//            buildMessage(updateTask.getProcessInstanceId(),
//                updateTask.build()))
//        );
//    return result;
//  }
//
//  /**
//   * TaskRestored reset all fields to the sent values.
//   */
//  public List<Message<CCMessage>> on(TaskRestored message) {
//    trace(message);
//
//    String processInstanceId = findProcessIdFromTaskMessage(message.getProcessInstanceId(),
//        message.getTaskInstanceId());
//
//    if (StringUtils.isNotEmpty(processInstanceId)) {
//      Builder restoreTaskBuilder = RestoreTask.newBuilder()
//          .setProcessInstanceId(processInstanceId)
//          .setTaskInstanceId(message.getTaskInstanceId())
//          .setTaskTypeId(message.getTaskTypeId())
//          .setProviderId(message.getProviderId())
//          .setCreatedOn(message.getCreatedOn())
//          .setClaimedOn(message.getClaimedOn())
//          .setClaimedBy(message.getClaimerUserId())
//          .setClaimedOnBehalfOf(message.getClaimerOnBehalfOfUserId())
//          .setCompletedOn(message.getCompletedOn())
//          .setCompletedBy(message.getCompleteUserId())
//          .setCompletedOnBehalfOf(message.getCompleteOnBehalfOfUserId());
//      //.setCancelledOn(message.getCancelledOn()) // no cancelledOn in provider proto
//
//      if (message.hasTitle()) {
//        restoreTaskBuilder.setTaskTitle(message.getTitle());
//      }
//
//      if (message.hasDescription()) {
//        restoreTaskBuilder.setTaskDescription(message.getDescription());
//      }
//
//      return Collections.singletonList(
//          buildMessage(processInstanceId,
//              restoreTaskBuilder
//                  .build()));
//    } else {
//      LOG.warn(
//          "processInstanceId was not found for taskInstanceId:{}. Consuming TaskRestore aborted",
//          message.getTaskInstanceId());
//    }
//    return emptyList();
//  }
//
//  private Optional<UpdateTask.Builder> updateTask(String processInstanceId, String taskInstanceId) {
//    processInstanceId = findProcessIdFromTaskMessage(processInstanceId, taskInstanceId);
//
//    return StringUtils.isBlank(processInstanceId)
//        ? Optional.empty()
//        : Optional.of(UpdateTask.newBuilder().setProcessInstanceId(processInstanceId)
//            .setTaskInstanceId(taskInstanceId));
//  }
//
//
//  public List<Message<CCMessage>> on(ServiceTaskCreated message, Timestamp timestamp) {
//    trace(message);
//
//    String processInstanceId = message.getProcessInstanceId();
//
//    List<Message<CCMessage>> result = Lists.newArrayList();
//
//    if (StringUtils.isNotEmpty(processInstanceId)) {
//      result.add(
//          buildMessage(processInstanceId,
//              CreateServiceTask.newBuilder()
//                  .setProcessInstanceId(processInstanceId)
//                  .setServiceTaskId(message.getServiceTaskId())
//                  .setServiceTaskTypeId(message.getServiceTaskTypeKey())
//                  .setProviderId(message.getProviderId())
//                  .setActivityId(message.getActivityId())
//                  .setActivityInstanceId(message.getActivityInstanceId())
//                  .setExecutionId(message.getExecutionId())
//                  .setProcessTypeId(message.getProcessTypeKey())
//                  .setTitle(message.getTitle())
//                  .setTopicName(message.getTopicName())
//                  .setBusinessKey(message.getBusinessKey())
//                  .setSuspended(message.getSuspended())
//                  .setCreatedOn(timestamp)
//                  .build())
//      );
//
//    }
//    return result;
//  }
//
//  private List<Message<CCMessage>> on(ServiceTaskCompleteSucceeded message, Timestamp messageDate) {
//    trace(message);
//
//    String processInstanceId = findProcessIdFromTaskMessage(message.getProcessInstanceId(),
//        message.getServiceTaskId());
//
//    List<Message<CCMessage>> result = Lists.newArrayList();
//
//    if (StringUtils.isNotEmpty(processInstanceId)) {
//
//      result.add(
//          buildMessage(processInstanceId,
//              CompleteServiceTask.newBuilder()
//                  .setProcessInstanceId(processInstanceId)
//                  .setServiceTaskId(message.getServiceTaskId())
//                  .setWorkerId(message.getWorkerId())
//                  .setActivityId(message.getActivityId())
//                  .setActivityInstanceId(message.getActivityInstanceId())
//                  .setExecutionId(message.getExecutionId())
//                  .setProcessTypeId(message.getProcessTypeKey())
//                  .setTopicName(message.getTopicName())
//                  .setBusinessKey(message.getBusinessKey())
//                  .setCompletedOn(messageDate)
//                  .setInfoMessage(message.getInfoMessage())
//                  .build()));
//
//    }
//
//    return result;
//  }
//
//  private List<Message<CCMessage>> on(ServiceTaskCompleteFailed message, Timestamp messageDate) {
//    trace(message);
//    if (message.getRetries() > 0) {
//      LOG.debug(
//          "ServiceTaskCompleteFailed for serviceTaskId {} is ignored in PC - retries left: {}",
//          message.getServiceTaskId(), message.getRetries());
//      return emptyList();
//    }
//
//    List<Message<CCMessage>> result = Lists.newArrayList();
//
//    result.add(
//        buildMessage(message.getProcessInstanceId(),
//            FailServiceTask.newBuilder()
//                .setProcessInstanceId(message.getProcessInstanceId())
//                .setServiceTaskId(message.getServiceTaskId())
//                .setErrorMsg(message.getErrorMessage())
//                .setFailedOn(message.getTimestamp())
//                .build()));
//
//    return result;
//  }
//
//  private Collection<? extends Message<CCMessage>> on(ServiceTaskUnlocked message) {
//    trace(message);
//
//    List<Message<CCMessage>> result = Lists.newArrayList();
//
//    result
//        .add(
//            buildMessage(message.getProcessInstanceId(),
//                UnlockServiceTask
//                    .newBuilder()
//                    .setServiceTaskId(message.getServiceTaskId())
//                    .setProcessInstanceId(message.getProcessInstanceId())
//                    .setUnlockedOn(message.getTimestamp())
//                    .build()));
//
//    return result;
//  }
//
//  private Collection<? extends Message<CCMessage>> on(ServiceTaskLocked message) {
//    trace(message);
//
//    List<Message<CCMessage>> result = Lists.newArrayList();
//
//    result
//        .add(
//            buildMessage(message.getProcessInstanceId(),
//                LockServiceTask
//                    .newBuilder()
//                    .setServiceTaskId(message.getServiceTaskId())
//                    .setProcessInstanceId(message.getProcessInstanceId())
//                    .setLockedOn(message.getTimestamp())
//                    .build()));
//
//    return result;
//  }
//
//
//
//  private Collection<? extends Message<CCMessage>> on(RetryServiceTaskResponse message) {
//    trace(message);
//
//    List<Message<CCMessage>> result = Lists.newArrayList();
//
//    String processInstanceId = StringUtils.isNotEmpty(message.getProcessInstanceId())
//        ? message.getProcessInstanceId()
//        : findProcessIdFromTaskMessage(message.getProcessInstanceId(), message.getServiceTaskId());
//    if (processInstanceId != null) {
//      result
//          .add(
//              buildMessage(message.getProcessInstanceId(),
//                  RegisterRetryServiceTaskResponse
//                      .newBuilder()
//                      .setServiceTaskId(message.getServiceTaskId())
//                      .setProviderId(message.getProviderId())
//                      .setProcessInstanceId(processInstanceId)
//                      .setSuccess(message.getSuccess())
//                      .setMessage(message.getMessage())
//                      .build()));
//    }
//
//    return result;
//  }
//
//  private Collection<? extends Message<CCMessage>> on(ServiceTaskCancelled message) {
//    trace(message);
//
//    List<Message<CCMessage>> result = Lists.newArrayList();
//
//    String processInstanceId = StringUtils.isNotEmpty(message.getProcessInstanceId())
//        ? message.getProcessInstanceId()
//        : findProcessIdFromTaskMessage(message.getProcessInstanceId(), message.getServiceTaskId());
//    if (processInstanceId != null) {
//      result
//          .add(
//              buildMessage(message.getProcessInstanceId(),
//                  CancelServiceTask
//                      .newBuilder()
//                      .setServiceTaskId(message.getServiceTaskId())
//                      .setProviderId(message.getProviderId())
//                      .setProcessInstanceId(message.getProcessInstanceId())
//                      .setReason(message.getReason())
//                      .setTimestamp(message.getTimestamp())
//                      .build()));
//    }
//
//    return result;
//  }
//
//  private Collection<? extends Message<CCMessage>> on(SkipServiceTaskResponse message, Timestamp messageDate) {
//    trace(message);
//
//    List<Message<CCMessage>> result = Lists.newArrayList();
//
//    String processInstanceId = StringUtils.isNotEmpty(message.getProcessInstanceId())
//        ? message.getProcessInstanceId()
//        : findProcessIdFromTaskMessage(message.getProcessInstanceId(), message.getServiceTaskId());
//    if (processInstanceId != null) {
//      result
//          .add(
//              buildMessage(processInstanceId,
//                  RegisterSkipServiceTaskResponse
//                      .newBuilder()
//                      .setServiceTaskId(message.getServiceTaskId())
//                      .setProviderId(message.getProviderId())
//                      .setProcessInstanceId(processInstanceId)
//                      .setSuccess(message.getSuccess())
//                      .setMessage(message.getMessage())
//                      .setSkippedOn(messageDate) // no field with date in the message
//                      .build()));
//    }
//    return result;
//  }
//
//
//  /*
//   * Listen on the events defined by the portfolio manager
//   */
//  public List<Message<CCMessage>> on(PortfolioItemCreated message) {
//    trace(message);
//
//    List<Message<CCMessage>> result = Lists.newArrayList();
//
//    processDocumentCustomRepository
//        .findProcessIdsByPortfolioId(message.getPortfolioItemId())
//        .stream()
//        .forEach(id -> result
//            .add(buildMessage((id),
//                UpdatePortfolioData
//                    .newBuilder()
//                    .setProcessInstanceId(id)
//                    .setPortfolioItemId(message.getPortfolioItemId())
//                    .setBusinessKey(message.getBusinessId())
//                    .setDescription(message.getDescription())
//                    .build())));
//
//    asyncListener.handle(portfolioReadModelService::handle, message);
//    return result;
//  }
//
//  public List<Message<CCMessage>> on(PortfolioItemTypeChanged message) {
//    trace(message);
//
//    asyncListener.handle(portfolioReadModelService::handle, message);
//    return emptyList();
//  }
//
//  public List<Message<CCMessage>> on(PortfolioItemDescriptionChanged message) {
//    trace(message);
//
//    List<Message<CCMessage>> result = Lists.newArrayList();
//
//    processDocumentCustomRepository
//        .findProcessIdsByPortfolioId(message.getPortfolioItemId())
//        .stream()
//        .forEach(id -> result
//            .add(buildMessage((id),
//                UpdatePortfolioData
//                    .newBuilder()
//                    .setProcessInstanceId(id)
//                    .setPortfolioItemId(message.getPortfolioItemId())
//                    .setDescription(message.getDescription())
//                    .build())));
//
//    asyncListener.handle(portfolioReadModelService::handle, message);
//    return result;
//  }
//
//
//  /* Listen on the events defined by the translation service Babel */
//
//  public List<Message<CCMessage>> on(TranslationCreated message) {
//    trace(message);
//
//    return onTranslation(message.getLanguageCode(), message.getUrn(), message.getText(),
//        message.getDefault());
//  }
//
//  public List<Message<CCMessage>> on(TranslationUpdated message) {
//    trace(message);
//
//    return onTranslation(message.getLanguageCode(), message.getUrn(), message.getText(),
//        message.getDefault());
//  }
//
//  List<Message<CCMessage>> onTranslation(ISO6391LanguageCode languageCode,
//      String urn, String text, boolean isDefault) {
//    if (languageCode.equals(ISO6391LanguageCode.UNRECOGNIZED)) {
//      LOG.warn("No language code for urn: {}", urn);
//      return emptyList();
//    }
//
//    if (!babelTextService
//        .isTranslationUpdated(urn, text,
//            languageCode.name(), isDefault)) {
//      return emptyList();
//    }
//
//    List<Message<CCMessage>> result = Lists.newArrayList();
//
//    // update db
//    babelTextService
//        .updateTranslation(urn, text, languageCode.name(),
//            isDefault);
//
//    babelTextService
//        .findProcessIdsByBabelUrnInDescription(urn)
//        .forEach(p -> result
//            .add(buildMessage(p,
//                UpdateProcessBabelTranslation
//                    .newBuilder()
//                    .setProcessInstanceId(p)
//                    .setFieldUpdated(BabelTextField.PROCESS_DESCRIPTION)
//                    .setUrn(urn)
//                    .setLiteralValue(
//                        LiteralValue
//                            .newBuilder()
//                            .setLanguageCode(languageCode)
//                            .setOrClearText(text)
//                            .setDefault(isDefault))
//                    .build())));
//
//    babelTextService
//        .findProcessIdsByBabelUrnInTypeName(urn)
//        .forEach(p -> result
//            .add(buildMessage(p,
//                UpdateProcessBabelTranslation
//                    .newBuilder()
//                    .setProcessInstanceId(p)
//                    .setFieldUpdated(BabelTextField.PROCESS_TYPE_NAME)
//                    .setUrn(urn)
//                    .setLiteralValue(
//                        LiteralValue
//                            .newBuilder()
//                            .setLanguageCode(languageCode)
//                            .setOrClearText(text)
//                            .setDefault(isDefault))
//                    .build())));
//
//    babelTextService
//        .findProcessIdsByBabelUrnInTypeTitleTemplate(urn)
//        .forEach(p -> result
//            .add(buildMessage(p,
//                UpdateProcessBabelTranslation
//                    .newBuilder()
//                    .setProcessInstanceId(p)
//                    .setFieldUpdated(BabelTextField.PROCESS_TYPE_TITLE_TEMPLATE)
//                    .setUrn(urn)
//                    .setLiteralValue(
//                        LiteralValue
//                            .newBuilder()
//                            .setLanguageCode(languageCode)
//                            .setOrClearText(text)
//                            .setDefault(isDefault))
//                    .build())));
//
//    babelTextService
//        .findProcessIdsByBabelUrnInPortfolio(urn)
//        .forEach(p -> result
//            .add(buildMessage(p,
//                UpdateProcessBabelTranslation
//                    .newBuilder()
//                    .setProcessInstanceId(p)
//                    .setFieldUpdated(BabelTextField.PORTFOLIO_ITEM_DESCRIPTION)
//                    .setUrn(urn)
//                    .setLiteralValue(
//                        LiteralValue
//                            .newBuilder()
//                            .setLanguageCode(languageCode)
//                            .setOrClearText(text)
//                            .setDefault(isDefault))
//                    .build())));
//
//    babelTextService
//        .findProcessIdsByBabelUrnInTaskTitle(urn)
//        .forEach(p -> result
//            .add(buildMessage(p,
//                UpdateProcessBabelTranslation
//                    .newBuilder()
//                    .setProcessInstanceId(p)
//                    .setFieldUpdated(BabelTextField.TASK_TITLE)
//                    .setUrn(urn)
//                    .setLiteralValue(
//                        LiteralValue
//                            .newBuilder()
//                            .setLanguageCode(languageCode)
//                            .setOrClearText(text)
//                            .setDefault(isDefault))
//                    .build())));
//
//    babelTextService
//        .findProcessIdsByBabelUrnInTaskDescription(urn)
//        .forEach(p -> result
//            .add(buildMessage(p,
//                UpdateProcessBabelTranslation
//                    .newBuilder()
//                    .setProcessInstanceId(p)
//                    .setFieldUpdated(BabelTextField.TASK_DESCRIPTION)
//                    .setUrn(urn)
//                    .setLiteralValue(
//                        LiteralValue
//                            .newBuilder()
//                            .setLanguageCode(languageCode)
//                            .setOrClearText(text)
//                            .setDefault(isDefault))
//                    .build())));
//
//    return result;
//  }
//
//
//  /* Listen on the events defined by the participant container */
//  public List<Message<CCMessage>> on(ParticipantUpdated message) {
//    trace(message);
//
//    // disabling ParticipantUpdated for now, not needed for PC and generates a huge load
//    return emptyList();
//  }
//
//  List<Message<CCMessage>> on(OrganisationChanged message) {
//    trace(message);
//    List<Message<CCMessage>> result = Lists.newArrayList();
//    if (StringUtils.isEmpty(message.getNewOrganisationCode())) {
//      LOG.info("OrganisationChanged: organisationCode for organisationId: {} is empty - no update in Process Centre", message.getOrganisationId());
//      return result;
//    }
//
//    List<String> processIds = processRepository.findProcessIdByResponsibleOrganisationId(
//        message.getOrganisationId());
//
//    for (String processId : processIds) {
////            if (process.getResponsibleOrganisationCode() != null && process.getResponsibleOrganisationCode().equals(message.getNewOrganisationCode())) {
////                LOG.debug("The organisation code ({}) hasn't changed for the processId:{}.", process.getResponsibleOrganisationCode(), process.getId());
////            } else {
//      result.add(
//          buildMessage(processId,
//              UpdateOrganisationInfo.newBuilder()
//                  .setProcessInstanceId(processId)
//                  .setOrganisationId(message.getOrganisationId())
//                  .setOrganisationCode(message.getNewOrganisationCode())
//                  .build())
//      );
////            }
//    }
//    return result;
//  }
//
//  @SneakyThrows
//  void trace(com.google.protobuf.Message message) {
//    if (LOG.isInfoEnabled()) {
//      LOG.info("Consuming kafka event {}: {}", message.getClass().getName(),
//          JsonFormat.printer().print(message));
//    }
//    tracing.tag(message);
//  }
//
//  /**
//   * Return {@code timestamp} if is set, otherwise return {@code other}.
//   *
//   * @param other the value to be returned if {@code timestamp} is null or not set
//   * @return {@code timestamp} if not set, otherwise {@code other}
//   */
//  static Timestamp orElse(Timestamp timestamp, Timestamp other) {
//    return (timestamp != null && timestamp.getSeconds() > 0 ? timestamp : other);
//  }

}
