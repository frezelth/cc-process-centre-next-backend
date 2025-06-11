package eu.europa.ec.cc.processcentre.exception;

/**
 * This is the exception to throw for authorization errors (e.g., missing or wrong Secunda/access rights, etc.).
 * It hints at using the proper HTTP (error) status code 403 Forbidden instead of 401 Unauthorized.
 *
 * @author <a href="mailto:Octavian.NITA@ext.ec.europa.eu">Octavian NITA</a>
 * @version $Id$
 * @see <a href="https://stackoverflow.com/questions/3297048/403-forbidden-vs-401-unauthorized-http-responses">403
 *   Forbidden vs 401 Unauthorized HTTP responses</a>
 * @see <a href="https://auth0.com/blog/forbidden-unauthorized-http-status-codes/">Forbidden, Unauthorized, or What
 *   Else?</a>
 * @see <a href="https://leastprivilege.com/2014/10/02/401-vs-403/">401 vs 403</a>
 */
public class ForbiddenException extends ApplicationException {

  public ForbiddenException() {}

  public ForbiddenException(String message) {
    super(message);
  }

  public ForbiddenException(Throwable cause) {
    super(cause);
  }

  public ForbiddenException(String message, Throwable cause) {
    super(message, cause);
  }
}
