package eu.europa.ec.cc.processcentre.util;

import static eu.europa.ec.digit.apigw.filter.AccessTokenFilter.ACCESS_TOKEN;
import static java.lang.Boolean.TRUE;

import eu.europa.ec.digit.apigw.filter.AccessToken.UserDetails;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

/**
 * @param actingUser either the logged-in user or the impersonated one, when impersonating
 * @author <a href="mailto:Octavian.NITA@ext.ec.europa.eu">Octavian NITA</a>
 * @version $Id$
 */
@Slf4j
record AuthDetails(String actingUser, String loggedUser) {

  public AuthDetails(String actingUser) {
    this(actingUser, actingUser);
  }

  @NotNull
  static AuthDetails fromAccessToken() {
    final var accessToken = ACCESS_TOKEN.get();
    if (accessToken == null) {
      LOG.warn("Cannot obtain the access token to determine authorization details");
      return new AuthDetails(null, null);
    }

    final var actingUser = userId(accessToken.getUserDetails());

    return TRUE.equals(accessToken.isImpersonating()) //@fmt:off
      ? new AuthDetails(actingUser, userId(accessToken.getImpersonatingUserDetails()))
      : new AuthDetails(actingUser, actingUser);      //@fmt:on
  }

  private static String userId(UserDetails userDetails) {
    return userDetails == null ? null : userDetails.getUserId();
  }
}
