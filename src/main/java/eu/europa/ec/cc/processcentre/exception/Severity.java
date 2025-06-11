package eu.europa.ec.cc.processcentre.exception;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author <a href="mailto:Octavian.NITA@ext.ec.europa.eu">Octavian NITA</a>
 * @version $Id$
 */
public enum Severity {

  WARNING,
  DANGER {
    @Override
    public String getSummary() {
      return "ERROR";
    }
  };

  public String getSummary() {
    return name();
  }

  @JsonValue
  public String toLowerCase() {
    return name().toLowerCase();
  }
}
