package eu.europa.ec.cc.processcentre.process.command.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.cc.configuration.domain.ConfigurationSet;
import eu.europa.ec.cc.configuration.service.DomainConfigService;
import eu.europa.ec.cc.configuration.service.MergeStrategy;
import eu.europa.ec.cc.configuration.service.SearchStrategy;
import eu.europa.ec.cc.processcentre.config.ProcessTypeConfig;
import eu.europa.ec.cc.processcentre.config.service.ConfigService;
import eu.europa.ec.cc.processcentre.process.command.converter.EventConverter;
import eu.europa.ec.cc.processcentre.process.command.repository.model.FindProcessByIdQueryResponse;
import eu.europa.ec.cc.processcentre.process.command.repository.model.FindProcessConfigByIdQueryResponse;
import eu.europa.ec.cc.processcentre.process.command.repository.model.InsertOrUpdateProcessConfigQueryParam;
import eu.europa.ec.cc.processcentre.proto.UpdateProcessContext;
import eu.europa.ec.cc.processcentre.process.command.repository.ProcessMapper;
import eu.europa.ec.cc.processcentre.translation.TranslationAttribute;
import eu.europa.ec.cc.processcentre.translation.TranslationObjectType;
import eu.europa.ec.cc.processcentre.translation.TranslationQuery;
import eu.europa.ec.cc.processcentre.translation.TranslationService;
import eu.europa.ec.cc.processcentre.util.Context;
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
  private final EventConverter eventConverter;
  private final ProcessSecurityService processSecurityService;

  private final ConfigService configService;

  private final ObjectMapper objectMapper;
  private final TranslationService translationService;
  private final ProcessConfigService processConfigService;

  public ProcessService(
      ApplicationEventPublisher eventPublisher,
      ProcessMapper processMapper, EventConverter eventConverter,
      ConfigService configService,
      ProcessSecurityService processSecurityService,
      ObjectMapper objectMapper, TranslationService translationService,
      TranslationQuery translationQuery, ProcessConfigService processConfigService) {
    this.eventPublisher = eventPublisher;
    this.processMapper = processMapper;
    this.eventConverter = eventConverter;
    this.configService = configService;
    this.processSecurityService = processSecurityService;
    this.objectMapper = objectMapper;
    this.translationService = translationService;
    this.processConfigService = processConfigService;
  }

  /**
   * This method is used to load the process configuration and update all configuration
   * aspects that are stored locally (access rights, title, description, type name...)
   */
  @Transactional
  @SneakyThrows
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

      ProcessTypeConfig processTypeConfig = configService.fetchProcessTypeConfig(context);
      String processResultConfig = configService.fetchResultCardLayoutConfig(context);

      processMapper.insertOrUpdateProcessConfig(
          new InsertOrUpdateProcessConfigQueryParam(
              command.getProcessInstanceId(),
              objectMapper.writeValueAsString(processTypeConfig),
              processResultConfig
          )
      );

      // initialize correctly the access rights, title template and process result card
      FindProcessByIdQueryResponse process = processMapper.findById(
          command.getProcessInstanceId()).orElseThrow();

      // handle access rights


      // insert security rules
      processSecurityService.updateProcessSecurity(command.getProcessInstanceId(), processTypeConfig);

      // create translations for the type name
      translationService.insertOrUpdateTranslations(TranslationObjectType.PROCESS,
          command.getProcessInstanceId(), TranslationAttribute.PROCESS_TYPE_NAME,
          processTypeConfig.name());

      // create translations for the title template
//      translationService.insertOrUpdateTranslations(TranslationObjectType.PROCESS,
//          command.getProcessInstanceId(), TranslationAttribute.PROCESS_TITLE_TEMPLATE,
//          processTypeConfig.titleTemplate());
    }

    // insert or update the title template if necessary
    processConfigService.updateStoredConfiguration(command.getProcessInstanceId());

  }

}
