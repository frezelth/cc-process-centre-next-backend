package eu.europa.ec.cc.processcentre.process.command.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.cc.configadmin.event.proto.RefreshConfiguration;
import eu.europa.ec.cc.processcentre.babel.BabelText;
import eu.europa.ec.cc.processcentre.config.AccessRight;
import eu.europa.ec.cc.processcentre.config.AccessRight.Right;
import eu.europa.ec.cc.processcentre.config.ProcessTypeConfig;
import eu.europa.ec.cc.processcentre.config.service.ConfigService;
import eu.europa.ec.cc.processcentre.event.ProcessModelChanged;
import eu.europa.ec.cc.processcentre.event.ProcessRegistered;
import eu.europa.ec.cc.processcentre.event.ProcessVariablesChanged;
import eu.europa.ec.cc.processcentre.exception.NotFoundException;
import eu.europa.ec.cc.processcentre.process.command.repository.ProcessMapper;
import eu.europa.ec.cc.processcentre.process.command.repository.model.FindProcessByIdQueryResponse;
import eu.europa.ec.cc.processcentre.process.command.repository.model.FindProcessByIdQueryResponseTranslation;
import eu.europa.ec.cc.processcentre.process.command.repository.model.FindProcessConfigByIdQueryResponse;
import eu.europa.ec.cc.processcentre.process.command.repository.model.InsertOrUpdateProcessConfigQueryParam;
import eu.europa.ec.cc.processcentre.process.command.repository.model.UpdateResolvedConfigQueryParam;
import eu.europa.ec.cc.processcentre.template.TemplateConverter;
import eu.europa.ec.cc.processcentre.template.TemplateExtractor;
import eu.europa.ec.cc.processcentre.template.TemplateModel;
import eu.europa.ec.cc.processcentre.translation.TranslationAttribute;
import eu.europa.ec.cc.processcentre.translation.TranslationObjectType;
import eu.europa.ec.cc.processcentre.translation.TranslationService;
import eu.europa.ec.cc.processcentre.translation.repository.DeleteTranslationsParam;
import eu.europa.ec.cc.processcentre.translation.repository.InsertOrUpdateTranslationsParam;
import eu.europa.ec.cc.processcentre.translation.repository.TranslationMapper;
import eu.europa.ec.cc.processcentre.util.Context;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.commons.config.DefaultsBindHandlerAdvisor.MappingsProvider;
import org.springframework.context.event.EventListener;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.CollectionUtils;

@Service
@Slf4j
public class ProcessConfigService {

  private final TranslationMapper translationMapper;
  private final ProcessMapper processMapper;
  private final TemplateConverter templateConverter;
  private final ObjectMapper objectMapper;
  private final ConfigService configService;
  private final TranslationService translationService;

  public ProcessConfigService(
      TranslationMapper translationMapper,
      ProcessMapper processMapper,
      TemplateConverter templateConverter,
      ObjectMapper objectMapper, ConfigService configService,
      TranslationService translationService) {
    this.translationMapper = translationMapper;
    this.processMapper = processMapper;
    this.templateConverter = templateConverter;
    this.objectMapper = objectMapper;
    this.configService = configService;
    this.translationService = translationService;
  }

  @EventListener
  @SneakyThrows
  public void handle(ProcessRegistered event){

    Map<String, String> context = Context.context(event.providerId(), event.domainKey(), event.processTypeKey());
    // load the process type config
    ProcessTypeConfig processTypeConfig = configService.fetchProcessTypeConfig(context);

    if (processTypeConfig == null) {
      LOG.warn("No process type configuration found for context {}", context);
      return;
    }

    // load the process result card config, not mandatory
    String resultCardLayoutConfig = configService.fetchResultCardLayoutConfig(context);

    // create translations for the type name / process title, initially based on type name
    // it will be overridden later by the
    translationService.insertOrUpdateTranslations(TranslationObjectType.PROCESS,
        event.processInstanceId(), TranslationAttribute.PROCESS_TYPE_NAME,
        processTypeConfig.name());

    // create translations for the type name
    translationService.insertOrUpdateTranslations(TranslationObjectType.PROCESS,
        event.processInstanceId(), TranslationAttribute.PROCESS_TITLE,
        processTypeConfig.name());

    processMapper.insertOrUpdateProcessConfig(
        new InsertOrUpdateProcessConfigQueryParam(
            event.processInstanceId(), objectMapper.writeValueAsString(processTypeConfig), resultCardLayoutConfig)
    );

    persistResolvedConfiguration(event.processInstanceId(), processTypeConfig, resultCardLayoutConfig);
  }

  @EventListener
  @SneakyThrows
  public void handle(ProcessVariablesChanged event){
    Set<String> namesInTemplate = event.names().stream().map(name -> TemplateModel.PROCESS_VARIABLE_PREFIX + name)
        .collect(Collectors.toSet());
    handleModelChange(event.processInstanceId(), namesInTemplate);
  }

  @EventListener
  @SneakyThrows
  public void handle(ProcessModelChanged event){
    handleModelChange(event.processInstanceId(),
        event.changes().stream().map(change -> change.tag).collect(Collectors.toSet()));
  }

  private void handleModelChange(String processInstanceId, @Nullable Set<String> modelAttributesChanged) {
    processMapper.findProcessConfigById(
        processInstanceId).ifPresent(
        processConfigById -> {

          try {
            ProcessTypeConfig processTypeConfig = objectMapper.readValue(processConfigById.type(),
                ProcessTypeConfig.class);

            // try to see if the first VIEW access rights contains any changed variable
            boolean scopeIdMatches = Optional.ofNullable(processTypeConfig.accessRights())
                .orElse(Map.of())
                .getOrDefault(Right.VIEW, Collections.emptyList()).stream()
                .anyMatch(a -> TemplateExtractor.matchAny(a.scopeId(), modelAttributesChanged));

            boolean organisationIdMatches = Optional.ofNullable(processTypeConfig.accessRights())
                .orElse(Map.of())
                .getOrDefault(Right.VIEW, Collections.emptyList()).stream()
                .anyMatch(
                    a -> TemplateExtractor.matchAny(a.organisationId(), modelAttributesChanged));

            boolean titleTemplateMatches = Optional.ofNullable(processTypeConfig.titleTemplate())
                .map(t -> {
                  try {
                    return objectMapper.writeValueAsString(t);
                  } catch (JsonProcessingException e) {
                    return null;
                  }
                })
                .stream().anyMatch(titleAsString -> TemplateExtractor.matchAny(titleAsString,
                    modelAttributesChanged));

            boolean cardMatches = Optional.ofNullable(processConfigById.resultCard())
                .map(r -> TemplateExtractor.matchAny(r, modelAttributesChanged))
                .orElse(Boolean.FALSE);

            if (!scopeIdMatches && !organisationIdMatches && !titleTemplateMatches
                && !cardMatches) {
              // the variable did not change any stored configuration
              return;
            }

            persistResolvedConfiguration(processInstanceId, processTypeConfig,
                processConfigById.resultCard());
          } catch (JsonProcessingException e) {
            LOG.error("Error while persisting process configuration changes", e);
          }
        }
    );
  }

  private void persistResolvedConfiguration(String processInstanceId,
      ProcessTypeConfig processTypeConfig, String resultCardConfig) throws JsonProcessingException {
    FindProcessByIdQueryResponse process = processMapper.findById(processInstanceId).orElseThrow();
    TemplateModel model = new TemplateModel(process);

    String resolvedApplicationId = null;
    String resolvedScopeId = null;
    String resolvedOrganisationId = null;
    String resolvedScopeTypeId = null;
    String resolvedSecundaTaskId = null;
    if (processTypeConfig.accessRights() != null &&
        !CollectionUtils.isEmpty(processTypeConfig.accessRights().get(Right.VIEW))){
      resolvedScopeId = templateConverter.convert("scopeId_"+ processInstanceId,
          processTypeConfig.accessRights().get(Right.VIEW).getFirst().scopeId(), model);

      resolvedOrganisationId = templateConverter.convert("organisationId_"+ processInstanceId,
          processTypeConfig.accessRights().get(Right.VIEW).getFirst().organisationId(), model);

      resolvedApplicationId = processTypeConfig.accessRights().get(Right.VIEW).getFirst().applicationId() != null ?
          processTypeConfig.accessRights().get(Right.VIEW).getFirst().applicationId() : AccessRight.PROCESS_CENTRE;

      resolvedSecundaTaskId = processTypeConfig.accessRights().get(Right.VIEW).getFirst().permissionId();

      resolvedScopeTypeId = processTypeConfig.accessRights().get(Right.VIEW).getFirst().scopeTypeId();
    } else {
      resolvedScopeId = AccessRight.PROCESS_CENTRE;
      resolvedSecundaTaskId = processTypeConfig.secundaTask();
    }

    String resolvedTitleAsString = templateConverter.convert("titleTemplate_"+ processInstanceId,
        objectMapper.writeValueAsString(processTypeConfig.titleTemplate()), model);
    BabelText resolvedTitle = objectMapper.readValue(resolvedTitleAsString, BabelText.class);

    String resolvedCard = templateConverter.convert("cardLayout_"+ processInstanceId, resultCardConfig, model);

    // if needed update the title
    if (resolvedTitle != null){
      translationService.insertOrUpdateTranslations(TranslationObjectType.PROCESS,
          processInstanceId,
          TranslationAttribute.PROCESS_TITLE, resolvedTitle);
    }

    // update the rest of the config (security and result card)
    processMapper.updateResolvedConfig(
        new UpdateResolvedConfigQueryParam(
            processInstanceId,
            resolvedApplicationId,
            resolvedSecundaTaskId,
            resolvedScopeTypeId,
            resolvedScopeId,
            resolvedOrganisationId,
            resolvedCard
        )
    );
  }
}
