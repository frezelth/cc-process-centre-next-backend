package eu.europa.ec.cc.processcentre.translation;

import eu.europa.ec.cc.processcentre.babel.BabelText;
import eu.europa.ec.cc.processcentre.translation.repository.DeleteTranslationsParam;
import eu.europa.ec.cc.processcentre.translation.repository.InsertOrUpdateTranslationsParam;
import eu.europa.ec.cc.processcentre.translation.repository.TranslationMapper;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class TranslationService {

  private final TranslationMapper translationMapper;

  public TranslationService(TranslationMapper translationMapper) {
    this.translationMapper = translationMapper;
  }

  @Transactional
  public void insertOrUpdateTranslations(
      TranslationObjectType objectType,
      String objectId,
      TranslationAttribute attribute,
      BabelText text){

    if (StringUtils.isNotEmpty(text.getUrn())){
      // handle babel text
    } else {
      // handle internal translations
      insertOrUpdateInternalTranslations(objectType, objectId, attribute, text);
    }

  }

  private void insertOrUpdateInternalTranslations(TranslationObjectType objectType, String objectId,
      TranslationAttribute attribute, BabelText text) {
    // delete current translations for the object
    translationMapper.deleteTranslationsForAttribute(new DeleteTranslationsParam(objectType, objectId,
        attribute));

    if (text.getTranslations() == null || text.getTranslations().isEmpty()) {
      if (LOG.isDebugEnabled()){
        LOG.debug("No translations found for object id {}", objectId);
      }
      return;
    }

    List<InsertOrUpdateTranslationsParam> newTranslations = text.getTranslations().entrySet().stream().map(
        e -> new InsertOrUpdateTranslationsParam(
            objectType, objectId, attribute, e.getKey(), e.getValue(),
            e.getKey().equals(text.getDefaultLanguage())
        )
    ).toList();

    // insert new translations
    translationMapper.insertOrUpdateTranslations(newTranslations);
  }

}
