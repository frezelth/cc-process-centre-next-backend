package eu.europa.ec.cc.processcentre.exception;

/**
 * This is the exception to throw when we can't obtain the currently logged-in user.
 * It hints at using the proper HTTP (error) 401 Unauthorized status code instead of 403 Forbidden.
 *
 * @author <a href="mailto:Octavian.NITA@ext.ec.europa.eu">Octavian NITA</a>
 * @version $Id$
 */
public class UnauthorizedException extends ApplicationException {

  public UnauthorizedException() {
    this("Cannot determine the currently logged-in user");
  }

  public UnauthorizedException(String message) {
    super(message);
  }

  public UnauthorizedException(Throwable cause) {
    super(cause);
  }

  public UnauthorizedException(String message, Throwable cause) {
    super(message, cause);
  }
}
