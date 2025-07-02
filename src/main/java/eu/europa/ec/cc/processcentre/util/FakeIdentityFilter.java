package eu.europa.ec.cc.processcentre.util;

import eu.europa.ec.digit.apigw.filter.AccessToken;
import eu.europa.ec.digit.apigw.filter.AccessTokenFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import net.minidev.json.JSONObject;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Profile("cc-local")
@Component
public class FakeIdentityFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {

    if (AccessTokenFilter.ACCESS_TOKEN.get() == null) {
      AccessTokenFilter.ACCESS_TOKEN.set(
          new AccessToken(new JSONObject(Map.of("sub", "frezeth")), null)
      );
    }

    filterChain.doFilter(request, response);
  }

}
