package eu.europa.ec.cc.processcentre.security.secunda;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;

import eu.europa.ec.cc.processcentre.security.SecundaScope;
import eu.europa.ec.cc.processcentre.security.SecurityRepository;
import eu.europa.ec.rtd.secunda.author.rest.domain.TaskRight;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@ConditionalOnProperty(value = "secunda.enabled", havingValue = "false")
public class MockSecurityRepositoryImpl implements SecurityRepository {

  @NonNull
  @Override
  public Set<String> findTasks(String username) {
    return emptySet();
  }

  @Override
  public List<SecundaScope> findScopes(String username) {
    return emptyList();
  }

  @Override
  public Set<String> findBusinessDomainIds(
    String username, String applicationId, String taskId, String domainScopeType
  ) {
    return emptySet();
  }

  @NonNull
  @Override
  public Collection<TaskRight> getAssignedTaskRights(String username) {
    return emptyList();
  }
}
