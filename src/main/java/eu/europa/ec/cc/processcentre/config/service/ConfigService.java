package eu.europa.ec.cc.processcentre.config.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.cc.configuration.domain.ConfigurationSet;
import eu.europa.ec.cc.configuration.service.DomainConfigService;
import eu.europa.ec.cc.configuration.service.MergeStrategy;
import eu.europa.ec.cc.configuration.service.SearchStrategy;
import eu.europa.ec.cc.processcentre.config.ProcessTypeConfig;
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
    Context.requireValidContext(context);

    ConfigurationSet processTypeConfigAsJson = domainConfigService.get(context, ProcessTypeConfig.CONFIGURATION_TYPE_NAME,
        SearchStrategy.BEST_MATCH,
        MergeStrategy.NO_MERGE_HIGHEST_SCORE_WINS);

    try {
      if (LOG.isDebugEnabled()){
        LOG.debug("Config for context {} fetched, config is {}",
            context, processTypeConfigAsJson.getContent());
      }
      return Optional.of(objectMapper.treeToValue(processTypeConfigAsJson.getContent(),
          ProcessTypeConfig.class));
    } catch (JsonProcessingException e) {
      LOG.warn("Cannot convert process configuration for context {} into json, config is {}",
          context, processTypeConfigAsJson.getContent());
      return Optional.empty();
    }
  }

}
