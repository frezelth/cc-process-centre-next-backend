package eu.europa.ec.cc.processcentre.process.query;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isBlank;

import eu.europa.ec.cc.processcentre.process.query.web.dto.SearchProcessRequestDto;
import eu.europa.ec.cc.processcentre.mapper.QueryConverter;
import eu.europa.ec.cc.processcentre.process.query.repository.QueryMapper;
import eu.europa.ec.cc.processcentre.process.query.repository.model.SearchProcessQueryParam;
import eu.europa.ec.cc.processcentre.process.query.repository.model.SearchProcessQueryParam.SecurityFilter;
import eu.europa.ec.cc.processcentre.process.query.repository.model.SearchProcessQueryParam.SecurityFilterScope;
import eu.europa.ec.cc.processcentre.process.query.repository.model.SearchProcessQueryResponse;
import eu.europa.ec.cc.processcentre.process.query.web.dto.SearchProcessResponseDto;
import eu.europa.ec.cc.processcentre.process.query.web.dto.SearchProcessResponseDto.SearchProcessResponseDtoProcess;
import eu.europa.ec.cc.processcentre.security.ECHierarchyService;
import eu.europa.ec.cc.processcentre.security.Scope;
import eu.europa.ec.cc.processcentre.security.ScopeRule;
import eu.europa.ec.cc.processcentre.security.SecundaScope;
import eu.europa.ec.cc.processcentre.security.SecurityRepository;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class ProcessQueries {

  private static final String SCOPE_TYPE_ID_EC_HIER = "EC-Hierarchy";
  private static final String SCOPE_TYPE_ID_CRF_HIER = "CRF-Hierarchy";

  private final QueryMapper queryMapper;
  private final QueryConverter queryConverter;
  private final SecurityRepository securityRepository;
  private final ECHierarchyService ecHierarchyService;

  public ProcessQueries(QueryMapper queryMapper,
      QueryConverter queryConverter,
      SecurityRepository securityRepository,
      ECHierarchyService ecHierarchyService) {
    this.queryMapper = queryMapper;
    this.queryConverter = queryConverter;
    this.securityRepository = securityRepository;
    this.ecHierarchyService = ecHierarchyService;
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

  public SearchProcessResponseDto searchProcesses(
      SearchProcessRequestDto searchProcessDto,
      int offset, int limit, Locale locale, String username){

    List<SecundaScope> scopes = securityRepository.findScopes(username);

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

    // if user has no security filter we should just return an empty result list
    // otherwise we'll not generate the where clause and the user will see all processes
    if (securityFilters.isEmpty()) {
      return new SearchProcessResponseDto(0,
          Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    SearchProcessQueryParam queryParam = queryConverter.toQueryParam(searchProcessDto, locale,
        username, limit, offset, securityFilters);
    List<SearchProcessQueryResponse> search = queryMapper.search(queryParam);

    if (securityFilters.isEmpty()) {
      return new SearchProcessResponseDto(0,
          Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    return new SearchProcessResponseDto(
        search.getFirst().getTotalCount(),
        search.stream().map(e ->
            new SearchProcessResponseDtoProcess(
                e.getProcessInstanceId(),
                e.getTitle(),
                e.getStatus(),
                e.getCardLayout(),
                false,
                e.getActiveTasks()
            )).toList(),
        Collections.emptyList(),
        Collections.emptyList()
    );

  }

}
