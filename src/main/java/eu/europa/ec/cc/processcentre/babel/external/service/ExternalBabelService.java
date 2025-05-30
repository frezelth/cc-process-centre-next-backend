package eu.europa.ec.cc.processcentre.babel.external.service;

import eu.europa.ec.cc.processcentre.babel.external.service.model.BabelTranslation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

/**
 * Call to the external service of babel
 * @author <a href="thomas.frezel@ext.ec.europa.eu">Thomas Frezel</a>
 * @author <a href="guillaume.devriendt@ext.ec.europa.eu">Guillaume Devriendt</a>
 */
@ConditionalOnProperty(value="service.babel.enabled", havingValue = "true")
@HttpExchange(url = "${service.babel.endpoint}")
public interface ExternalBabelService {

  @GetExchange(
      value = "/admin/translations/urn/{urn}",
      accept = {MediaType.APPLICATION_JSON_VALUE}
  )
  BabelTranslation findTranslations(@PathVariable(name="urn") String urn);

}