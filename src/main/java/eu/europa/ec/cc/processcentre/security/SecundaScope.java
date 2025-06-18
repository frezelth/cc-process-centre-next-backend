package eu.europa.ec.cc.processcentre.security;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecundaScope {

  String secundaTaskId;

  // data from Secunda scopeRuleList
  List<ScopeRule> scopeRules;

  // data from Secunda scopeList
  List<Scope> scopes;

}
