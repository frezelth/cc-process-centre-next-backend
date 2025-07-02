package eu.europa.ec.cc.processcentre.security;

import java.util.List;
import java.util.Set;
import org.springframework.lang.NonNull;

public interface SecurityRepository {

  String APPLICATION_ID_PROCESS_CENTRE = "PROCESS_CENTRE";

  @NonNull
  List<SecundaScope> findScopes(String username);

  @NonNull
  Set<String> findTasks(String username);

}
