package eu.europa.ec.cc.processcentre.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.cc.configuration.domain.ConfigurationSet;
import eu.europa.ec.cc.configuration.service.DomainConfigService;
import eu.europa.ec.cc.configuration.service.MergeStrategy;
import eu.europa.ec.cc.configuration.service.SearchStrategy;
import eu.europa.ec.cc.processcentre.config.ProcessTypeConfig;
import eu.europa.ec.cc.processcentre.event.ProcessRegistered;
import eu.europa.ec.cc.processcentre.mapper.EventConverter;
import eu.europa.ec.cc.processcentre.proto.UpdateProcessContext;
import eu.europa.ec.cc.processcentre.proto.UpdateProcessVariables;
import eu.europa.ec.cc.processcentre.repository.ProcessMapper;
import eu.europa.ec.cc.processcentre.repository.model.CreateProcessQueryParam;
import eu.europa.ec.cc.processcentre.translation.TranslationAttribute;
import eu.europa.ec.cc.processcentre.translation.TranslationObjectType;
import eu.europa.ec.cc.processcentre.translation.TranslationQuery;
import eu.europa.ec.cc.processcentre.translation.TranslationService;
import eu.europa.ec.cc.processcentre.util.Context;
import eu.europa.ec.cc.processcentre.util.ProtoUtils;
import eu.europa.ec.cc.provider.proto.ProcessCreated;
import java.time.Instant;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class ProcessService {

  private final ApplicationEventPublisher eventPublisher;

  private final ProcessMapper processMapper;
  private final ProcessVariableService processVariableService;
  private final EventConverter eventConverter;
  private final ProcessSecurityService processSecurityService;

  private final DomainConfigService domainConfigService;

  private final ObjectMapper objectMapper;
  private final TranslationService translationService;

  private final TranslationQuery translationQuery;

  public ProcessService(
      ApplicationEventPublisher eventPublisher,
      ProcessMapper processMapper, ProcessVariableService processVariableService, EventConverter eventConverter,
      DomainConfigService domainConfigService,
      ProcessSecurityService processSecurityService,
      ObjectMapper objectMapper, TranslationService translationService,
      TranslationQuery translationQuery) {
    this.eventPublisher = eventPublisher;
    this.processMapper = processMapper;
    this.processVariableService = processVariableService;
    this.eventConverter = eventConverter;
    this.domainConfigService = domainConfigService;
    this.processSecurityService = processSecurityService;
    this.objectMapper = objectMapper;
    this.translationService = translationService;
    this.translationQuery = translationQuery;
  }

  @Transactional
  public void handle(ProcessCreated event) {

    if (LOG.isDebugEnabled()){
      LOG.debug("Handling createProcess for process {}", event.getProcessInstanceId());
    }

    Instant startedOn = ProtoUtils.timestampToInstant(event.getCreatedOn());
    if (startedOn == null) {
      startedOn = Instant.now();
    }

    CreateProcessQueryParam createProcessQueryParam = eventConverter.toCreateProcessQueryParam(event, startedOn);
    processMapper.insertOrUpdateProcess(createProcessQueryParam);

    if (LOG.isDebugEnabled()){
      LOG.debug("Process {} persisted", event.getProcessInstanceId());
    }

    // handle process variables, create the dedicated command
    UpdateProcessVariables updateVariablesCommand = UpdateProcessVariables.newBuilder()
        .setProcessInstanceId(event.getProcessInstanceId())
        .putAllProcessVariables(event.getProcessVariablesMap())
        .build();
    processVariableService.updateVariables(updateVariablesCommand);

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
        startedOn
    ));
  }

  /**
   * This method is used to load the process configuration and update all configuration
   * aspects that are stored locally (access rights, title, description, type name...)
   */
  @Transactional
  public void updateProcessConfiguration(UpdateProcessContext command){

    // load process type to store translations and security,
    // can be handled in a subsequent message for better resilience
    final Map<String, String> context = Context.context(
        command.getProviderId(),
        command.getDomainKey(),
        command.getProcessTypeKey()
    );

    if (!Context.isValidContext(context)) {
      LOG.warn("Context is not valid for process {}, context is providerId={}, domainKey={}, processTypeKey={}", command.getProcessInstanceId(),
          command.getProviderId(), command.getDomainKey(), command.getProcessTypeKey());
    } else {

      ConfigurationSet processTypeConfigAsJson = domainConfigService.get(context, ProcessTypeConfig.CONFIGURATION_TYPE_NAME,
          SearchStrategy.BEST_MATCH,
          MergeStrategy.NO_MERGE_HIGHEST_SCORE_WINS);

      ProcessTypeConfig processTypeConfig;
      try {
        processTypeConfig = objectMapper.treeToValue(processTypeConfigAsJson.getContent(),
            ProcessTypeConfig.class);
      } catch (JsonProcessingException e) {
        LOG.warn("Cannot convert process configuration for process {} into json, config is {}",
            command.getProcessInstanceId(), processTypeConfigAsJson.getContent());
        return;
      }

      // insert security rules
      processSecurityService.updateProcessSecurity(command.getProcessInstanceId(), processTypeConfig);

      // create translations for the type name
      translationService.insertOrUpdateTranslations(TranslationObjectType.PROCESS,
          command.getProcessInstanceId(), TranslationAttribute.PROCESS_TYPE_NAME,
          processTypeConfig.name());

      // create translations for the title template
      translationService.insertOrUpdateTranslations(TranslationObjectType.PROCESS,
          command.getProcessInstanceId(), TranslationAttribute.PROCESS_TITLE_TEMPLATE,
          processTypeConfig.titleTemplate());
    }
  }

}
