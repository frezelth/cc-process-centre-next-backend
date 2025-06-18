package eu.europa.ec.cc.processcentre;

import eu.europa.ec.cc.processcentre.ccm.external.service.CcmRestService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@ConditionalOnProperty("infra.ccm2.service.url")
public class RestClientConfig {

  @Value("${infra.ccm2.service.url:}")
  private String ccmServiceUrl;

  @Bean
  CcmRestService ccmRestService() {
    return HttpServiceProxyFactory
        .builderFor(RestClientAdapter.create(RestClient.create(ccmServiceUrl)))
        .build()
        .createClient(CcmRestService.class);
  }
}
