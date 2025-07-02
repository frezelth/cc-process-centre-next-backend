package eu.europa.ec.cc.processcentre.userorganisation.external.service;

import eu.europa.ec.cc.processcentre.userorganisation.external.service.model.Organisation;
import eu.europa.ec.cc.processcentre.userorganisation.external.service.model.User;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.service.annotation.GetExchange;

/**
 * Call to the external service of user/organisation
 * @author <a href="thomas.frezel@ext.ec.europa.eu">Thomas Frezel</a>
 * @author <a href="guillaume.devriendt@ext.ec.europa.eu">Guillaume Devriendt</a>
 */
public interface ExternalUserOrganisationService {

  @GetExchange(
      value = "/users/{userId}",
      accept = {MediaType.APPLICATION_JSON_VALUE}
  )
  User findUser(@PathVariable(name="userId") String userId);

  @GetExchange(
      value = "/organisations/{organisationId}",
      accept = {MediaType.APPLICATION_JSON_VALUE}
  )
  Organisation findOrganisation(@PathVariable(name="organisationId") String organisationId);

}