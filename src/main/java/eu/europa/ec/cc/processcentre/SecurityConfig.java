package eu.europa.ec.cc.processcentre;

import static java.util.Optional.ofNullable;

import eu.europa.ec.cc.processcentre.security.SecurityRepository;
import eu.europa.ec.cc.processcentre.util.AuthInterceptor;
import eu.europa.ec.cc.processcentre.util.FakeIdentityFilter;
import eu.europa.ec.cc.processcentre.util.System2SystemTokenRetriever;
import eu.europa.ec.digit.apigw.filter.AccessTokenFilter;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author <a href="mailto:guillaume.devriendt@ext.ec.europa.eu">Guillaume Devriendt</a>
 */
@Configuration
public class SecurityConfig {


  @Bean
  @ConditionalOnProperty({ //@fmt:off
    "eulogin.client.id",
    "eulogin.client.secret",
    "eulogin.token.url"
  })
  FilterRegistrationBean<?> accessTokenFilter(
    @Value("${eulogin.client.id}") String euLoginClientId,
    @Value("${eulogin.client.secret}") String euLoginClientSecret,
    @Value("${eulogin.token.url}") String euLoginTokenUrl,
    @Value("${infra.keycloak-test.issuer:}") String testIssuer,
    @Value("${infra.keycloak-test.jwks-uri:}") String testJwksUri,
    @Value("${infra.keycloak-test.fallbackPublicKey:}") String testFallbackPublicKey
  ) { //@fmt:on

    final var introspectionEndpoint = euLoginTokenUrl + "/introspect";

    final AccessTokenFilter tokenFilter = new AccessTokenFilter(euLoginClientId, euLoginClientSecret, introspectionEndpoint);
    if (StringUtils.isNotBlank(testIssuer) && StringUtils.isNotBlank(testJwksUri)) {
      tokenFilter.apiGwTokenSupport().addTrustedIssuer(testIssuer, testJwksUri, testFallbackPublicKey);
    }
    tokenFilter.whitelist(request -> {

      final var requestURI = request.getRequestURI();
      return requestURI.equalsIgnoreCase("/health")        || //@fmt:off
             requestURI.equalsIgnoreCase("/metrics")       ||
             requestURI.contains("swagger")                ||
             requestURI.contains("v3/api-docs")            ||
             requestURI.contains("/webjars/")              ||
             requestURI.contains("/config/migration")      ||
             requestURI.contains("/configuration/refresh") ||
             requestURI.contains("actuator")               ||
             (requestURI.contains("actuator") &&
              ofNullable(request.getHeader("Accept")).orElse("").contains("hal+json")); //@fmt:on

    });
    FilterRegistrationBean<AccessTokenFilter> bean = new FilterRegistrationBean<>();
    bean.setFilter(tokenFilter);
    bean.setOrder(1);
    return bean;
  }

  @Bean
  @ConditionalOnProperty({ //@fmt:off
    "eulogin.client.id",
    "eulogin.client.secret",
    "eulogin.token.url"
  })
  System2SystemTokenRetriever system2SystemTokenRetriever(
    @Value("${eulogin.client.id}") String euLoginClientId,
    @Value("${eulogin.client.secret}") String euLoginClientSecret,
    @Value("${eulogin.token.url}") String euLoginTokenUrl
  ) { //@fmt:on
    return new System2SystemTokenRetriever(euLoginClientId, euLoginClientSecret, euLoginTokenUrl);
  }

//  @Bean
//  public FilterRegistrationBean<FakeIdentityFilter> userIdFilterRegistration(FakeIdentityFilter filter) {
//    FilterRegistrationBean<FakeIdentityFilter> registration = new FilterRegistrationBean<>();
//    registration.setFilter(filter);
//    registration.setOrder(-100);
//    return registration;
//  }

  @Configuration
  @ConditionalOnProperty(value = "secunda.enabled", havingValue = "true")
  public static class AuthConfig implements WebMvcConfigurer {

    @Lazy
    @Autowired
    private SecurityRepository secundaRepository;

    @Override
    public void addInterceptors(@NotNull InterceptorRegistry registry) {
      //@fmt:off
      registry.addInterceptor(
        new AuthInterceptor(
          secundaRepository,
          Set.of("PC_AGRI_ADMIN")
        )
      ).addPathPatterns("/adminTool/*");

      registry.addInterceptor(
        new AuthInterceptor(
          // Allow read-only operations to all!
          reqUri -> reqUri != null && (
                      reqUri.equals("/operations/processTypes")                                      ||
                      reqUri.equals("/operations/processType")                                       ||
                      (reqUri.startsWith("/operations/processType/") && reqUri.endsWith("/refresh")) ||
                      (reqUri.startsWith("/operations/") && reqUri.endsWith("/es-read-model/fix"))
                    ),
          secundaRepository,
          Set.of("PC_DOMAIN_ADMIN")
        )
      ).addPathPatterns("/operations/*");
      //@fmt:on
    }
  }

}
