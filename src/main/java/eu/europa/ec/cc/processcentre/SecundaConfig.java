package eu.europa.ec.cc.processcentre;

import static java.util.Collections.emptyList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConditionalOnProperty(value = "secunda.enabled", havingValue = "true")
public class SecundaConfig {

  @Bean("secundaAuthorConfig")
  @ConfigurationProperties(prefix = "secunda.author.config")
  public Map<String, String> authorConfig() {
    return new HashMap<>();
  }

  @Bean
  public eu.europa.ec.rtd.secunda.author.rest.client.AuthorizationManager authorizationManager(
    @Autowired @Qualifier("secundaAuthorConfig") Map<String, String> secundaAuthorConfig
  ) {

    if (secundaAuthorConfig == null || secundaAuthorConfig.isEmpty()) {
      LOG.warn("Cannot instantiate a Secunda Author, no applicable configuration detected");
      return null;
    }

    final Properties config = new Properties();
    config.putAll(secundaAuthorConfig);

    try {
      return eu.europa.ec.rtd.secunda.author.rest.client.AuthorizationManagerFactory.getInstance(config).getAuthor();
    } catch (Exception e) {
      LOG.error("Cannot instantiate a Secunda Author", e);
      return null;
    }
  }
}
