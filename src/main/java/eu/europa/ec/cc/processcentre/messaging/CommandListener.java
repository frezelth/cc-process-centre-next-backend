package eu.europa.ec.cc.processcentre.messaging;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import eu.europa.ec.cc.message.proto.CCMessage;
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
import eu.europa.ec.cc.processcentre.proto.RefreshProcessTypeConfiguration;
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
import eu.europa.ec.cc.processcentre.proto.UpdateProcessResponsibleUser;
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
import io.micrometer.tracing.annotation.NewSpan;
import java.time.Instant;
import java.util.Date;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CommandListener {

//  private static final String CONSUMING_MESSAGE_LOG = "Consuming kafka message {}: {}";
//  private static final String CONTAINER_NOT_FOUND_LOG = "No container found for {}";
//
//  private final ApplicationEventPublisher eventPublisher;
//  private final TracingHelper tracing;
//
//  public CommandListener(ApplicationEventPublisher eventPublisher, TracingHelper tracing) {
//    this.eventPublisher = eventPublisher;
//    this.tracing = tracing;
//  }
//
//  /**
//   * This method is called by the kafka listener, it starts a transaction automatically
//   */
//  @NewSpan
//  @KafkaListener(
//      topics = "${kafka.command-topic.name}",
//      id = "processcentre.command.listener",
//      idIsGroup = false,
//  )
//  public void onCommand(@Payload(required = false) CCMessage message) throws Exception {
//    if (message != null) {
//
//      try {
//
//        boolean ignored = false;
//
//        tracing.tagMessageType(message);
//
//        boolean supportedCommand = isProcessCommand(message)
//            || isProcessUpdateCommand(message)
//            || isProcessTypeCommand(message)
//            || isTaskCommand(message)
//            || isServiceTaskCommand(message)
//            || isUserOrgCommand(message)
//            || isBabelCommand(message)
//            || isPortfolioCommand(message)
//            || isContainerCommand(message)
//            || isCommentCommand(message);
//
//        if (!supportedCommand) {
//          LOG.info("Ignoring kafka command: {}", new String(Base64.encodeBase64(message.toByteArray())));
//          ignored = true;
//        }
//
//        tracing.tag("message.ignored", String.valueOf(ignored));
//
//      } catch (Exception e) {
//        LOG.error("{} on kafka command: {} : {}", e.getMessage(), message.getClass().getName(), JsonFormat.printer().print(message), e);
//        tracing.error(e);
//        throw e;
//      }
//    }
//  }
//
//  private boolean isCommentCommand(CCMessage message) throws InvalidProtocolBufferException {
//    if (message.getPayload().is(AddComment.class)) {
//      on(message.getPayload().unpack(AddComment.class));
//    } else if (message.getPayload().is(DeleteComment.class)) {
//      on(message.getPayload().unpack(DeleteComment.class));
//    } else if (message.getPayload().is(UpdateComment.class)) {
//      on(message.getPayload().unpack(UpdateComment.class));
//    } else {
//      return false;
//    }
//    return true;
//  }
//
//  private boolean isPortfolioCommand(CCMessage message) throws InvalidProtocolBufferException {
//    if (message.getPayload().is(UpdatePortfolioData.class)) {
//      on(message.getPayload().unpack(UpdatePortfolioData.class));
//    } else if (message.getPayload().is(UpdatePortfolioTag.class)) {
//      on(message.getPayload().unpack(UpdatePortfolioTag.class));
//    } else {
//      return false;
//    }
//    return true;
//  }
//
//  private boolean isContainerCommand(CCMessage message) throws InvalidProtocolBufferException {
//    if (message.getPayload().is(CreateOrUpdateContainer.class)) {
//      on(message.getPayload().unpack(CreateOrUpdateContainer.class));
//    } else if (message.getPayload().is(CompleteContainer.class)) {
//      on(message.getPayload().unpack(CompleteContainer.class));
//    } else if (message.getPayload().is(ReopenContainer.class)) {
//      on(message.getPayload().unpack(ReopenContainer.class));
//    } else if (message.getPayload().is(DeleteContainer.class)) {
//      on(message.getPayload().unpack(DeleteContainer.class));
//    } else if (message.getPayload().is(UpdateVariables.class)) {
//      on(message.getPayload().unpack(UpdateVariables.class));
//    } else if (message.getPayload().is(UpdateParticipant.class)) {
//      on(message.getPayload().unpack(UpdateParticipant.class));
//    } else if (message.getPayload().is(UpdateContainerConfig.class)) {
//      on(message.getPayload().unpack(UpdateContainerConfig.class));
//    } else {
//      return false;
//    }
//    return true;
//  }
//
//  private boolean isProcessUpdateCommand(CCMessage message) throws InvalidProtocolBufferException {
//    if (message.getPayload().is(UpdateProcess.class)) {
//      on(message.getPayload().unpack(UpdateProcess.class));
//    } else if (message.getPayload().is(UpdateProcessContext.class)) {
//      on(message.getPayload().unpack(UpdateProcessContext.class));
//    } else if (message.getPayload().is(UpdateProcessTypeConfiguration.class)) {
//      on(message.getPayload().unpack(UpdateProcessTypeConfiguration.class));
//    } else if (message.getPayload().is(UpdateProcessResultCardConfiguration.class)) {
//      on(message.getPayload().unpack(UpdateProcessResultCardConfiguration.class));
//    } else if (message.getPayload().is(UpdateProcessResponsibleOrganisation.class)) {
//      on(message.getPayload().unpack(UpdateProcessResponsibleOrganisation.class));
//    } else if (message.getPayload().is(UpdateProcessResponsibleUser.class)) {
//      on(message.getPayload().unpack(UpdateProcessResponsibleUser.class));
//    } else if (message.getPayload().is(UpdateProcessDescription.class)) {
//      on(message.getPayload().unpack(UpdateProcessDescription.class));
//    } else if (message.getPayload().is(UpdateProcessSpecificAttribute.class)) {
//      on(message.getPayload().unpack(UpdateProcessSpecificAttribute.class));
//    } else if (message.getPayload().is(RemoveProcessSpecificAttribute.class)) {
//      on(message.getPayload().unpack(RemoveProcessSpecificAttribute.class));
//    } else if (message.getPayload().is(UpdateProcessVariables.class)) {
//      on(message.getPayload().unpack(UpdateProcessVariables.class));
//    } else if (message.getPayload().is(UpdateProcessBusinessDomainId.class)) {
//      on(message.getPayload().unpack(UpdateProcessBusinessDomainId.class));
//    } else if (message.getPayload().is(UpdateProcessBusinessStatus.class)) {
//      on(message.getPayload().unpack(UpdateProcessBusinessStatus.class));
//    } else {
//      return false;
//    }
//    return true;
//  }
//
//  private boolean isTaskCommand(CCMessage message) throws InvalidProtocolBufferException {
//    /* Command about tasks */
//    if (message.getPayload().is(CreateTask.class)) {
//      on(message.getPayload().unpack(CreateTask.class));
//    } else if (message.getPayload().is(CompleteTask.class)) {
//      on(message.getPayload().unpack(CompleteTask.class));
//    } else if (message.getPayload().is(AssignTask.class)) {
//      on(message.getPayload().unpack(AssignTask.class));
//    } else if (message.getPayload().is(ClaimTask.class)) {
//      on(message.getPayload().unpack(ClaimTask.class));
//    } else if (message.getPayload().is(UnclaimTask.class)) {
//      on(message.getPayload().unpack(UnclaimTask.class));
//    } else if (message.getPayload().is(CancelTask.class)) {
//      on(message.getPayload().unpack(CancelTask.class));
//    } else if (message.getPayload().is(DeleteTask.class)) {
//      on(message.getPayload().unpack(DeleteTask.class));
//    } else if (message.getPayload().is(RegisterTask.class)) {
//      on(message.getPayload().unpack(RegisterTask.class));
//    } else if (message.getPayload().is(RestoreTask.class)) {
//      on(message.getPayload().unpack(RestoreTask.class));
//    } else if (message.getPayload().is(UpdateTask.class)) {
//      on(message.getPayload().unpack(UpdateTask.class));
//    } else {
//      return false;
//    }
//    return true;
//  }
//
//  private boolean isServiceTaskCommand(CCMessage message) throws InvalidProtocolBufferException {
//    if (message.getPayload().is(CreateServiceTask.class)) {
//      on(message.getPayload().unpack(CreateServiceTask.class));
//    } else if (message.getPayload().is(CompleteServiceTask.class)) {
//      on(message.getPayload().unpack(CompleteServiceTask.class));
//    } else if (message.getPayload().is(FailServiceTask.class)) {
//      on(message.getPayload().unpack(FailServiceTask.class));
//    } else if (message.getPayload().is(LockServiceTask.class)) {
//      on(message.getPayload().unpack(LockServiceTask.class));
//    } else if (message.getPayload().is(UnlockServiceTask.class)) {
//      on(message.getPayload().unpack(UnlockServiceTask.class));
//    } else if (message.getPayload().is(RegisterRetryServiceTaskResponse.class)) {
//      on(message.getPayload().unpack(RegisterRetryServiceTaskResponse.class));
//    } else if (message.getPayload().is(RegisterSkipServiceTaskResponse.class)) {
//      on(message.getPayload().unpack(RegisterSkipServiceTaskResponse.class));
//    } else if (message.getPayload().is(CancelServiceTask.class)) {
//      on(message.getPayload().unpack(CancelServiceTask.class));
//    } else {
//      return false;
//    }
//    return true;
//  }
//
//  private boolean isUserOrgCommand(CCMessage message) throws InvalidProtocolBufferException {
//    /* Command about user and organisation information */
//    if (message.getPayload().is(UpdateOrganisationInfo.class)) {
//      on(message.getPayload().unpack(UpdateOrganisationInfo.class));
//    } else if (message.getPayload().is(UpdateUserInfo.class)) {
//      on(message.getPayload().unpack(UpdateUserInfo.class));
//    } else if (message.getPayload().is(UpdateUserInfo4ActionLog.class)) {
//      on(message.getPayload().unpack(UpdateUserInfo4ActionLog.class));
//    } else {
//      return false;
//    }
//    return true;
//  }
//
//  private boolean isBabelCommand(CCMessage message) throws InvalidProtocolBufferException {
//    /* Command about the babel translation*/
//    if (message.getPayload().is(UpdateProcessBabelTranslation.class)) {
//      on(message.getPayload().unpack(UpdateProcessBabelTranslation.class));
//    } else if (message.getPayload().is(UpdateProcessTypeBabelTranslation.class)) {
//      on(message.getPayload().unpack(UpdateProcessTypeBabelTranslation.class));
//    } else if (message.getPayload().is(UpdateProcessBabelText.class)) {
//      on(message.getPayload().unpack(UpdateProcessBabelText.class));
//    } else {
//      return false;
//    }
//    return true;
//  }
//
//  private boolean isProcessTypeCommand(CCMessage message) throws InvalidProtocolBufferException {
//    /* Command about the process type */
//    if (message.getPayload().is(CreateProcessType.class)) {
//      on(message.getPayload().unpack(CreateProcessType.class));
//    } else if (message.getPayload().is(UpdateProcessType.class)) {
//      on(message.getPayload().unpack(UpdateProcessType.class));
//    } else if (message.getPayload().is(UpdateProcessSecurity.class)) {
//      on(message.getPayload().unpack(UpdateProcessSecurity.class));
//    } else if (message.getPayload().is(AddProcessTaxonomy.class)) {
//      on(message.getPayload().unpack(AddProcessTaxonomy.class));
//    } else if (message.getPayload().is(RemoveProcessTaxonomy.class)) {
//      on(message.getPayload().unpack(RemoveProcessTaxonomy.class));
//    } else if (message.getPayload().is(UpdateProcessTaxonomy.class)) {
//      on(message.getPayload().unpack(UpdateProcessTaxonomy.class));
//    } else if (message.getPayload().is(LinkProcessTypeTaskType.class)) {
//      on(message.getPayload().unpack(LinkProcessTypeTaskType.class));
//    } else if (message.getPayload().is(LinkProcessTypeProcessState.class)) {
//      on(message.getPayload().unpack(LinkProcessTypeProcessState.class));
//    } else if (message.getPayload().is(UpdateDefaultBusinessDomainId.class)) {
//      on(message.getPayload().unpack(UpdateDefaultBusinessDomainId.class));
//    } else if (message.getPayload().is(RefreshProcessTypeConfiguration.class)) {
//      on(message.getPayload().unpack(RefreshProcessTypeConfiguration.class));
//    } else {
//      return false;
//    }
//    return true;
//  }
//
//  private boolean isProcessCommand(CCMessage message) throws InvalidProtocolBufferException {
//    /* Command about processes */
//    if (message.getPayload().is(CreateProcess.class)) {
//      on(message.getPayload().unpack(CreateProcess.class));
//    } else if (message.getPayload().is(LinkProcess.class)) {
//      on(message.getPayload().unpack(LinkProcess.class));
//    } else if (message.getPayload().is(UnlinkProcess.class)) {
//      on(message.getPayload().unpack(UnlinkProcess.class));
//    } else if (message.getPayload().is(AssociateProcessPortfolioItem.class)) {
//      on(message.getPayload().unpack(AssociateProcessPortfolioItem.class));
//    } else if (message.getPayload().is(DisassociateProcessPortfolioItem.class)) {
//      on(message.getPayload().unpack(DisassociateProcessPortfolioItem.class));
//    } else  if (message.getPayload().is(StartProcess.class)) {
//      on(message.getPayload().unpack(StartProcess.class));
//    } else if (message.getPayload().is(PauseProcess.class)) {
//      on(message.getPayload().unpack(PauseProcess.class));
//    } else if (message.getPayload().is(CompleteProcess.class)) {
//      on(message.getPayload().unpack(CompleteProcess.class));
//    } else if (message.getPayload().is(RegisterProcessStateChange.class)) {
//      on(message.getPayload().unpack(RegisterProcessStateChange.class));
//    } else if (message.getPayload().is(DeleteProcess.class)) {
//      on(message.getPayload().unpack(DeleteProcess.class));
//    } else if (message.getPayload().is(CancelProcess.class)) {
//      on(message.getPayload().unpack(CancelProcess.class));
//    } else if (message.getPayload().is(DeleteProcessPhysically.class)){
//      on(message.getPayload().unpack(DeleteProcessPhysically.class));
//    } else if (message.getPayload().is(UpdateProcessTaxonomyPaths.class)){
//      on(message.getPayload().unpack(UpdateProcessTaxonomyPaths.class));
//    } else if (message.getPayload().is(AddProcessFavourite.class)){
//      on(message.getPayload().unpack(AddProcessFavourite.class));
//    } else if (message.getPayload().is(RemoveProcessFavourite.class)){
//      on(message.getPayload().unpack(RemoveProcessFavourite.class));
//    } else {
//      return false;
//    }
//    return true;
//  }
//
//  @SneakyThrows
//  void on(RefreshProcessTypeConfiguration message) {
//
//    if (LOG.isDebugEnabled()){
//      LOG.debug(CONSUMING_MESSAGE_LOG, message.getClass().getName(), JsonFormat.printer().print(message));
//    }
//    tracing.tag(message);
//
//    configUpdateService.refreshProcessTypeConfiguration(message.getProviderId(), message.getDomainKey(), message.getProcessTypeKey());
//
//  }
//
//  @SneakyThrows
//  void on(DeleteProcessPhysically message) {
//
//    if (LOG.isDebugEnabled()){
//      LOG.debug(CONSUMING_MESSAGE_LOG, message.getClass().getName(), JsonFormat.printer().print(message));
//    }
//    tracing.tag(message);
//
//    eventPublisher.publishEvent(new ProcessPhysicalDeleteCommandReceived(message.getProcessInstanceId()));
//
//  }
//
//  void on(CreateOrUpdateContainer message) {
//
//    if (LOG.isDebugEnabled()) {
//      LOG.debug(CONSUMING_MESSAGE_LOG, message.getClass().getName(),
//          JSON_FORMAT.printToString(message));
//    }
//    tracing.tag(message);
//
//    try {
//      participantContainerService.createOrUpdateContainer(message);
//    } catch (ConfigNotFoundException e){
//      LOG.warn("Process detail configuration not found for: {}", message.getContainerId());
//    }
//  }
//
//  void on(CompleteContainer message) {
//
//    if (LOG.isDebugEnabled()) {
//      LOG.debug(CONSUMING_MESSAGE_LOG, message.getClass().getName(),
//          JSON_FORMAT.printToString(message));
//    }
//    tracing.tag(message);
//
//    try {
//      participantContainerService.completeContainer(message);
//    } catch (AggregateNotFoundException e){
//      LOG.debug(CONTAINER_NOT_FOUND_LOG, message.getContainerId());
//    }
//  }
//
//  void on(ReopenContainer message) {
//
//    if (LOG.isDebugEnabled()) {
//      LOG.debug(CONSUMING_MESSAGE_LOG, message.getClass().getName(),
//          JSON_FORMAT.printToString(message));
//    }
//    tracing.tag(message);
//
//    try {
//      participantContainerService.reopenContainer(message);
//    } catch (AggregateNotFoundException e){
//      LOG.debug(CONTAINER_NOT_FOUND_LOG, message.getContainerId());
//    }
//  }
//
//  void on(DeleteContainer message) {
//
//    if (LOG.isDebugEnabled()) {
//      LOG.debug(CONSUMING_MESSAGE_LOG, message.getClass().getName(),
//          JSON_FORMAT.printToString(message));
//    }
//    tracing.tag(message);
//
//    try {
//      participantContainerService.deleteContainer(message);
//    } catch (AggregateNotFoundException e){
//      LOG.debug(CONTAINER_NOT_FOUND_LOG, message.getContainerId());
//    }
//  }
//
//  void on(UpdateVariables message) {
//
//    if (LOG.isDebugEnabled()) {
//      LOG.debug(CONSUMING_MESSAGE_LOG, message.getClass().getName(),
//          JSON_FORMAT.printToString(message));
//    }
//    tracing.tag(message);
//
//    try {
//      participantContainerService.updateVariables(message);
//    } catch (AggregateNotFoundException e){
//      LOG.debug(CONTAINER_NOT_FOUND_LOG, message.getContainerId());
//    }
//  }
//
//  void on(UpdateParticipant message) {
//
//    if (LOG.isDebugEnabled()) {
//      LOG.debug(CONSUMING_MESSAGE_LOG, message.getClass().getName(),
//          JSON_FORMAT.printToString(message));
//    }
//    tracing.tag(message);
//
//    try {
//      participantContainerService.handle(message);
//    } catch (AggregateNotFoundException e){
//      LOG.debug(CONTAINER_NOT_FOUND_LOG, message.getContainerId());
//    }
//  }
//
//  void on(UpdateContainerConfig message) {
//
//    if (LOG.isDebugEnabled()) {
//      LOG.debug(CONSUMING_MESSAGE_LOG, message.getClass().getName(),
//          JSON_FORMAT.printToString(message));
//    }
//    tracing.tag(message);
//
//    try {
//      participantContainerService.updateContainerConfig(message);
//    } catch (AggregateNotFoundException e){
//      LOG.debug(CONTAINER_NOT_FOUND_LOG, message.getContainerId());
//    }
//  }
//
//  @SneakyThrows
//  <T extends Message> void on(T message) {
//
//    if (LOG.isDebugEnabled()){
//      LOG.debug(CONSUMING_MESSAGE_LOG, message.getClass().getName(), JsonFormat.printer().print(message));
//    }
//
//    tracing.tag(message);
//    commandGateway.sendAndWait(message);
//  }

}
