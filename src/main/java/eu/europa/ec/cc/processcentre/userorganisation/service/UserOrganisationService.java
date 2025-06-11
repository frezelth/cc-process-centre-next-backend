package eu.europa.ec.cc.processcentre.userorganisation.service;

import static eu.europa.ec.cc.processcentre.util.Perf.trace;
import static java.util.regex.Pattern.compile;

import eu.europa.ec.cc.processcentre.exception.ApplicationException;
import eu.europa.ec.cc.processcentre.userorganisation.external.service.ExternalUserOrganisationService;
import eu.europa.ec.cc.processcentre.userorganisation.model.Organisation;
import eu.europa.ec.cc.processcentre.userorganisation.model.User;
import jakarta.validation.constraints.NotNull;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Bridge to the external user organisation service.
 */
@Slf4j
@Service
public class UserOrganisationService {

  private final ExternalUserOrganisationService service;

  private static final Pattern PATTERN_ORG_ID = compile("-?\\d+");

  public UserOrganisationService(
    @Autowired(required = false) ExternalUserOrganisationService service
  ) {
    this.service = service;
  }

  /**
   * Finds an @{@link Organisation} from a COMREF organisationId
   * @param organisationId
   * @return
   */
  @Cacheable(cacheNames = "organisations")
  public Organisation findOrganisation(String organisationId) {

    if (!PATTERN_ORG_ID.matcher(organisationId).matches()) {
      LOG.warn("OrganisationId: {} is not valid", organisationId);
      return null;
    }

    try {

      final var organisation = trace(
        () -> service.findOrganisation(organisationId),
        "Calling the external user/organisation service for organization {}...",
        organisationId
      );
      if (organisation == null) {
        return null;
      }

      return  Organisation.builder().id(organisationId).code(organisation.getCode()).build();
    } catch (Exception e) {
      if (is404NotFound(e)) {
        // exception was caught in the user organisation service, log it and continue
        LOG.warn("Organisation {} not found in the external organisation service: {}", organisationId, e.getMessage());
        return null;
      } else {
        throw new ApplicationException("Exception in the user organisation service while getting organisation: " + organisationId, e);
      }
    }
  }

  /**
   *
   * @param organisationId
   * @return
   */
  @Cacheable(cacheNames = "organisations")
  public Organisation findOrganisationOrUndefinedIfException(String organisationId) {
    try {
      return findOrganisation(organisationId);
    } catch (ApplicationException e) {
      LOG.error("Exception in user organisation service. Return mock organisation: code=UNDEFINED, id=0", e);
      return Organisation.builder().code("UNDEFINED").id("0").build();
    }
  }

  /**
   * Finds a @{@link User from a COMREF userId}
   * @param userId
   * @return
   */
  @Cacheable(cacheNames = "users")
  public User findUser(@NotNull String userId) {


    try {
      User.UserBuilder userBuilder = User.builder().userId(userId);

      final var user = trace(
        () -> service.findUser(userId),
        "Calling the external user/organisation service for user {}...",
        userId
      );
      if (user == null) {
        return null;
      }

      userBuilder.firstName(user.getFirstName())
        .lastName(user.getLastName())
        .email(user.getEmail())
        .title(user.getTitle());

      if (user.getOrganisation() != null){
        userBuilder.organisation(
          Organisation.builder()
            .id(user.getOrganisation().getId())
            .code(user.getOrganisation().getCode())
            .build()
        );
      }

      return userBuilder.build();

    } catch (Exception e){
      if (is404NotFound(e)){
        // exception was caught in the user organisation service, log it and continue
        LOG.warn("User {} not found in the external user service: {}", userId, e.getMessage());
        return null;
      } else {
        throw new ApplicationException("Exception in the user organisation service while getting user: " + userId, e);
      }
    }
  }

  static boolean is404NotFound(Throwable failure) {
    return (
     true
    )
      || (failure.getCause() != null && is404NotFound(failure.getCause()));
  }
}
