package eu.europa.ec.cc.processcentre.process.command.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.cc.processcentre.config.AccessRight;
import eu.europa.ec.cc.processcentre.config.AccessRight.Right;
import eu.europa.ec.cc.processcentre.config.ProcessTypeConfig;
import eu.europa.ec.cc.processcentre.config.service.ConfigService;
import eu.europa.ec.cc.processcentre.event.ProcessRegistered;
import eu.europa.ec.cc.processcentre.model.ProcessAction;
import eu.europa.ec.cc.processcentre.process.command.converter.EventConverter;
import eu.europa.ec.cc.processcentre.process.command.repository.ProcessMapper;
import eu.europa.ec.cc.processcentre.process.command.repository.model.DeleteProcessPortfolioItems;
import eu.europa.ec.cc.processcentre.process.command.repository.model.DeleteProcessQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.DeleteProcessStateQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.DeleteProcessVariableQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.InsertOrUpdateProcessVariableQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.InsertProcessActionLogQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.InsertProcessPortfolioItems;
import eu.europa.ec.cc.processcentre.process.command.repository.model.InsertProcessQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.InsertProcessStateQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.UpdateBusinessStatusQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.UpdateProcessResponsibleOrganisationQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.UpdateProcessResponsibleUserQueryParam;
import eu.europa.ec.cc.processcentre.task.repository.TaskMapper;
import eu.europa.ec.cc.processcentre.template.TemplateConverter;
import eu.europa.ec.cc.processcentre.template.TemplateModel;
import eu.europa.ec.cc.processcentre.template.TemplateModel.Model;
import eu.europa.ec.cc.processcentre.template.TemplateModel.ModelGroup;
import eu.europa.ec.cc.processcentre.translation.TranslationService;
import eu.europa.ec.cc.processcentre.util.Context;
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
import eu.europa.ec.cc.provider.proto.ProcessStateChanged;
import eu.europa.ec.cc.provider.proto.ProcessStateChanged.Change;
import eu.europa.ec.cc.provider.proto.ProcessVariableUpdated;
import eu.europa.ec.cc.provider.proto.ProcessVariablesUpdated;
import eu.europa.ec.cc.variables.proto.VariableValue;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class IngestionService {

  private static final String MIME_TYPE_JSON = "application/json";

  private final ProcessMapper processMapper;
  private final EventConverter eventConverter;
  private final ApplicationEventPublisher eventPublisher;
  private final TaskMapper taskMapper;
  private final TranslationService translationService;
  private final ConfigService configService;
  private final TemplateConverter templateConverter;
  private final ObjectMapper objectMapper;

  public IngestionService(ProcessMapper processMapper,
      EventConverter eventConverter,
      ApplicationEventPublisher eventPublisher, TaskMapper taskMapper,
      TranslationService translationService,
      ConfigService configService,
      TemplateConverter templateConverter,
      ObjectMapper objectMapper) {
    this.processMapper = processMapper;
    this.eventConverter = eventConverter;
    this.eventPublisher = eventPublisher;
    this.taskMapper = taskMapper;
    this.translationService = translationService;
    this.configService = configService;
    this.templateConverter = templateConverter;
    this.objectMapper = objectMapper;
  }

  @Transactional
  public void handle(ProcessCreated event) {

    if (LOG.isDebugEnabled()){
      LOG.debug("Handling ProcessCreated for process {}", event.getProcessInstanceId());
    }

    Map<String, String> context = Context.context(event.getProviderId(), event.getDomainKey(), event.getProcessTypeKey());

    // load the process type config
    ProcessTypeConfig processTypeConfig = configService.fetchProcessTypeConfig(context);

    // load the process result card config, not mandatory
    String resultCardLayoutConfig = configService.fetchResultCardLayoutConfig(context);

    List<AccessRight> viewAccessRights = processTypeConfig.accessRights().getOrDefault(Right.VIEW,
        Collections.emptyList());

    AccessRight resolvedAccessRight = null;
    if (viewAccessRights.isEmpty()) {
      resolvedAccessRight = new AccessRight(processTypeConfig.secundaTask(), AccessRight.PROCESS_CENTRE, null, null, null);
    } else {
      AccessRight viewAccessRight = viewAccessRights.getFirst();

      // to define access rights we support process variables and
      TemplateModel model = new TemplateModel();
      model.addProcessVariables(event.getProcessVariablesMap());
      model.add(Model.RESPONSIBLE, event.getResponsibleOrganisationId());
      // transform to string and parse the template back, we consider we can use process variables and responsible org in the model
      try {
        String convert = templateConverter.convert(event.getProcessInstanceId(),
            objectMapper.writeValueAsString(viewAccessRight), model);

        // parse it back to pojo
        resolvedAccessRight = objectMapper.readValue(convert, AccessRight.class);

      } catch (JsonProcessingException e){
        LOG.error("Error while creating access rights", e);
      }
    }

    InsertProcessQueryParam insertProcessQueryParam = eventConverter.toInsertProcessQueryParam(event, resolvedAccessRight);
    processMapper.insertOrUpdateProcess(insertProcessQueryParam);

    if (LOG.isDebugEnabled()){
      LOG.debug("Process {} persisted", event.getProcessInstanceId());
    }

    // store process variables
    updateProcessVariables(event.getProcessInstanceId(), event.getProcessVariablesMap());

    if (LOG.isDebugEnabled()){
      LOG.debug("Process variables for process {} persisted, sending ProcessRegistered", event.getProcessInstanceId());
    }

    // store new action
    processMapper.insertProcessActionLog(
        new InsertProcessActionLogQueryParam(
            event.getProcessInstanceId(),
            ProcessAction.START,
            ProtoUtils.timestampToInstant(event.getCreatedOn()),
            event.getUserId(),
            event.getOnBehalfOfUserId()
        )
    );

    if (!event.getAssociatedPortfolioItemIdsList().isEmpty()) {
      InsertProcessPortfolioItems insertProcessPortfolioItem = eventConverter.toInsertProcessPortfolioItem(
          ProcessAssociatedPortfolioItemAdded.newBuilder()
              .setProcessInstanceId(event.getProcessInstanceId())
              .addAllPortfolioItemIds(event.getAssociatedPortfolioItemIdsList())
              .build()
      );
      processMapper.insertProcessPortfolioItems(insertProcessPortfolioItem);
    }

    eventPublisher.publishEvent(new ProcessRegistered(
        event.getProcessInstanceId(),
        event.getProviderId(),
        event.getDomainKey(),
        event.getProcessTypeKey(),
        event.getUserId(),
        event.getOnBehalfOfUserId(),
        ProtoUtils.timestampToInstant(event.getCreatedOn())
    ));
  }

  @Transactional
  public void handle(ProcessVariablesUpdated event){
    if (LOG.isDebugEnabled()){
      LOG.debug("Handling ProcessVariablesUpdated for process {}", event.getProcessId());
    }

    // store process variables
    updateProcessVariables(event.getProcessId(), event.getProcessVariablesMap());
  }

  @Transactional
  public void handle(ProcessVariableUpdated event){
    if (LOG.isDebugEnabled()){
      LOG.debug("Handling ProcessVariableUpdated for process {}", event.getProcessId());
    }

    // store process variables
    updateProcessVariables(event.getProcessId(), Map.of(event.getName(), event.getValue()));
  }

  @Transactional
  public void handle(ProcessCancelled event){
    if (LOG.isDebugEnabled()){
      LOG.debug("Handling ProcessCancelled for process {}", event.getProcessInstanceId());
    }

    InsertProcessActionLogQueryParam changeProcessRunningStatusQueryParam = eventConverter.toInsertProcessRunningStatusQueryParam(event);
    processMapper.insertProcessActionLog(changeProcessRunningStatusQueryParam);
  }

  @Transactional
  public void handle(ProcessAssociatedPortfolioItemAdded event){
    if (LOG.isDebugEnabled()){
      LOG.debug("Handling ProcessAssociatedPortfolioItemAdded for process {}", event.getProcessInstanceId());
    }

    if (event.getPortfolioItemIdsList().isEmpty()){
      return;
    }

    InsertProcessPortfolioItems insertProcessPortfolioItems = eventConverter.toInsertProcessPortfolioItem(event);
    processMapper.insertProcessPortfolioItems(insertProcessPortfolioItems);
  }

  @Transactional
  public void handle(ProcessAssociatedPortfolioItemRemoved event){
    if (LOG.isDebugEnabled()){
      LOG.debug("Handling ProcessAssociatedPortfolioItemRemoved for process {}", event.getProcessInstanceId());
    }

    if (event.getPortfolioItemIdsList().isEmpty()){
      return;
    }

    DeleteProcessPortfolioItems deleteProcessPortfolioItems = eventConverter.toDeleteProcessPortfolioItem(event);
    processMapper.deleteProcessPortfolioItems(deleteProcessPortfolioItems);
  }

  @Transactional
  public void handle(ProcessResponsibleUserChanged event){
    if (LOG.isDebugEnabled()){
      LOG.debug("Handling ProcessResponsibleUserChanged for process {}", event.getProcessInstanceId());
    }

    UpdateProcessResponsibleUserQueryParam updateProcessResponsibleUserQueryParam = eventConverter.toUpdateProcessResponsibleUserQueryParam(event);
    processMapper.updateResponsibleUser(updateProcessResponsibleUserQueryParam);
  }

  @Transactional
  public void handle(ProcessResponsibleOrganisationChanged event){
    if (LOG.isDebugEnabled()){
      LOG.debug("Handling ProcessResponsibleOrganisationChanged for process {}", event.getProcessInstanceId());
    }

    UpdateProcessResponsibleOrganisationQueryParam updateProcessResponsibleOrganisationQueryParam =
        eventConverter.toUpdateProcessResponsibleOrganisationQueryParam(event);
    processMapper.updateResponsibleOrganisation(updateProcessResponsibleOrganisationQueryParam);
  }

  /**
   * For the ProcessRestored use case, we delete the process along with all its linked objects
   * (variables, portfolio items, states, running action log...)
   * And we recreate a brand new one
   * @param event
   */
  @Transactional
  public void handle(ProcessRestored event){
    if (LOG.isDebugEnabled()){
      LOG.debug("Handling ProcessRestored for process {}", event.getProcessInstanceId());
    }

    Map<String, String> context = Context.context(event.getProviderId(), event.getDomainKey(), event.getProcessTypeKey());
    Context.requireValidContext(context);

    // delete a process, all linked objects will be removed in cascade
    processMapper.deleteProcess(new DeleteProcessQueryParam(event.getProcessInstanceId()));

    InsertProcessQueryParam updateProcessQueryParam = eventConverter.toInsertProcessQueryParam(event, null);
    processMapper.insertOrUpdateProcess(updateProcessQueryParam);

    if (LOG.isDebugEnabled()){
      LOG.debug("Process {} persisted", event.getProcessInstanceId());
    }

    // store process variables
    updateProcessVariables(event.getProcessInstanceId(), event.getProcessVariablesMap());

    if (LOG.isDebugEnabled()){
      LOG.debug("Process variables for process {} persisted, sending ProcessRegistered", event.getProcessInstanceId());
    }

    // store ongoing running action, and then potentially other statuses
    processMapper.insertProcessActionLog(
        new InsertProcessActionLogQueryParam(
            event.getProcessInstanceId(),
            ProcessAction.START,
            ProtoUtils.timestampToInstant(event.getCreatedOn()),
            event.getUserId(),
            event.getOnBehalfOfUserId()
        )
    );

    // process has been paused
    if (event.getPausedOn().getSeconds() > 0){
      processMapper.insertProcessActionLog(
          new InsertProcessActionLogQueryParam(
              event.getProcessInstanceId(),
              ProcessAction.PAUSE,
              ProtoUtils.timestampToInstant(event.getPausedOn()),
              event.getUserId(),
              event.getOnBehalfOfUserId()
          )
      );
    }

    // process has been restarted
    if (event.getRestartedOn().getSeconds() > 0){
      processMapper.insertProcessActionLog(
          new InsertProcessActionLogQueryParam(
              event.getProcessInstanceId(),
              ProcessAction.START,
              ProtoUtils.timestampToInstant(event.getCompletedOn()),
              event.getUserId(),
              event.getOnBehalfOfUserId()
          )
      );
    }

    // process has been completed
    if (event.getCompletedOn().getSeconds() > 0){
      processMapper.insertProcessActionLog(
          new InsertProcessActionLogQueryParam(
              event.getProcessInstanceId(),
              ProcessAction.COMPLETE,
              ProtoUtils.timestampToInstant(event.getCompletedOn()),
              event.getUserId(),
              event.getOnBehalfOfUserId()
          )
      );
    }

    if (!event.getAssociatedPortfolioItemIdsList().isEmpty()) {
      InsertProcessPortfolioItems insertProcessPortfolioItem = eventConverter.toInsertProcessPortfolioItem(
          ProcessAssociatedPortfolioItemAdded.newBuilder()
              .setProcessInstanceId(event.getProcessInstanceId())
              .addAllPortfolioItemIds(event.getAssociatedPortfolioItemIdsList())
              .build()
      );
      processMapper.insertProcessPortfolioItems(insertProcessPortfolioItem);
    }

    eventPublisher.publishEvent(new ProcessRegistered(
        event.getProcessInstanceId(),
        event.getProviderId(),
        event.getDomainKey(),
        event.getProcessTypeKey(),
        event.getUserId(),
        event.getOnBehalfOfUserId(),
        ProtoUtils.timestampToInstant(event.getCreatedOn())
    ));
  }

  @Transactional
  public void handle(ProcessRunningStatusChanged event){
    if (LOG.isDebugEnabled()){
      LOG.debug("Handling ProcessRunningStatusChanged for process {}", event.getProcessInstanceId());
    }

    InsertProcessActionLogQueryParam changeProcessRunningStatusQueryParam = eventConverter.toInsertProcessRunningStatusQueryParam(event);
    processMapper.insertProcessActionLog(changeProcessRunningStatusQueryParam);
  }

  @Transactional
  public void handle(ProcessBusinessStatusChanged event){
    if (LOG.isDebugEnabled()){
      LOG.debug("Handling ProcessBusinessStatusChanged for process {}", event.getProcessInstanceId());
    }

    UpdateBusinessStatusQueryParam updateBusinessStatusQueryParam = eventConverter.toUpdateBusinessStatus(event);
    processMapper.updateBusinessStatus(updateBusinessStatusQueryParam);
  }

  @Transactional
  public void handle(ProcessDeleted event){
    if (LOG.isDebugEnabled()){
      LOG.debug("Handling ProcessDeleted for process {}", event.getProcessInstanceId());
    }

    DeleteProcessQueryParam deleteProcessQueryParam = eventConverter.toDeleteProcessQueryParam(event);
    processMapper.deleteProcess(deleteProcessQueryParam);
  }

  @Transactional
  public void handle(ProcessStateChanged event){
    if (LOG.isDebugEnabled()){
      LOG.debug("Handling ProcessStateChanged for process {}", event.getProcessInstanceId());
    }

    if (event.getChange() == Change.ENTERING){
      // insert
      InsertProcessStateQueryParam insertProcessStateQueryParam = eventConverter.toInsertProcessStateQueryParam(event);
      processMapper.insertProcessState(insertProcessStateQueryParam);
    } else if (event.getChange() == Change.LEAVING){
      DeleteProcessStateQueryParam deleteProcessStateQueryParam = eventConverter.toDeleteProcessStateQueryParam(event);
      processMapper.deleteProcessState(deleteProcessStateQueryParam);
    }
  }

  private void updateProcessVariables(String processInstanceId, Map<String, VariableValue> variables) {
    Set<InsertOrUpdateProcessVariableQueryParam> variablesToInsertOrUpdate = variables
        .entrySet().stream()
        .filter(entry -> !entry.getValue().getDeleted())
        .map(
            e -> buildInsertOrUpdateProcessVariableDto(
                processInstanceId,
                e.getKey(),
                e.getValue()
            )
        ).filter(Objects::nonNull).collect(Collectors.toSet());
    if (!variablesToInsertOrUpdate.isEmpty()) {
      processMapper.insertOrUpdateProcessVariables(variablesToInsertOrUpdate);
    }

    Set<DeleteProcessVariableQueryParam> variablesToDelete = variables
        .entrySet().stream()
        .filter(entry -> entry.getValue().getDeleted())
        .map(
            e -> new DeleteProcessVariableQueryParam(processInstanceId, e.getKey())
        ).collect(Collectors.toSet());

    if (!variablesToDelete.isEmpty()) {
      processMapper.deleteProcessVariables(variablesToDelete);
    }
  }

  private static InsertOrUpdateProcessVariableQueryParam buildInsertOrUpdateProcessVariableDto(
      String processInstanceId,
      String name,
      VariableValue value
  ){

    return switch (value.getKindCase()){
      case STRINGVALUE -> new InsertOrUpdateProcessVariableQueryParam(
          processInstanceId,
          name, null, value.getStringValue(), null, null,
          null, null, null, null, null
      );
      case BOOLEANVALUE -> new InsertOrUpdateProcessVariableQueryParam(
          processInstanceId,
          name, null, null, null, null,
          null, value.getBooleanValue(), null, null, null
      );
      case INTEGERVALUE -> new InsertOrUpdateProcessVariableQueryParam(
          processInstanceId,
          name, value.getIntegerValue(), null, null, null,
          null, null, null, null, null
      );
      case LONGVALUE -> new InsertOrUpdateProcessVariableQueryParam(
          processInstanceId,
          name, null, null, value.getLongValue(), null,
          null, null, null, null, null
      );
      case DOUBLEVALUE -> new InsertOrUpdateProcessVariableQueryParam(
          processInstanceId,
          name, null, null, null, value.getDoubleValue(),
          null, null, null, null, null
      );
      case TIMEVALUE -> new InsertOrUpdateProcessVariableQueryParam(
          processInstanceId,
          name, null, null, null, null,
          ProtoUtils.timestampToInstant(value.getTimeValue()), null, null, null, null
      );
      case BYTESVALUE -> {
        if (value.getMimeType().equals(MIME_TYPE_JSON)) {
          yield  new InsertOrUpdateProcessVariableQueryParam(
              processInstanceId,
              name, null, null, null, value.getDoubleValue(),
              null, null, new String(value.getBytesValue().toByteArray(), StandardCharsets.UTF_8), null, null
          );
        }
        yield new InsertOrUpdateProcessVariableQueryParam(
            processInstanceId,
            name, null, null, null, null,
            null, null, null, value.getMimeType(), value.toByteArray()
        );
      }
      case DELETED, KIND_NOT_SET -> null;
    };

  }

}
