package eu.europa.ec.cc.processcentre.process.query;

import static eu.europa.ec.cc.processcentre.util.Context.contextAsKey;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.cc.configuration.service.EmbeddedConfigurationLibRefreshFinished;
import eu.europa.ec.cc.processcentre.config.ConfigType;
import eu.europa.ec.cc.processcentre.config.service.ConfigService;
import eu.europa.ec.cc.processcentre.process.query.repository.QueryMapper;
import eu.europa.ec.cc.processcentre.process.query.repository.model.SearchProcessQueryParam;
import eu.europa.ec.cc.processcentre.process.query.repository.model.SearchProcessQueryResponse;
import eu.europa.ec.cc.processcentre.process.query.repository.model.SearchProcessQueryResponse.SearchProcessQueryResponseType;
import eu.europa.ec.cc.processcentre.util.Context;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CommonColumnsQueriesImpl implements CommonColumnsQueries {

  // cache of columns, key = providerId:domainKey:processTypeKey as ImmutableMap/EN label = list of columns
  private final Map<String, List<String>> columnsCache = new HashMap<>();
  private final ConfigService configService;
  private final QueryMapper queryMapper;

  private final AtomicBoolean refreshOngoing = new AtomicBoolean(false);
  private final ObjectMapper objectMapper;

  @EventListener
  public void onConfigLibInit(ContextRefreshedEvent event) {
    populateColumnsCache();
  }

  @EventListener
  public void onConfigLibRefreshed(EmbeddedConfigurationLibRefreshFinished event) {
    populateColumnsCache();
  }

  void populateColumnsCacheAsync() {
    long methodStart = System.currentTimeMillis();

    try {

      if (LOG.isDebugEnabled()) {
        LOG.debug("Starting loading common columns cache");
      }

      final var allAvailableContexts = queryMapper.search(new SearchProcessQueryParam())
          .stream()
          .filter(r -> r.getType() == SearchProcessQueryResponseType.agg)
          .map(SearchProcessQueryResponse::getPayload)
          .findFirst();

      Set<Map<String,String>> allContexts = objectMapper.readValue(allAvailableContexts.get(), new TypeReference<>() {});

      if (LOG.isDebugEnabled()){
        LOG.debug("Retrieved {} process contexts", allContexts.size());
      }

      allContexts
          .stream().filter(Context::isValidContext)
          .forEach(
          ctx ->
            configService.fetchConfig(ConfigType.PROCESS_RESULT, ctx)
                .ifPresentOrElse(
                    // update or add config in the cache for existing process result config
                    // only support config containing triplet providerId/domainKey/processTypeKey
                    config -> columnsCache.put(
                        contextAsKey(ctx), parseColumnsFromConfig(config)),
                    // remove from the cache if config is not found
                    () -> columnsCache.remove(contextAsKey(ctx))
                )
      );

      // remove processes that are not present in the resultset anymore
      columnsCache.keySet()
          .retainAll(allContexts.stream().map(Context::contextAsKey).collect(Collectors.toSet()));
    } catch (Exception e){
      LOG.error("Exception loading common columns cache", e);
    } finally {
      refreshOngoing.set(false);
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug("Time to load common columns {}ms", (System.currentTimeMillis() - methodStart));
    }
  }

  private synchronized void populateColumnsCache() {
    if (refreshOngoing.get()) {
      return;
    }
    refreshOngoing.set(true);
    CompletableFuture.runAsync(this::populateColumnsCacheAsync);
  }

  public List<String> findCommonColumns(Set<Map<String, String>> contexts) {
    if (LOG.isTraceEnabled()){
      LOG.trace("Retrieving common columns for contexts {}", contexts);
    }
    if (contexts == null) {
      return Collections.emptyList();
    }
    List<String> commonColumns = contexts.stream()
        .map(ctx -> columnsCache.get(contextAsKey(ctx)))
        .filter(Objects::nonNull)
        .reduce(ListUtils::retainAll)
        .orElse(Collections.emptyList());

    List<String> distinctCommonColumns = commonColumns.stream().distinct().toList();

    if (LOG.isTraceEnabled()){
      LOG.trace("Found common columns for contexts {}, {}", contexts, distinctCommonColumns);
    }
    return distinctCommonColumns;
  }

  private List<String> parseColumnsFromConfig(JsonNode config) {
    List<String> headerColumns = extractColumnsFromConfig(config, "header");
    List<String> collapsedColumns = extractColumnsFromConfig(config, "expanded");
    return ListUtils.union(headerColumns, collapsedColumns);
  }

  private static List<String> extractColumnsFromConfig(JsonNode config, String section) {
    List<String> result = new ArrayList<>();
    JsonNode headerProperties = config.findPath("sections")
        .findPath(section)
        .findPath("properties");
    if (headerProperties.isMissingNode()) {
      return Collections.emptyList();
    }
    headerProperties.forEach(
        property -> {
          JsonNode node = property.findPath("label").findPath("translations").findPath("en");
          if (!node.isMissingNode()) {
            result.add(node.textValue());
          }
        }
    );
    return result;
  }

}
