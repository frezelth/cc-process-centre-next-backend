package eu.europa.ec.cc.processcentre.process.command.service;

import eu.europa.ec.cc.processcentre.process.command.repository.ProcessMapper;
import eu.europa.ec.cc.processcentre.process.command.repository.model.FindProcessByIdQueryResponse;
import eu.europa.ec.cc.processcentre.process.command.repository.model.FindProcessByIdQueryResponseTranslation;
import eu.europa.ec.cc.processcentre.process.command.repository.model.FindProcessVariableQueryResponse;
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
import java.util.Set;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

@Service
public class ProcessTitleService {

  private final TranslationMapper translationMapper;
  private final ProcessMapper processMapper;

  public ProcessTitleService(
      TranslationMapper translationMapper,
      ProcessMapper processMapper) {
    this.translationMapper = translationMapper;
    this.processMapper = processMapper;
  }

  @Transactional
  @SneakyThrows
  public void updateProcessTitle(String processInstanceId) {
    processMapper.findById(processInstanceId).ifPresent(
        this::updateTitle
    );
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

    List<FindProcessByIdQueryResponseTranslation> titleTemplates = allTranslations.get(
        TranslationAttribute.PROCESS_TITLE_TEMPLATE);
    List<FindProcessByIdQueryResponseTranslation> typeNames = allTranslations.get(
        TranslationAttribute.PROCESS_TYPE_NAME);

    if (titleTemplates != null && !titleTemplates.isEmpty()) {
      // case for titleTemplate
      // process variables in title template
      Map<String, String> newTitle = new HashMap<>();
      for (FindProcessByIdQueryResponseTranslation titleTemplate : titleTemplates) {
        Map<String, Map<String, Serializable>> model = Map.of("processVariables",
            findProcessByIdQueryResponse.getVariables().stream()
                .collect(Collectors.toMap(FindProcessVariableQueryResponse::getName,
                    FindProcessVariableQueryResponse::getVariableValue)));
        String computedTitle = FreeMarkerTemplateUtils.processTemplateIntoString(
                new Template(null, new StringReader(titleTemplate.getText()),
                        new Configuration(Configuration.VERSION_2_3_34)),
            model
        );
        newTitle.put(titleTemplate.getLanguageCode(), computedTitle);
      }

      if (!existingProcessTitle.equals(newTitle)) {
        updateTitle(findProcessByIdQueryResponse.getProcessInstanceId(), titleTemplates, newTitle);
      }
    } else if (typeNames != null && !typeNames.isEmpty()) {
      // case for process type name
      Map<String, String> currentTypeNames = typeNames.stream()
          .collect(Collectors.toMap(FindProcessByIdQueryResponseTranslation::getLanguageCode,
              FindProcessByIdQueryResponseTranslation::getText));
      if (!currentTypeNames.equals(existingProcessTitle)) {
        updateTitle(findProcessByIdQueryResponse.getProcessInstanceId(), typeNames, currentTypeNames);
      }
    }
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
