package eu.europa.ec.cc.processcentre.exception;

/**
 * @author <a href="mailto:Octavian.NITA@ext.ec.europa.eu">Octavian NITA</a>
 * @version $Id$
 */
public class InvalidInputException extends ApplicationException {

  public InvalidInputException() {}

  public InvalidInputException(String message) {
    super(message);
  }

  public InvalidInputException(Throwable cause) {
    super(cause);
  }

  public InvalidInputException(String message, Throwable cause) {
    super(message, cause);
  }
}
