package eu.europa.ec.cc.processcentre.template;

import eu.europa.ec.cc.processcentre.babel.BabelText;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Rafal Oleksiak
 *
 */
@Component
@Slf4j
public class ProcessTitleTemplateConverter {

  private TemplateConverter templateConverter;

  public ProcessTitleTemplateConverter() {
    TemplateEngine templateEngine =
        new TemplateEngine(TemplateEngine.getFreeMarkerConfiguration(false));

    templateConverter = new TemplateConverter(templateEngine);
  }

  /**
   * Generate BabelText title. Title has no {@code urn} because it's generated from titleTemplate. 
   * @param processId
   * @param template BabelText with templates in translations. 
   * @param name
   * @param description
   * @param templateModel
   * @return title as a BabelText. If BabelText {@code template} is null or {@code template.getTranslations()} is empty, the title is null.
   */
  public BabelText convert(String processId, BabelText template, BabelText name,
      BabelText description, TemplateModel templateModel) {
    if (BabelText.isEmpty(template) || template.getTranslations() == null || template.getTranslations().isEmpty()) {
      return null;
    }
    LOG.debug("Converting title for processId:{} and template:{}",  processId, template.getTranslations().get(template.getDefaultLanguage()));
    BabelText.BabelTextBuilder titleBuilder = BabelText
        .builder()
        .defaultLanguage(template.getDefaultLanguage());

    Map<String, String> translations = new HashMap<>();
    titleBuilder.translations(translations);

    for (Map.Entry<String, String> entry : template.getTranslations().entrySet()) {
      if (StringUtils.isBlank(entry.getValue())) {
        // no template for the language
        continue;
      }

//      if (name != null) {
//        templateModel.add(TemplateModel.Model.NAME, name.getTranslation(entry.getKey()));
//      }
//      if (description != null) {
//        templateModel.add(TemplateModel.Model.NAME, description.getTranslation(entry.getKey()));
//      }

      String title = templateConverter.convert(processId, entry.getValue(), templateModel);
      if (!StringUtils.isEmpty(title)) {
        translations.put(entry.getKey(), title);
      }
    }

    return titleBuilder.build();
  }
}
