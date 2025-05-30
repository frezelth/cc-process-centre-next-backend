package eu.europa.ec.cc.processcentre.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.cc.configuration.domain.ConfigurationSet;
import eu.europa.ec.cc.configuration.service.DomainConfigService;
import eu.europa.ec.cc.configuration.service.MergeStrategy;
import eu.europa.ec.cc.configuration.service.SearchStrategy;
import eu.europa.ec.cc.processcentre.config.ProcessTypeConfig;
import eu.europa.ec.cc.processcentre.event.ProcessRegistered;
import eu.europa.ec.cc.processcentre.mapper.CommandConverter;
import eu.europa.ec.cc.processcentre.proto.UpdateProcess;
import eu.europa.ec.cc.processcentre.proto.UpdateProcessContext;
import eu.europa.ec.cc.processcentre.proto.UpdateProcessVariables;
import eu.europa.ec.cc.processcentre.repository.ProcessMapper;
import eu.europa.ec.cc.processcentre.repository.model.CreateProcessQueryParam;
import eu.europa.ec.cc.processcentre.util.Context;
import eu.europa.ec.cc.processcentre.util.ProtoUtils;
import java.time.Instant;
import java.util.Map;
import lombok.SneakyThrows;
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
  private final CommandConverter commandConverter;
  private final ProcessSecurityService processSecurityService;

  private final DomainConfigService domainConfigService;

  private final ObjectMapper objectMapper;

  public ProcessService(
      ApplicationEventPublisher eventPublisher,
      ProcessMapper processMapper, ProcessVariableService processVariableService, CommandConverter commandConverter,
      DomainConfigService domainConfigService,
      ProcessSecurityService processSecurityService,
      ObjectMapper objectMapper) {
    this.eventPublisher = eventPublisher;
    this.processMapper = processMapper;
    this.processVariableService = processVariableService;
    this.commandConverter = commandConverter;
    this.domainConfigService = domainConfigService;
    this.processSecurityService = processSecurityService;
    this.objectMapper = objectMapper;
  }

  @Transactional
  public void createProcess(UpdateProcess command) {

    if (LOG.isDebugEnabled()){
      LOG.debug("Handling createProcess for process {}", command.getProcessInstanceId());
    }

    Instant startedOn = ProtoUtils.timestampToInstant(command.getCreatedOn());
    if (startedOn == null) {
      startedOn = Instant.now();
    }

    CreateProcessQueryParam createProcessQueryParam = commandConverter.toQueryParam(command, startedOn);
    processMapper.insertOrUpdateProcess(createProcessQueryParam);

    if (LOG.isDebugEnabled()){
      LOG.debug("Process {} persisted", command.getProcessInstanceId());
    }

    // handle process variables, create the dedicated command
    UpdateProcessVariables updateVariablesCommand = UpdateProcessVariables.newBuilder()
        .setProcessInstanceId(command.getProcessInstanceId())
        .putAllProcessVariables(command.getProcessVariablesMap())
        .build();
    processVariableService.updateVariables(updateVariablesCommand);

    if (LOG.isDebugEnabled()){
      LOG.debug("Process variables for process {} persisted, sending ProcessRegistered", command.getProcessInstanceId());
    }

    eventPublisher.publishEvent(new ProcessRegistered(
        command.getProcessInstanceId(),
        command.getProviderId(),
        command.getDomainKey(),
        command.getProcessTypeKey(),
        command.getUserId(),
        command.getOnBehalfOfUserId(),
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

      processSecurityService.updateProcessSecurity(command.getProcessInstanceId(), processTypeConfig);
    }
  }

}
