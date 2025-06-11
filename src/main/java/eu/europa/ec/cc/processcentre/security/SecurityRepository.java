package eu.europa.ec.cc.processcentre.security;

import eu.europa.ec.rtd.secunda.author.rest.domain.TaskRight;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.lang.NonNull;

public interface SecurityRepository {

  String APPLICATION_ID_PROCESS_CENTRE = "PROCESS_CENTRE";

  @NonNull
  Set<String> findTasks(String username);

  @NonNull
  List<SecundaScope> findScopes(String username);

  @NonNull
  Set<String> findBusinessDomainIds(
    String username, String applicationId, String taskId, String domainScopeType
  );

  @NonNull
  Collection<TaskRight> getAssignedTaskRights(String username);
}
