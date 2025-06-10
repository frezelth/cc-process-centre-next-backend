package eu.europa.ec.cc.processcentre.process.command.repository.model;

import eu.europa.ec.cc.processcentre.translation.TranslationAttribute;
import eu.europa.ec.cc.processcentre.translation.TranslationObjectType;
import lombok.Data;

@Data
public class FindProcessByIdQueryResponseTranslation {
  private String objectId;
  private TranslationObjectType objectType;
  private TranslationAttribute attribute;
  private String languageCode;
  private String text;
  private Boolean isDefault;

}
