package eu.europa.ec.cc.processcentre.process.query;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isBlank;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.cc.processcentre.config.SortableField;
import eu.europa.ec.cc.processcentre.mapper.QueryConverter;
import eu.europa.ec.cc.processcentre.process.query.repository.QueryMapper;
import eu.europa.ec.cc.processcentre.process.query.repository.model.SearchProcessQueryParam;
import eu.europa.ec.cc.processcentre.process.query.repository.model.SearchProcessQueryParam.SecurityFilter;
import eu.europa.ec.cc.processcentre.process.query.repository.model.SearchProcessQueryParam.SecurityFilterScope;
import eu.europa.ec.cc.processcentre.process.query.repository.model.SearchProcessQueryResponse;
import eu.europa.ec.cc.processcentre.process.query.repository.model.SearchProcessQueryResponse.SearchProcessQueryResponseType;
import eu.europa.ec.cc.processcentre.process.query.web.dto.SearchProcessRequestDto;
import eu.europa.ec.cc.processcentre.process.query.web.dto.SearchProcessResponseDto;
import eu.europa.ec.cc.processcentre.security.ECHierarchyService;
import eu.europa.ec.cc.processcentre.security.Scope;
import eu.europa.ec.cc.processcentre.security.ScopeRule;
import eu.europa.ec.cc.processcentre.security.SecundaScope;
import eu.europa.ec.cc.processcentre.security.SecurityRepository;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Component
@Slf4j
public class ProcessQueries {

  private static final String SCOPE_TYPE_ID_EC_HIER = "EC-Hierarchy";
  private static final String SCOPE_TYPE_ID_CRF_HIER = "CRF-Hierarchy";

  private final QueryMapper queryMapper;
  private final QueryConverter queryConverter;
  private final SecurityRepository securityRepository;
  private final ECHierarchyService ecHierarchyService;
  private final SortableFieldsQueries sortableFieldsQueries;
  private final ObjectMapper objectMapper;
  private final CommonColumnsQueries commonColumnsQueries;

  public ProcessQueries(QueryMapper queryMapper,
      QueryConverter queryConverter,
      SecurityRepository securityRepository,
      ECHierarchyService ecHierarchyService, ObjectMapper objectMapper,
      CommonColumnsQueries commonColumnsQueries,
      SortableFieldsQueries sortableFieldsQueries) {
    this.queryMapper = queryMapper;
    this.queryConverter = queryConverter;
    this.securityRepository = securityRepository;
    this.ecHierarchyService = ecHierarchyService;
    this.objectMapper = objectMapper;
    this.commonColumnsQueries = commonColumnsQueries;
    this.sortableFieldsQueries = sortableFieldsQueries;
  }

  public Map<String, Set<String>> getScopeRulesSecurityFilters(Collection<ScopeRule> scopeRules) {

    if (scopeRules == null || scopeRules.isEmpty()) {
      return emptyMap();
    }

    Map<String, Set<String>> securityScopeTypeFilters = new HashMap<>();

    // ec-hierarchy scope rules
    final var ecHierarchyScopeRules = ecHierarchyScopeRules(scopeRules);

    if (!ecHierarchyScopeRules.isEmpty()) {
      securityScopeTypeFilters.put(SCOPE_TYPE_ID_EC_HIER, ecHierarchyScopeRules);
    }

    final var crfHierarchyScopeRules = crfHierarchyScopeRules(scopeRules);
    if (!crfHierarchyScopeRules.isEmpty()) {
      securityScopeTypeFilters.put(SCOPE_TYPE_ID_CRF_HIER, crfHierarchyScopeRules);
    }

    return securityScopeTypeFilters;
  }

  private @NotNull Set<String> crfHierarchyScopeRules(Collection<ScopeRule> scopeRules){
    return scopeRules
        .stream()
        .filter(rule -> SCOPE_TYPE_ID_CRF_HIER.equals(rule.getScopeTypeId()))
        .map(ScopeRule::getAttrVal)
        .filter(StringUtils::isNotBlank)
        .collect(toSet());
  }

  private @NotNull Set<String> ecHierarchyScopeRules(Collection<ScopeRule> scopeRules) {
    Set<String> ecHierarchyScopeRuleValues = scopeRules
        .stream()
        .filter(rule -> SCOPE_TYPE_ID_EC_HIER.equals(rule.getScopeTypeId()))
        .map(ScopeRule::getAttrVal)
        .filter(StringUtils::isNotBlank)
        .collect(toSet());

    if (ecHierarchyScopeRuleValues.isEmpty()) {
      return emptySet();
    }

    return ecHierarchyService.findTopLevelOrganisationCodes(ecHierarchyScopeRuleValues);
  }

  @SneakyThrows
  public SearchProcessResponseDto searchProcesses(
      SearchProcessRequestDto searchProcessDto,
      int offset, int limit, Locale locale, String username){

    StopWatch sw = new StopWatch();

    sw.start("Fetch secunda tasks");
    List<SecundaScope> scopes = securityRepository.findScopes(username);
    sw.stop();

    sw.start("Gather security filters");
    Set<SecurityFilter> securityFilters = new HashSet<>();
    for (final var secundaScope : scopes) {
      if (isBlank(secundaScope.getSecundaTaskId())) {
        continue; // we require at least a Secunda task ID
      }

      Map<String, Set<String>> securityScopeRuleFilters = getScopeRulesSecurityFilters(secundaScope.getScopeRules());
      Map<String, Set<String>> securityExplicitScopeFilters = secundaScope.getScopes().stream().collect(groupingBy(
          Scope::getScopeTypeId, mapping(Scope::getScopeId, toSet())
      ));

      // combine scope and scope rules
      Map<String, Set<String>> combineScopes = Stream.concat(securityScopeRuleFilters.entrySet().stream(),
              securityExplicitScopeFilters.entrySet().stream())
          .collect(toMap(
              Entry::getKey,
              Entry::getValue, // defensive copy to avoid mutating input
              (set1, set2) -> {
                set1.addAll(set2); // union on key collision
                return set1;
              }
          ));

      Set<SecurityFilterScope> combinedSecurityFilterScope = combineScopes.entrySet().stream()
          .map(e -> new SecurityFilterScope(e.getKey(), e.getValue()))
          .collect(toSet());

      SecurityFilter filter = new SecurityFilter(
          secundaScope.getSecundaTaskId(),
          combinedSecurityFilterScope
      );
      securityFilters.add(filter);
    }

    sw.stop();
    // if user has no security filter we should just return an empty result list
    // otherwise we'll not generate the where clause and the user will see all processes
//    if (securityFilters.isEmpty()) {
//      return new SearchProcessResponseDto(0,
//          Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
//    }

    sw.start("Creating search param");
    SearchProcessQueryParam queryParam = queryConverter.toQueryParam(searchProcessDto, locale,
        username, limit, offset, securityFilters);
    sw.stop();

    sw.start("Searching for processes");
    List<SearchProcessQueryResponse> search = queryMapper.search(queryParam);
    sw.stop();

    sw.start("Formatting results");
    if (search.isEmpty()) {
      return new SearchProcessResponseDto(0,
          Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    String totalCountJson = search.stream()
        .filter(s -> s.getType() == SearchProcessQueryResponseType.count)
        .map(SearchProcessQueryResponse::getPayload)
        .findFirst().orElseThrow();

    long totalCount = objectMapper.readTree(totalCountJson).get("total_count").longValue();

    if (totalCount == 0){
      return new SearchProcessResponseDto(0,
          Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    List<String> processes = search.stream()
        .filter(s -> s.getType() == SearchProcessQueryResponseType.data)
        .map(SearchProcessQueryResponse::getPayload)
        .toList();

    String contextKeysAsString = search.stream()
        .filter(s -> s.getType() == SearchProcessQueryResponseType.agg)
        .map(SearchProcessQueryResponse::getPayload)
        .findFirst().orElse("{[]}");

    Set<Map<String,String>> contexts = objectMapper.readValue(contextKeysAsString,
        new TypeReference<>() {
        });

    Set<Map<String, String>> orderedContext = contexts
        .stream()
        .map(c -> {
          LinkedHashMap<String, String> map = new LinkedHashMap<>();
          map.put("providerId", c.get("providerId"));
          map.put("domainKey", c.get("domainKey"));
          map.put("processTypeKey", c.get("processTypeKey"));
          return map;
        }).collect(toSet());

    sw.stop();
    LOG.debug(sw.prettyPrint());

    List<SortableField> sortableFields = sortableFieldsQueries.findSortableFields(
        orderedContext,
        locale // used, for example, to sort the fields
    );

    List<String> commonColumns = commonColumnsQueries.findCommonColumns(contexts);

    return new SearchProcessResponseDto(
        totalCount,
        processes,
        sortableFields,
        commonColumns
    );

  }

}
