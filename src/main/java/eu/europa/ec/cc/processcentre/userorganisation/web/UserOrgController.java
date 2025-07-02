package eu.europa.ec.cc.processcentre.userorganisation.web;

import eu.europa.ec.cc.processcentre.userorganisation.model.User;
import eu.europa.ec.cc.processcentre.userorganisation.service.UserOrganisationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/user-org")
public class UserOrgController {

  private final UserOrganisationService userOrganisationService;

  public UserOrgController(UserOrganisationService userOrganisationService) {
    this.userOrganisationService = userOrganisationService;
  }

  @GetMapping(path = "/users/{userId}")
  public User getUser(@PathVariable String userId){
    return userOrganisationService.findUser(userId);
  }

}
