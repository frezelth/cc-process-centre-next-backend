package eu.europa.ec.cc.processcentre.config.service;

import static eu.europa.ec.cc.configuration.service.MergeStrategy.NO_MERGE_HIGHEST_SCORE_WINS;
import static eu.europa.ec.cc.configuration.service.SearchStrategy.BEST_MATCH;
import static eu.europa.ec.cc.processcentre.config.ConfigException.translate;
import static eu.europa.ec.cc.processcentre.config.ConfigType.PROCESS_RESULT;
import static eu.europa.ec.cc.processcentre.config.ConfigType.PROCESS_SORTABLE_FIELDS;
import static eu.europa.ec.cc.processcentre.config.ConfigType.PROCESS_TYPE;
import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.cc.configuration.domain.ConfigurationSet;
import eu.europa.ec.cc.configuration.service.DomainConfigService;
import eu.europa.ec.cc.processcentre.config.ConfigType;
import eu.europa.ec.cc.processcentre.config.InvalidConfigException;
import eu.europa.ec.cc.processcentre.config.ProcessTypeConfig;
import eu.europa.ec.cc.processcentre.config.SortableFieldConfig;
import eu.europa.ec.cc.processcentre.util.Context;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
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

  public Optional<ProcessTypeConfig> fetchProcessTypeConfig(Map<String, String> context) {
    return fetchConfig(ProcessTypeConfig.class, PROCESS_TYPE, context);
  }

  public Optional<String> fetchResultCardLayoutConfig(Map<String, String> context) {
    return fetchConfig(String.class, PROCESS_RESULT, context);
  }

  public Optional<JsonNode> fetchConfig(ConfigType configType, Map<String, String> context){
    Context.requireValidContext(context);

    try {

      return ofNullable(domainConfigService.get(
          context,
          configType.toString(),
          BEST_MATCH,
          NO_MERGE_HIGHEST_SCORE_WINS
      )).map(ConfigurationSet::getContent);

    } catch (Exception exception) {
      throw translate(exception);
    }
  }

  @SuppressWarnings("unchecked")
  private <T> Optional<T> fetchConfig(Class<T> configClass, ConfigType configType, Map<String, String> context) {
    return fetchConfig(configType, context).map(
        c -> {

          if (LOG.isDebugEnabled()) {
            LOG.debug("Config for context {} fetched, config is {}",
                context, c);
          }

          try {
            if (configClass.isAssignableFrom(String.class)){
              return (T) objectMapper.writeValueAsString(c);
            } else {
              return objectMapper.treeToValue(c, configClass);
            }
          } catch (JsonProcessingException e) {
            LOG.warn("Invalid config for type {} and context {}", configType, context);
            throw new InvalidConfigException(e);
          }
        }
    );

  }

  public Optional<SortableFieldConfig> fetchProcessSortableFields(Map<String, String> context) {
    return fetchConfig(SortableFieldConfig.class, PROCESS_SORTABLE_FIELDS, context);
  }
}
