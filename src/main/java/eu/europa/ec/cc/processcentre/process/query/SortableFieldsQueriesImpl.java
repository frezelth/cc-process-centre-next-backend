package eu.europa.ec.cc.processcentre.process.query;

import static eu.europa.ec.cc.processcentre.util.Context.contextAsKey;
import static eu.europa.ec.cc.processcentre.util.Context.isValidContext;
import static java.lang.System.currentTimeMillis;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingInt;
import static java.util.Comparator.nullsLast;
import static java.util.concurrent.CompletableFuture.runAsync;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.cc.configuration.service.EmbeddedConfigurationLibRefreshFinished;
import eu.europa.ec.cc.processcentre.config.SortableField;
import eu.europa.ec.cc.processcentre.config.service.ConfigService;
import eu.europa.ec.cc.processcentre.process.query.repository.QueryMapper;
import eu.europa.ec.cc.processcentre.process.query.repository.model.SearchProcessQueryParam;
import eu.europa.ec.cc.processcentre.process.query.repository.model.SearchProcessQueryResponse;
import eu.europa.ec.cc.processcentre.process.query.repository.model.SearchProcessQueryResponse.SearchProcessQueryResponseType;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SortableFieldsQueriesImpl implements SortableFieldsQueries {

  private final Map<String, Set<SortableField>> sortableFieldsCache = new HashMap<>();

  private final AtomicBoolean cachePopulationOngoing = new AtomicBoolean(false);

  private final QueryMapper queryMapper;
  private final ObjectMapper objectMapper;
  private final ConfigService configService;

  public SortableFieldsQueriesImpl(QueryMapper queryMapper, ObjectMapper objectMapper, ConfigService configService) {
    this.queryMapper = queryMapper;
    this.objectMapper = objectMapper;
    this.configService = configService;
  }

  @NonNull
  public List<SortableField> findSortableFields(
      Collection<Map<String, String>> contexts, Locale locale) {
    if (contexts == null || contexts.isEmpty()) {
      return emptyList();
    }

    // See the identity of a SortableField
    final var sortableFields = new HashSet<SortableField>();
    final var contextIter = contexts.iterator();

    // 1. Find the first context having sortable fields and add those to the result
    Map<String, String> currContext;
    while (contextIter.hasNext() && (currContext = contextIter.next()) != null) {
      final var currSortableFields = sortableFieldsCache.get(contextAsKey(currContext));
      if (currSortableFields != null) {
        sortableFields.addAll(currSortableFields);
        break;
      }
    }

    // 2. Add the rest of the sortable fields, but detect when no "new" fields get added (to sort differently)
    boolean moreThanOneSortableFieldSet = false;
    while (contextIter.hasNext() && (currContext = contextIter.next()) != null) {
      final var currSortableFields = sortableFieldsCache.get(contextAsKey(currContext));
      if (currSortableFields != null) {
        for (final var sortableField : currSortableFields) {
          moreThanOneSortableFieldSet |= sortableFields.add(sortableField);
        }
      }
    }

    // If more than one configuration, sort alphabetically; otherwise, sort by property order
    final Comparator<SortableField> cmp =
        moreThanOneSortableFieldSet
            ? comparing(sf -> sf.label(locale), nullsLast(String::compareTo))
            : comparingInt(SortableField::getPropertyOrder);

    return sortableFields
        .stream()
        .sorted(cmp.thenComparing(SortableField::getField, nullsLast(String::compareTo))) // keep a default comparator
        .toList();
  }

  @EventListener
  public void onConfigLibInit(ContextRefreshedEvent event) {
    populateSortableFieldsCache();
  }

  @EventListener
  public void onConfigLibRefreshed(EmbeddedConfigurationLibRefreshFinished event) {
    populateSortableFieldsCache();
  }

  final void populateSortableFieldsCache() {
    if (!cachePopulationOngoing.compareAndSet(false, true)) {
      return;
    }

    runAsync(() -> {
      final var startTime = currentTimeMillis();

      try {
        final var allAvailableContexts = queryMapper.search(new SearchProcessQueryParam())
            .stream()
            .filter(r -> r.getType() == SearchProcessQueryResponseType.agg)
            .map(SearchProcessQueryResponse::getPayload)
            .findFirst();

        if (allAvailableContexts.isEmpty()) {
          LOG.debug("No process contexts found to populate the sortable fields cache");
          return;
        }

        Set<Map<String,String>> allContexts = objectMapper.readValue(allAvailableContexts.get(), new TypeReference<>() {});

        LOG.debug("Populating the sortable fields cache for {} process contexts...", allContexts.size());
        sortableFieldsCache.clear();

        for (final var context : allContexts) {
          if (!isValidContext(context)) {
            continue;
          }

          final var cacheKey = contextAsKey(context);
          configService.fetchProcessSortableFields(context).ifPresentOrElse(
              sortableFieldsConfig -> {
                final var sortableFields = sortableFieldsConfig.getSortableFields();
                if (sortableFields != null && !sortableFields.isEmpty()) {
                  sortableFieldsCache.computeIfAbsent(cacheKey, ck -> new HashSet<>()).addAll(sortableFields);
                }
              },
              () -> sortableFieldsCache.remove(cacheKey)
          );
        }
      } catch (Exception ex) {
        LOG.error("An error has occurred while populating the sortable fields cache", ex);
      } finally {
        LOG.debug("Done populating the sortable fields cache");
        cachePopulationOngoing.set(false);
      }

      final var endTime = currentTimeMillis();
      LOG.debug("Sortable fields cache population time: {}ms", endTime - startTime);
    });
  }

}
