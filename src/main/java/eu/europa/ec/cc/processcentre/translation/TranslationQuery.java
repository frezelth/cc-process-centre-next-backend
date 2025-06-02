package eu.europa.ec.cc.processcentre.translation;

import eu.europa.ec.cc.processcentre.babel.BabelText;
import eu.europa.ec.cc.processcentre.translation.repository.FindTranslationsForAttributeQueryParam;
import eu.europa.ec.cc.processcentre.translation.repository.TranslationMapper;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TranslationQuery {

  private final TranslationMapper translationMapper;

  public TranslationQuery(TranslationMapper translationMapper) {
    this.translationMapper = translationMapper;
  }

  @Transactional(readOnly = true)
  public Map<String, String> findTranslations(String processInstanceId, TranslationObjectType objectType, TranslationAttribute attribute) {
    return translationMapper.findTranslationsForAttribute(
        new FindTranslationsForAttributeQueryParam(objectType, processInstanceId, attribute));
  }

}
