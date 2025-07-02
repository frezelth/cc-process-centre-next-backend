package eu.europa.ec.cc.processcentre.security.secunda;

import static eu.europa.ec.cc.processcentre.exception.ApplicationExceptionHandler.log;
import static eu.europa.ec.cc.processcentre.exception.Severity.WARNING;
import static eu.europa.ec.cc.processcentre.util.Perf.trace;
import static java.lang.String.join;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.apache.commons.lang3.StringUtils.isBlank;

import eu.europa.ec.cc.processcentre.exception.ApplicationException;
import eu.europa.ec.cc.processcentre.security.Scope;
import eu.europa.ec.cc.processcentre.security.ScopeRule;
import eu.europa.ec.cc.processcentre.security.SecundaScope;
import eu.europa.ec.cc.processcentre.security.SecurityRepository;
import eu.europa.ec.rtd.secunda.author.rest.client.AuthorizationManager;
import eu.europa.ec.rtd.secunda.author.rest.client.criteria.TaskRightCriteria;
import eu.europa.ec.rtd.secunda.author.rest.client.criteria.TaskRightCriteria.TaskRightCriteriaBuilder;
import eu.europa.ec.rtd.secunda.author.rest.domain.TaskRight;
import eu.europa.ec.rtd.secunda.author.rest.domain.exceptions.EntryNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@ConditionalOnProperty(value = "secunda.enabled", havingValue = "true")
public class SecurityRepositoryImpl implements SecurityRepository {

  private final AuthorizationManager authorizationManager;

  /**
   * Performs a Secunda access operation if the primary {@link AuthorizationManager authorization manager} is available.
   * Otherwise, it logs a message and returns an empty result. The {@code action} receives the authorization manager as
   * an argument and can eventually return a result.
   */
  private <T> Optional<T> secunda(Function<AuthorizationManager, T> action) {
    if (authorizationManager == null) {
      LOG.debug("Cannot access Secunda, no authorization manager has been configured");
    }

    final T result;
    if (authorizationManager != null && action != null) {
      result = action.apply(authorizationManager);
    } else {
      result = null;
    }
    return ofNullable(result);
  }

  @NonNull
  private Collection<TaskRight> findTaskRights(TaskRightCriteriaBuilder<?, ?> criteriaBuilder, String... criteriaQ) {
    return secunda(author -> {

      final var criteria = (criteriaBuilder == null ? TaskRightCriteria.builder() : criteriaBuilder).build();
      if (criteriaQ != null && criteriaQ.length > 0) {
        criteria.setQ(join(" ", criteriaQ));
      }

      try {

        // getTaskRights() should not return null, according to the documentation
        return trace(
          () -> unmodifiableCollection(author.getTaskRights(criteria)),
          "Calling Secunda to get task rights for criteria {}...",
          criteria
        );

      } catch (Exception ex) {

        final var appEx = new ApplicationException(ex);
        if (ex instanceof EntryNotFoundException) {
          appEx.setSeverity(WARNING); // not found entities should probably be logged as warnings
        }
        throw appEx;

      }

    }).orElseGet(Collections::emptySet);
  }

  private static void forEachRuleDataEntry(
    Supplier<Collection<eu.europa.ec.rtd.secunda.author.rest.domain.ScopeRule>> scopeRulesSupplier,
    BiConsumer<String, Set<Map<String, Set<String>>>> ruleDataEntryConsumer
  ) {
    if (ruleDataEntryConsumer == null) {
      return;
    }

    final var scopeRules = scopeRulesSupplier == null ? null : scopeRulesSupplier.get();
    if (CollectionUtils.isEmpty(scopeRules)) {
      return;
    }

    for (final var scopeRule : scopeRules) {
      final var ruleData = scopeRule == null ? null : scopeRule.getRuleData();
      if (!CollectionUtils.isEmpty(ruleData)) {
        ruleData.forEach((scopeTypeId, scopeAttrs) -> {
          if (scopeTypeId != null && !CollectionUtils.isEmpty(scopeAttrs)) {
            ruleDataEntryConsumer.accept(scopeTypeId, scopeAttrs);
          }
        });
      }
    }
  }

  @NonNull
  @Override
  public Set<String> findTasks(String username) {
    if (isBlank(username)) {
      return emptySet();
    }

    try {
      return findTaskRights(TaskRightCriteria.builder().userId(username).applicationId(APPLICATION_ID_PROCESS_CENTRE))
          .stream()
          .map(TaskRight::getTaskId)
          .collect(toUnmodifiableSet());
    } catch (Exception ex) {
      log(ex, true, "Cannot obtain the Secunda tasks for user {}", username);
      return emptySet();
    }
  }

  @NonNull
  @Override
  public List<SecundaScope> findScopes(String username) {
    final var secundaScopes = new ArrayList<SecundaScope>();

    try {
      final var assignedTaskRights = getAssignedTaskRights(username);
      if (assignedTaskRights.isEmpty()) {
        LOG.debug(
          "No assigned Secunda tasks found for user {}, application ID {}",
          username,
          APPLICATION_ID_PROCESS_CENTRE
        );
      }

      for (final var taskRight : assignedTaskRights) {

        final var secundaScope = new SecundaScope();
        secundaScope.setSecundaTaskId(taskRight.getTaskId());
        secundaScope.setScopeRules(new ArrayList<>());
        secundaScope.setScopes(new ArrayList<>());

        // 1. Visit scope rules => get responsible organizations and scope rules
        //    scope rule list | CRF-Hierarchy  => organisation responsibles
        //    scope rule list | *              => scope rules
        forEachRuleDataEntry(taskRight::getScopeRules, (scopeTypeId, scopeAttrs) -> {

          scopeAttrs.forEach(scopeAttr -> scopeAttr
            .entrySet()
            .stream()
            .filter(scopeAttrNameAndVals -> !CollectionUtils.isEmpty(scopeAttrNameAndVals.getValue()))
            .forEach(scopeAttrNameAndVals -> {
              for (final var scopeAttrVal : scopeAttrNameAndVals.getValue()) {
                secundaScope.getScopeRules().add(ScopeRule.builder().scopeTypeId(scopeTypeId).attrName(
                  scopeAttrNameAndVals.getKey()).attrVal(scopeAttrVal).build());
              }
            }));

        });

        // 2. Visit scopes => get responsible organizations, business domain ids, and scopes
        //    scope list | CRF-Hierarchy  => organisation responsibles
        //    scope list | BusinessDomain => Portfolio item business ids
        //    scope list | *              => scopes
        final var scopes = taskRight.getScopes();
        if (!CollectionUtils.isEmpty(scopes)) {
          scopes.forEach((scopeTypeId, scopeIds) -> {
            for (final var scopeId : scopeIds) {
              secundaScope.getScopes().add(
                  Scope.builder().scopeTypeId(scopeTypeId).scopeId(scopeId).build());
            }
          });
        }

        secundaScopes.add(secundaScope);
      }

    } catch (Exception ex) {
      log(ex, true, "Cannot obtain the Secunda scopes for user {}", username);
    }

    return unmodifiableList(secundaScopes);
  }

  private Collection<TaskRight> getAssignedTaskRights(String username) {
    return findTaskRights(TaskRightCriteria
                            .builder()
                            .userId(username)
                            .includeScopes("explicit")
                            .applicationId(APPLICATION_ID_PROCESS_CENTRE));
  }
}
