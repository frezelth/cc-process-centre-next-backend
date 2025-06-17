package eu.europa.ec.cc.processcentre.servicetask.service;

import eu.europa.ec.cc.processcentre.babel.BabelText;
import eu.europa.ec.cc.processcentre.servicetask.converter.ServiceTaskEventConverter;
import eu.europa.ec.cc.processcentre.servicetask.model.ServiceTaskStatus;
import eu.europa.ec.cc.processcentre.servicetask.repository.ServiceTaskMapper;
import eu.europa.ec.cc.processcentre.servicetask.repository.model.ChangeStatusServiceTaskQueryParam;
import eu.europa.ec.cc.processcentre.servicetask.repository.model.CreateServiceTaskQueryParam;
import eu.europa.ec.cc.processcentre.translation.TranslationAttribute;
import eu.europa.ec.cc.processcentre.translation.TranslationObjectType;
import eu.europa.ec.cc.processcentre.translation.TranslationService;
import eu.europa.ec.cc.processcentre.util.ProtoUtils;
import eu.europa.ec.cc.provider.servicetask.event.proto.ServiceTaskCancelled;
import eu.europa.ec.cc.provider.servicetask.event.proto.ServiceTaskCompleteFailed;
import eu.europa.ec.cc.provider.servicetask.event.proto.ServiceTaskCompleteSucceeded;
import eu.europa.ec.cc.provider.servicetask.event.proto.ServiceTaskCreated;
import eu.europa.ec.cc.provider.servicetask.event.proto.ServiceTaskLocked;
import eu.europa.ec.cc.provider.servicetask.event.proto.ServiceTaskUnlocked;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class IngestionServiceTaskService {

  private final ServiceTaskMapper mapper;
  private final ServiceTaskEventConverter converter;
  private final TranslationService translationService;

  public IngestionServiceTaskService(ServiceTaskMapper mapper, ServiceTaskEventConverter converter,
      TranslationService translationService) {
    this.mapper = mapper;
    this.converter = converter;
    this.translationService = translationService;
  }

  @Transactional
  @EventListener
  public void handle(ServiceTaskCreated event){
    // only handle service tasks that have a process instance id
    if (StringUtils.isEmpty(event.getProcessInstanceId())){
      return;
    }

    if (LOG.isDebugEnabled()){
      LOG.debug("Handling ServiceTaskCreated for task {}", event.getServiceTaskId());
    }

    CreateServiceTaskQueryParam createServiceTaskQueryParam = this.converter.toCreateServiceTaskQueryParam(
        event);
    this.mapper.insertServiceTask(createServiceTaskQueryParam);

    this.translationService.insertOrUpdateTranslations(
        TranslationObjectType.SERVICE_TASK, event.getServiceTaskId(), TranslationAttribute.SERVICE_TASK_TITLE,
        BabelText.convert(event.getTitle())
    );
  }

  @Transactional
  @EventListener
  public void handle(ServiceTaskCompleteSucceeded event){

    if (LOG.isDebugEnabled()){
      LOG.debug("Handling ServiceTaskCompleteSucceeded for task {}", event.getServiceTaskId());
    }

    this.mapper.changeStatus(
        new ChangeStatusServiceTaskQueryParam(
            event.getServiceTaskId(), ServiceTaskStatus.COMPLETED,
            ProtoUtils.timestampToInstant(event.getTimestamp()), null, event.getInfoMessage())
    );
  }

  @Transactional
  @EventListener
  public void handle(ServiceTaskCompleteFailed event){
    if (event.getRetries() != 0){
      return;
    }

    if (LOG.isDebugEnabled()){
      LOG.debug("Handling ServiceTaskCompleteFailed for task {}", event.getServiceTaskId());
    }

    this.mapper.changeStatus(
        new ChangeStatusServiceTaskQueryParam(
            event.getServiceTaskId(), ServiceTaskStatus.FAILED,
            ProtoUtils.timestampToInstant(event.getTimestamp()), event.getErrorMessage(), null)
    );
  }

  @Transactional
  @EventListener
  public void handle(ServiceTaskCancelled event){

    if (LOG.isDebugEnabled()){
      LOG.debug("Handling ServiceTaskCancelled for task {}", event.getServiceTaskId());
    }

    this.mapper.changeStatus(
        new ChangeStatusServiceTaskQueryParam(
            event.getServiceTaskId(), ServiceTaskStatus.CANCELLED,
            ProtoUtils.timestampToInstant(event.getTimestamp()), null, null)
    );
  }

  @Transactional
  @EventListener
  public void handle(ServiceTaskLocked event){

    if (LOG.isDebugEnabled()){
      LOG.debug("Handling ServiceTaskLocked for task {}", event.getServiceTaskId());
    }

    this.mapper.changeStatus(
        new ChangeStatusServiceTaskQueryParam(
            event.getServiceTaskId(), ServiceTaskStatus.LOCKED,
            ProtoUtils.timestampToInstant(event.getTimestamp()), null, null)
    );
  }

  @Transactional
  @EventListener
  public void handle(ServiceTaskUnlocked event){

    if (LOG.isDebugEnabled()){
      LOG.debug("Handling ServiceTaskUnlocked for task {}", event.getServiceTaskId());
    }

    this.mapper.changeStatus(
        new ChangeStatusServiceTaskQueryParam(
            event.getServiceTaskId(), ServiceTaskStatus.CREATED,
            ProtoUtils.timestampToInstant(event.getTimestamp()), null, null)
    );
  }

}
