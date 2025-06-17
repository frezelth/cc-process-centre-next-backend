package eu.europa.ec.cc.processcentre.config.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.cc.configuration.domain.ConfigurationSet;
import eu.europa.ec.cc.configuration.service.DomainConfigService;
import eu.europa.ec.cc.configuration.service.MergeStrategy;
import eu.europa.ec.cc.configuration.service.SearchStrategy;
import eu.europa.ec.cc.processcentre.config.ProcessTypeConfig;
import eu.europa.ec.cc.processcentre.exception.ApplicationException;
import eu.europa.ec.cc.processcentre.util.Context;
import java.util.Map;
import java.util.Optional;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ConfigService {

  private final DomainConfigService domainConfigService;
  private final ObjectMapper objectMapper;

  public ConfigService(DomainConfigService domainConfigService,
      ObjectMapper objectMapper) {
    this.domainConfigService = domainConfigService;
    this.objectMapper = objectMapper;
  }

  public ProcessTypeConfig fetchProcessTypeConfig(Map<String, String> context) {
    return fetchConfig(ProcessTypeConfig.class, ProcessTypeConfig.CONFIGURATION_TYPE_NAME, context);
  }

  public String fetchResultCardLayoutConfig(Map<String, String> context) {
    return fetchConfig(String.class, ProcessTypeConfig.CONFIGURATION_RESULT_CARD_NAME, context);
  }

  public JsonNode fetchConfig(String configType, Map<String, String> context){
    Context.requireValidContext(context);

    ConfigurationSet config = domainConfigService.get(context,
            configType,
            SearchStrategy.BEST_MATCH,
            MergeStrategy.NO_MERGE_HIGHEST_SCORE_WINS);

    if (config == null) {
      return null;
    }

    return config.getContent();
  }

  @SuppressWarnings("unchecked")
  private <T> T fetchConfig(Class<T> configClass, String configType, Map<String, String> context) {

    JsonNode config = fetchConfig(configType, context);

    if (config == null) {
      LOG.warn("Config not found for type {} and context {}", configType, context);
      return null;
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug("Config for context {} fetched, config is {}",
          context, config);
    }

    try {
      if (configClass.isAssignableFrom(String.class)){
        return (T) objectMapper.writeValueAsString(config);
      } else {
        return objectMapper.treeToValue(config, configClass);
      }
    } catch (JsonProcessingException e) {
      LOG.warn("Invalid config for type {} and context {}", configType, context);
      return null;
    }
  }
}
