package eu.europa.ec.cc.processcentre.util;

import eu.europa.ec.digit.apigw.filter.AccessToken;
import eu.europa.ec.digit.apigw.filter.AccessToken.UserDetails;
import eu.europa.ec.digit.apigw.filter.AccessTokenFilter;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

@Slf4j
public class ApiHelper {

  public static Optional<String> getUsername(){
    AccessToken accessToken = AccessTokenFilter.ACCESS_TOKEN.get();
    if (accessToken != null) {
      UserDetails userDetails = accessToken.getUserDetails();

      LOG.debug("user={} // impersonation={}{}", userDetails,
          accessToken.isImpersonating(),
          accessToken.isImpersonating() ? " // impersonating user="
              + ReflectionToStringBuilder.toString(accessToken.getImpersonatingUserDetails()) : "");

      return Optional.of(userDetails.getUserId());

    }
    return Optional.empty();
  }

}
