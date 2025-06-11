package eu.europa.ec.cc.processcentre.security;

import eu.europa.ec.cc.processcentre.userorganisation.model.Organisation;
import java.util.List;
import java.util.Map;
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

  /**
   * @deprecated The organisation should be explicitly defined in
   *   {@link eu.europa.ec.processcentre.process.model.ProcessTypeConfiguration#setAccessRights(Map)}
   */
  @Deprecated
  List<Organisation> responsibleOrganisation;

  @Deprecated
  // the object referring to the portfolioId should be explicitly defined in Process Type >
  // accessRight
  List<String> portfolioItemBusinessIds;

  // data from Secunda scopeRuleList
  List<ScopeRule> scopeRules;

  // data from Secunda scopeList
  List<Scope> scopes;

}
