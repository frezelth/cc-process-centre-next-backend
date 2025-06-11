package eu.europa.ec.cc.processcentre.security;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScopeRule {

    String scopeTypeId;
    String attrName;
    String attrVal;
//    String scopeId; // TODO develop if needed from EAC - it requires a few changes in the Secunda calls - additional call for the each secunda task is required
    
    public String getScopeId() {
      // TODO analyse; FOR EAC it should be id in scopeRule, but it's not retrieved. 
      return attrVal;
    }
}
