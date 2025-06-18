package eu.europa.ec.cc.processcentre.ccm.external.service.model;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author <a href="mailto:Octavian.NITA@ext.ec.europa.eu">Octavian NITA</a>
 * @version $Id$
 */
public record CcmCode(

  String id,

  String abbreviation,

  boolean active

) {

  public boolean isValid() {
    return active && isNotBlank(id) && isNotBlank(abbreviation);
  }
}
