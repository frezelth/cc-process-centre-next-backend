package eu.europa.ec.cc.processcentre.task.service;

import eu.europa.ec.cc.processcentre.babel.BabelText;
import eu.europa.ec.cc.processcentre.task.converter.TaskEventConverter;
import eu.europa.ec.cc.processcentre.task.model.TaskStatus;
import eu.europa.ec.cc.processcentre.task.repository.TaskMapper;
import eu.europa.ec.cc.processcentre.task.repository.model.ChangeStatusQueryParam;
import eu.europa.ec.cc.processcentre.task.repository.model.CreateTaskQueryParam;
import eu.europa.ec.cc.processcentre.translation.TranslationAttribute;
import eu.europa.ec.cc.processcentre.translation.TranslationObjectType;
import eu.europa.ec.cc.processcentre.translation.TranslationService;
import eu.europa.ec.cc.processcentre.util.ProtoUtils;
import eu.europa.ec.cc.provider.task.event.proto.TaskAssigned;
import eu.europa.ec.cc.provider.task.event.proto.TaskCancelled;
import eu.europa.ec.cc.provider.task.event.proto.TaskClaimed;
import eu.europa.ec.cc.provider.task.event.proto.TaskCompleted;
import eu.europa.ec.cc.provider.task.event.proto.TaskCreated;
import eu.europa.ec.cc.provider.task.event.proto.TaskDeleted;
import eu.europa.ec.cc.provider.task.event.proto.TaskUnclaimed;
import eu.europa.ec.cc.provider.task.event.proto.TaskUpdated;
import eu.europa.ec.cc.taskcenter.event.proto.TaskRegistered;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class IngestionTaskService {

  private final TaskMapper taskMapper;
  private final TaskEventConverter eventConverter;
  private final TranslationService translationService;

  public IngestionTaskService(TaskMapper taskMapper,
      TaskEventConverter eventConverter,
      TranslationService translationService) {
    this.taskMapper = taskMapper;
    this.eventConverter = eventConverter;
    this.translationService = translationService;
  }

  @Transactional
  public void handle(TaskCreated event){
    if (event.getProcessInstanceId().isEmpty()){
      // tasks without process, not handled inside process centre
      return;
    }

    if (LOG.isDebugEnabled()){
      LOG.debug("Handling TaskCreated for task {} and process {}", event.getTaskInstanceId(), event.getProcessInstanceId());
    }

    CreateTaskQueryParam param = eventConverter.toCreateTaskQueryParam(event);
    taskMapper.insertOrUpdateTask(param);

    translationService.insertOrUpdateTranslations(
        TranslationObjectType.TASK,
        event.getTaskInstanceId(),
        TranslationAttribute.TASK_TITLE,
        BabelText.convert(event.getTitle())
    );
  }

  @Transactional
  public void handle(TaskRegistered event){
    if (LOG.isDebugEnabled()){
      LOG.debug("Handling TaskRegistered for task {}", event.getTaskInstanceId());
    }

    translationService.insertOrUpdateTranslations(
        TranslationObjectType.TASK,
        event.getTaskInstanceId(),
        TranslationAttribute.TASK_TITLE,
        BabelText.convert(event.getTitle())
    );
  }

  @Transactional
  public void handle(TaskDeleted event){
    if (LOG.isDebugEnabled()){
      LOG.debug("Handling TaskDeleted for task {}", event.getTaskInstanceId());
    }

    taskMapper.deleteTask(event.getTaskInstanceId());

    translationService.deleteTranslations(
        TranslationObjectType.TASK,
        event.getTaskInstanceId(),
        null
    );
  }

  @Transactional
  public void handle(TaskCompleted event){
    if (LOG.isDebugEnabled()){
      LOG.debug("Handling TaskCompleted for task {}", event.getTaskInstanceId());
    }

    taskMapper.changeStatus(
        new ChangeStatusQueryParam(
            event.getTaskInstanceId(),
            TaskStatus.COMPLETED,
            ProtoUtils.timestampToInstant(event.getTimestamp()),
            event.getUserId(),
            event.getOnBehalfOfUserId()
        )
    );
  }

  @Transactional
  public void handle(TaskCancelled event){
    if (LOG.isDebugEnabled()){
      LOG.debug("Handling TaskCancelled for task {}", event.getTaskInstanceId());
    }

    taskMapper.changeStatus(
        new ChangeStatusQueryParam(
            event.getTaskInstanceId(),
            TaskStatus.CANCELLED,
            ProtoUtils.timestampToInstant(event.getTimestamp()),
            null,
            null
        )
    );
  }

  @Transactional
  public void handle(TaskAssigned event){
    if (LOG.isDebugEnabled()){
      LOG.debug("Handling TaskAssigned for task {}", event.getTaskInstanceId());
    }

    taskMapper.changeStatus(
        new ChangeStatusQueryParam(
            event.getTaskInstanceId(),
            TaskStatus.ASSIGNED,
            ProtoUtils.timestampToInstant(event.getTimestamp()),
            event.getClaimerUserId(),
            event.getClaimerOnBehalfOfUserId()
        )
    );
  }

  @Transactional
  public void handle(TaskClaimed event){
    if (LOG.isDebugEnabled()){
      LOG.debug("Handling TaskClaimed for task {}", event.getTaskInstanceId());
    }

    taskMapper.changeStatus(
        new ChangeStatusQueryParam(
            event.getTaskInstanceId(),
            TaskStatus.ASSIGNED,
            ProtoUtils.timestampToInstant(event.getTimestamp()),
            event.getUserId(),
            event.getOnBehalfOfUserId()
        )
    );
  }

  @Transactional
  public void handle(TaskUnclaimed event){
    if (LOG.isDebugEnabled()){
      LOG.debug("Handling TaskUnclaimed for task {}", event.getTaskInstanceId());
    }

    taskMapper.changeStatus(
        new ChangeStatusQueryParam(
            event.getTaskInstanceId(),
            TaskStatus.CREATED,
            ProtoUtils.timestampToInstant(event.getTimestamp()),
            null,
            null
        )
    );
  }

  @Transactional
  public void handle(TaskUpdated event){
    if (LOG.isDebugEnabled()){
      LOG.debug("Handling TaskUpdated for task {}", event.getTaskInstanceId());
    }


  }
}
