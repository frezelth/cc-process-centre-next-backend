package eu.europa.ec.cc.processcentre;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.cc.configuration.domain.ConfigurationSet;
import eu.europa.ec.cc.configuration.service.DomainConfigService;
import eu.europa.ec.cc.configuration.service.MergeStrategy;
import eu.europa.ec.cc.configuration.service.SearchStrategy;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

@Configuration
public class ConfigLibraryMockConfig {

  @Bean
  public DomainConfigService domainConfigService() {
    return new MockDomainConfigService();
  }

  public static class MockDomainConfigService implements DomainConfigService {

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    @SneakyThrows
    public ConfigurationSet get(Map<String, String> context, String type,
        SearchStrategy searchStrategy, MergeStrategy mergeStrategy) {
      String processTypeKey = context.get("processTypeKey");
      if (processTypeKey != null) {
        Resource classPathResource = new ClassPathResource(
            "/config/" + processTypeKey + ".json");

        if (!classPathResource.exists()) {
          return new ConfigurationSet();
        }

        String configContent = classPathResource.getContentAsString(StandardCharsets.UTF_8);

        ConfigurationSet configurationSet = new ConfigurationSet();
        configurationSet.setContent(mapper.readTree(configContent));
        return configurationSet;
      }
      return null;
    }

    @Override
    public Collection<ConfigurationSet> getMerged(Map<String, String> context,
        SearchStrategy searchStrategy, MergeStrategy mergeStrategy) {
      return List.of();
    }

    @Override
    public Collection<ConfigurationSet> getAll(Map<String, String> metadata,
        SearchStrategy searchStrategy) {
      return List.of();
    }

    @Override
    public Collection<ConfigurationSet> getAll(Map<String, String> metadata,
        SearchStrategy searchStrategy, Predicate<ConfigurationSet> preSearchFilter) {
      return List.of();
    }

    @Override
    public Collection<ConfigurationSet> getAll(Map<String, String> context,
        SearchStrategy searchStrategy, Predicate<ConfigurationSet> preSearchFilter,
        boolean omitContent) {
      return List.of();
    }

    @Override
    public Collection<ConfigurationSet> getAbsolutelyAll(Map<String, String> context,
        SearchStrategy searchStrategy, Predicate<ConfigurationSet> preSearchFilter,
        boolean omitContent) {
      return List.of();
    }

    @Override
    public ConfigurationSet get(String id) {
      return null;
    }

    @Override
    public String getConfigurationInfo(Map<String, String> context, SearchStrategy searchStrategy) {
      return "";
    }

    @Override
    public InputStream getResource(String relativeResourcePath) {
      return null;
    }

    @Override
    public InputStream getResource(String configurationSetId, String source) {
      return null;
    }

    @Override
    public Map<String, Integer> getConfigurationFiles(Map<String, String> context, String type,
        SearchStrategy searchStrategy) {
      return Map.of();
    }
  }

}
