package eu.europa.ec.cc.processcentre.babel.repository.model;

public record CreateOrUpdateBabelQueryParam(
    String urn,
    String languageCode,
    boolean defaultTranslation,
    String translatedText
) {

}
