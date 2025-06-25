package eu.europa.ec.cc.processcentre.config;

/**
 * Exception type for configuration parsing problems, used when non-well-formed content (content that does not conform
 * to JSON syntax as per specification) or otherwise invalid configuration content is encountered.
 *
 * @author <a href="mailto:Octavian.NITA@ext.ec.europa.eu">Octavian NITA</a>
 * @version $Id$
 */
public class InvalidConfigException extends ConfigException {

  public InvalidConfigException(String message) {
    super(message);
  }

  public InvalidConfigException(Throwable cause) {
    super(cause);
  }

  public InvalidConfigException(String message, Throwable cause) {
    super(message, cause);
  }
}
