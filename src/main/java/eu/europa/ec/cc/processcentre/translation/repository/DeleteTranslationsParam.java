package eu.europa.ec.cc.processcentre.translation.repository;

import eu.europa.ec.cc.processcentre.translation.TranslationAttribute;
import eu.europa.ec.cc.processcentre.translation.TranslationObjectType;

public record DeleteTranslationsParam(
    TranslationObjectType objectType,
    String objectId,
    TranslationAttribute attribute
) {

}
