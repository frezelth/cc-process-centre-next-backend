package eu.europa.ec.cc.processcentre.ccm.external.service;

import eu.europa.ec.cc.processcentre.ccm.external.service.model.CcmCode;
import java.util.Set;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

/**
 * @author <a href="mailto:Octavian.NITA@ext.ec.europa.eu">Octavian NITA</a>
 * @version $Id$
 */
public interface CcmRestService {

  String CCM_CODE_TYPE_EC_HIER = "ECHierarchy";

  // Relevant contexts for ECHierarchy (so far)

  String CCM_CONTEXT_ECFIN = "ECFIN";

  String CCM_CONTEXT_GEN = "GEN";

  @GetExchange("/codes/{context}/{codeType}")
  Set<CcmCode> findCodes(@PathVariable String context, @PathVariable String codeType);
}
