package eu.europa.ec.cc.processcentre.babel.external.service.model;

import java.util.List;

/**
 * Representation of a babel translation in the babel microservice
 * @author <a href="guillaume.devriendt@ext.ec.europa.eu">Guillaume Devriendt</a>
 */
public record BabelTranslation(
    String urn,
    List<Translation> translations,
    List<String> collections
) {

}
