package eu.europa.ec.cc.processcentre.userorganisation.external.service;

import eu.europa.ec.cc.processcentre.userorganisation.external.service.model.Organisation;
import eu.europa.ec.cc.processcentre.userorganisation.external.service.model.User;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Call to the external service of user/organisation
 * @author <a href="thomas.frezel@ext.ec.europa.eu">Thomas Frezel</a>
 * @author <a href="guillaume.devriendt@ext.ec.europa.eu">Guillaume Devriendt</a>
 */
@ConditionalOnProperty(value="service.userorganisation.enabled", havingValue = "true")
public interface ExternalUserOrganisationService {

  @RequestMapping(
      method = RequestMethod.GET,
      value = "/users/{userId}",
      produces = {MediaType.APPLICATION_JSON_VALUE}
  )
  User findUser(@PathVariable(name="userId") String userId);

  @RequestMapping(
      method = RequestMethod.GET,
      value = "/organisations/{organisationId}",
      produces = {MediaType.APPLICATION_JSON_VALUE}
  )
  Organisation findOrganisation(@PathVariable(name="organisationId") String organisationId);

}