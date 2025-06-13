package eu.europa.ec.cc.processcentre.process.command.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.cc.processcentre.config.AccessRight;
import eu.europa.ec.cc.processcentre.config.AccessRight.Right;
import eu.europa.ec.cc.processcentre.config.ProcessTypeConfig;
import eu.europa.ec.cc.processcentre.event.ProcessModelChanged;
import eu.europa.ec.cc.processcentre.event.ProcessVariablesUpdated;
import eu.europa.ec.cc.processcentre.exception.NotFoundException;
import eu.europa.ec.cc.processcentre.process.command.repository.ProcessMapper;
import eu.europa.ec.cc.processcentre.process.command.repository.model.FindProcessByIdQueryResponse;
import eu.europa.ec.cc.processcentre.process.command.repository.model.FindProcessByIdQueryResponseTranslation;
import eu.europa.ec.cc.processcentre.process.command.repository.model.FindProcessConfigByIdQueryResponse;
import eu.europa.ec.cc.processcentre.process.command.repository.model.FindProcessVariableQueryResponse;
import eu.europa.ec.cc.processcentre.template.TemplateConverter;
import eu.europa.ec.cc.processcentre.template.TemplateExtractor;
import eu.europa.ec.cc.processcentre.translation.TranslationAttribute;
import eu.europa.ec.cc.processcentre.translation.TranslationObjectType;
import eu.europa.ec.cc.processcentre.translation.repository.DeleteTranslationsParam;
import eu.europa.ec.cc.processcentre.translation.repository.InsertOrUpdateTranslationsParam;
import eu.europa.ec.cc.processcentre.translation.repository.TranslationMapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import java.io.Serializable;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

@Service
public class ProcessConfigService {

  private final TranslationMapper translationMapper;
  private final ProcessMapper processMapper;
  private final TemplateConverter templateConverter;
  private final ObjectMapper objectMapper;

  public ProcessConfigService(
      TranslationMapper translationMapper,
      ProcessMapper processMapper,
      TemplateConverter templateConverter,
      ObjectMapper objectMapper) {
    this.translationMapper = translationMapper;
    this.processMapper = processMapper;
    this.templateConverter = templateConverter;
    this.objectMapper = objectMapper;
  }

  @TransactionalEventListener
  @Transactional
  @SneakyThrows
  public void handle(ProcessVariablesUpdated event){
    FindProcessConfigByIdQueryResponse processConfigById = processMapper.findProcessConfigById(
        event.processInstanceId()).orElseThrow();

    ProcessTypeConfig config = objectMapper.readValue(processConfigById.type(), ProcessTypeConfig.class);

    // check for places of the config where variables might be used
    Boolean accessRightsMatch = config.accessRights().get(Right.VIEW).stream().findFirst().map(
        accessRight -> accessRightMatch(accessRight, event.names())
    ).orElse(Boolean.FALSE);
  }

  private boolean accessRightMatch(AccessRight viewRights, Set<String> variables) {
    return TemplateExtractor.matchAny(viewRights.organisationId(), variables) ||
        TemplateExtractor.matchAny(viewRights.scopeId(), variables);
  }

  @Transactional
  @SneakyThrows
  public void updateStoredConfiguration(String processInstanceId) {
    FindProcessConfigByIdQueryResponse processConfigById = processMapper.findProcessConfigById(
        processInstanceId).orElseThrow();

    FindProcessByIdQueryResponse process = processMapper.findById(processInstanceId)
        .orElseThrow(NotFoundException::new);
    updateTitle(process);
  }

  @SneakyThrows
  private void updateResultCard(FindProcessByIdQueryResponse findProcessByIdQueryResponse){

  }

  @SneakyThrows
  private void updateTitle(FindProcessByIdQueryResponse findProcessByIdQueryResponse) {
    Map<TranslationAttribute, List<FindProcessByIdQueryResponseTranslation>> allTranslations =
        findProcessByIdQueryResponse.getTranslations().stream()
            .collect(Collectors.groupingBy(
                FindProcessByIdQueryResponseTranslation::getAttribute
            ));

    Map<String, String> existingProcessTitle = new HashMap<>();

    if (allTranslations
        .get(TranslationAttribute.PROCESS_TITLE) != null) {
      existingProcessTitle = allTranslations
          .get(TranslationAttribute.PROCESS_TITLE).stream()
          .collect(Collectors.toMap(
              FindProcessByIdQueryResponseTranslation::getLanguageCode,
              FindProcessByIdQueryResponseTranslation::getText
          ));
    }

//    List<FindProcessByIdQueryResponseTranslation> titleTemplates = allTranslations.get(
//        TranslationAttribute.PROCESS_TITLE_TEMPLATE);
    List<FindProcessByIdQueryResponseTranslation> typeNames = allTranslations.get(
        TranslationAttribute.PROCESS_TYPE_NAME);

//    if (titleTemplates != null && !titleTemplates.isEmpty()) {
//      // case for titleTemplate
//      // process variables in title template
//      Map<String, String> newTitle = new HashMap<>();
//      for (FindProcessByIdQueryResponseTranslation titleTemplate : titleTemplates) {
//        Map<String, Map<String, Serializable>> model = Map.of("processVariables",
//            findProcessByIdQueryResponse.getVariables().stream()
//                .collect(Collectors.toMap(FindProcessVariableQueryResponse::getName,
//                    FindProcessVariableQueryResponse::getVariableValue)));
//        String computedTitle = FreeMarkerTemplateUtils.processTemplateIntoString(
//                new Template(null, new StringReader(titleTemplate.getText()),
//                        new Configuration(Configuration.VERSION_2_3_34)),
//            model
//        );
//        newTitle.put(titleTemplate.getLanguageCode(), computedTitle);
//      }
//
//      if (!existingProcessTitle.equals(newTitle)) {
//        updateTitle(findProcessByIdQueryResponse.getProcessInstanceId(), titleTemplates, newTitle);
//      }
//    } else if (typeNames != null && !typeNames.isEmpty()) {
//      // case for process type name
//      Map<String, String> currentTypeNames = typeNames.stream()
//          .collect(Collectors.toMap(FindProcessByIdQueryResponseTranslation::getLanguageCode,
//              FindProcessByIdQueryResponseTranslation::getText));
//      if (!currentTypeNames.equals(existingProcessTitle)) {
//        updateTitle(findProcessByIdQueryResponse.getProcessInstanceId(), typeNames, currentTypeNames);
//      }
//    }
  }

  private void updateTitle(String processInstanceId,
      List<FindProcessByIdQueryResponseTranslation> titleTemplates, Map<String, String> newTitle) {
    // need to update the title
    translationMapper.deleteTranslationsForAttribute(new DeleteTranslationsParam(TranslationObjectType.PROCESS,
        processInstanceId, TranslationAttribute.PROCESS_TITLE));
    Set<InsertOrUpdateTranslationsParam> params = titleTemplates.stream()
        .map(t -> new InsertOrUpdateTranslationsParam(
            TranslationObjectType.PROCESS, processInstanceId,
            TranslationAttribute.PROCESS_TITLE,
            t.getLanguageCode(), newTitle.get(t.getLanguageCode()), t.getIsDefault()
        )).collect(Collectors.toSet());
    translationMapper.insertOrUpdateTranslations(params);
  }

}
