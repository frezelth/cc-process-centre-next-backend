package eu.europa.ec.cc.processcentre.task.service;

import eu.europa.ec.cc.processcentre.babel.BabelText;
import eu.europa.ec.cc.processcentre.task.converter.TaskEventConverter;
import eu.europa.ec.cc.processcentre.task.repository.TaskMapper;
import eu.europa.ec.cc.processcentre.task.repository.model.CreateTaskQueryParam;
import eu.europa.ec.cc.processcentre.translation.TranslationAttribute;
import eu.europa.ec.cc.processcentre.translation.TranslationObjectType;
import eu.europa.ec.cc.processcentre.translation.TranslationService;
import eu.europa.ec.cc.provider.task.event.proto.TaskCreated;
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
    if (LOG.isDebugEnabled()){
      LOG.debug("Handling TaskCreated for process {}", event.getProcessInstanceId());
    }

    if (event.getProcessInstanceId().isEmpty()){
      // tasks without process, not handled inside process centre
      return;
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
      LOG.debug("Handling TaskRegistered for process {}", event.getProcessInstanceId());
    }
  }
}
