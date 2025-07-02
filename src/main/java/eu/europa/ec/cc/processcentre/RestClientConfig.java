package eu.europa.ec.cc.processcentre;

import eu.europa.ec.cc.processcentre.ccm.external.service.CcmRestService;
import eu.europa.ec.cc.processcentre.userorganisation.external.service.ExternalUserOrganisationService;
import eu.europa.ec.cc.processcentre.util.System2SystemTokenRetriever;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.client.RestClientSsl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@Slf4j
public class RestClientConfig {

  private static final String AUTHORIZATION_HEADER = "Authorization";

  private static final String POP_TOKEN_TYPE = "pop";

  @Bean
  @ConditionalOnProperty("infra.ccm2.service.url")
  CcmRestService ccmRestService(@Value("${infra.ccm2.service.url}") String ccmServiceUrl) {
    return HttpServiceProxyFactory
        .builderFor(RestClientAdapter.create(RestClient.create(ccmServiceUrl)))
        .build()
        .createClient(CcmRestService.class);
  }

  @Bean
  @ConditionalOnProperty("service.userorganisation.endpoint.s2s")
  ExternalUserOrganisationService userOrgService(
      @Value("${service.userorganisation.endpoint.url}") String baseUrl,
      @Value("${service.userorganisation.endpoint.s2s}") String userOrgEuLoginResourceUrl,
      System2SystemTokenRetriever tokenRetriever,
      @Value("${service.userorganisation.clientId}") String clientId,
      RestClientSsl restClientSsl) {

    RestClient restClient = RestClient.builder().baseUrl(baseUrl)
        .apply(r -> restClientSsl.fromBundle("client"))
        .requestInterceptor(
            (request, body, execution) -> {
              if (request.getHeaders().containsKey(AUTHORIZATION_HEADER)) {
                LOG.debug("The Authorization token has been already set");
              } else {
                LOG.debug("Constructing Header {} for Token {}", AUTHORIZATION_HEADER, POP_TOKEN_TYPE);
                request.getHeaders().set(AUTHORIZATION_HEADER, String.format("%s %s", POP_TOKEN_TYPE,
                    tokenRetriever.getSystem2SystemToken(clientId, userOrgEuLoginResourceUrl)));
              }
              return execution.execute(request, body);
            }
        )
        .build();

    return HttpServiceProxyFactory
        .builderFor(RestClientAdapter.create(restClient))
        .build()
        .createClient(ExternalUserOrganisationService.class);
  }
}
