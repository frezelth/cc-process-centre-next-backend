package eu.europa.ec.cc.processcentre.babel.external.service.model;

/**
 * Representation of a translation in the babel microservice
 * @author <a href="guillaume.devriendt@ext.ec.europa.eu">Guillaume Devriendt</a>
 */
public record Translation(
    String language,
    String text,
    boolean defaultTranslation
) {

}
