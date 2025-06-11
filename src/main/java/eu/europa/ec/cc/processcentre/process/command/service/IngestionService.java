package eu.europa.ec.cc.processcentre.process.command.service;

import eu.europa.ec.cc.processcentre.event.ProcessRegistered;
import eu.europa.ec.cc.processcentre.process.command.converter.EventConverter;
import eu.europa.ec.cc.processcentre.model.ProcessRunningStatus;
import eu.europa.ec.cc.processcentre.process.command.repository.ProcessMapper;
import eu.europa.ec.cc.processcentre.task.repository.TaskMapper;
import eu.europa.ec.cc.processcentre.process.command.repository.model.ChangeBusinessStatusQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.ChangeProcessStateQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.CreateProcessQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.DeleteProcessQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.DeleteProcessVariableQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.InsertOrUpdateProcessVariableQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.InsertProcessRunningStatusLogQueryParam;
import eu.europa.ec.cc.processcentre.translation.TranslationService;
import eu.europa.ec.cc.processcentre.util.Context;
import eu.europa.ec.cc.processcentre.util.ProtoUtils;
import eu.europa.ec.cc.provider.proto.ProcessAssociatedPortfolioItemAdded;
import eu.europa.ec.cc.provider.proto.ProcessBusinessStatusChanged;
import eu.europa.ec.cc.provider.proto.ProcessCancelled;
import eu.europa.ec.cc.provider.proto.ProcessCreated;
import eu.europa.ec.cc.provider.proto.ProcessDeleted;
import eu.europa.ec.cc.provider.proto.ProcessRunningStatusChanged;
import eu.europa.ec.cc.provider.proto.ProcessStateChanged;
import eu.europa.ec.cc.provider.proto.ProcessVariableUpdated;
import eu.europa.ec.cc.provider.proto.ProcessVariablesUpdated;
import eu.europa.ec.cc.variables.proto.VariableValue;
import java.nio.charset.StandardCharsets;
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

  public IngestionService(ProcessMapper processMapper,
      EventConverter eventConverter,
      ApplicationEventPublisher eventPublisher, TaskMapper taskMapper,
      TranslationService translationService) {
    this.processMapper = processMapper;
    this.eventConverter = eventConverter;
    this.eventPublisher = eventPublisher;
    this.taskMapper = taskMapper;
    this.translationService = translationService;
  }

  @Transactional
  public void handle(ProcessCreated event) {

    if (LOG.isDebugEnabled()){
      LOG.debug("Handling ProcessCreated for process {}", event.getProcessInstanceId());
    }

    Map<String, String> context = Context.context(event.getProviderId(), event.getDomainKey(), event.getProcessTypeKey());
    Context.requireValidContext(context);

    CreateProcessQueryParam createProcessQueryParam = eventConverter.toCreateProcessQueryParam(event);
    processMapper.insertOrUpdateProcess(createProcessQueryParam);

    if (LOG.isDebugEnabled()){
      LOG.debug("Process {} persisted", event.getProcessInstanceId());
    }

    // store process variables
    updateProcessVariables(event.getProcessInstanceId(), event.getProcessVariablesMap());

    // store new status
    processMapper.insertProcessRunningStatusLog(
        new InsertProcessRunningStatusLogQueryParam(
            event.getProcessInstanceId(),
            ProcessRunningStatus.ONGOING,
            ProtoUtils.timestampToInstant(event.getCreatedOn()),
            event.getUserId(),
            event.getOnBehalfOfUserId()
        )
    );

    if (LOG.isDebugEnabled()){
      LOG.debug("Process variables for process {} persisted, sending ProcessRegistered", event.getProcessInstanceId());
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

    InsertProcessRunningStatusLogQueryParam changeProcessRunningStatusQueryParam = eventConverter.toChangeProcessRunningStatusQueryParam(event);
    processMapper.insertProcessRunningStatusLog(changeProcessRunningStatusQueryParam);
  }

  @Transactional
  public void handle(ProcessAssociatedPortfolioItemAdded event){
    if (LOG.isDebugEnabled()){
      LOG.debug("Handling ProcessAssociatedPortfolioItemAdded for process {}", event.getProcessInstanceId());
    }

  }

  @Transactional
  public void handle(ProcessRunningStatusChanged event){
    if (LOG.isDebugEnabled()){
      LOG.debug("Handling ProcessRunningStatusChanged for process {}", event.getProcessInstanceId());
    }

    InsertProcessRunningStatusLogQueryParam changeProcessRunningStatusQueryParam = eventConverter.toChangeProcessRunningStatusQueryParam(event);
    processMapper.insertProcessRunningStatusLog(changeProcessRunningStatusQueryParam);
  }

  @Transactional
  public void handle(ProcessBusinessStatusChanged event){
    if (LOG.isDebugEnabled()){
      LOG.debug("Handling ProcessBusinessStatusChanged for process {}", event.getProcessInstanceId());
    }

    ChangeBusinessStatusQueryParam changeBusinessStatusQueryParam = eventConverter.toChangeBusinessStatus(event);
    processMapper.changeBusinessStatus(changeBusinessStatusQueryParam);
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

    ChangeProcessStateQueryParam changeProcessStateQueryParam = eventConverter.toChangeProcessStateQueryParam(event);
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
