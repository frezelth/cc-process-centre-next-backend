package eu.europa.ec.cc.processcentre.security.secunda;

import static java.util.Collections.emptyList;

import eu.europa.ec.cc.processcentre.security.SecundaScope;
import eu.europa.ec.cc.processcentre.security.SecurityRepository;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@ConditionalOnProperty(value = "secunda.enabled", havingValue = "false")
public class MockSecurityRepositoryImpl implements SecurityRepository {

  @Override
  public List<SecundaScope> findScopes(String username) {
    return emptyList();
  }

  @Override
  public Set<String> findTasks(String username) {
    return Set.of();
  }
}
