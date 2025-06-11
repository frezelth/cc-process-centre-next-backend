package eu.europa.ec.cc.processcentre.exception;

import static eu.europa.ec.cc.processcentre.exception.Severity.DANGER;

import org.springframework.lang.NonNull;

/**
 * Convenient generic application {@link RuntimeException exception} to "throw and forget" at whatever level.
 * Usually no further refinements (i.e., extending classes) are required.
 * <p>
 * Try to keep the {@link #getMessage() message} concise and meaningful to the end user!
 *
 * @author <a href="mailto:Octavian.NITA@ext.ec.europa.eu">Octavian NITA</a>
 * @version $Id$
 * @see <a href="https://victorrentea.ro/blog/exception-handling-guide-in-java/">Exception Handling Guide in Java</a>
 * @see <a href="https://victorrentea.ro/blog/presenting-exceptions-to-users/">Presenting Exceptions to Users</a>
 * @see <a href="https://reflectoring.io/spring-boot-exception-handling/">Complete Guide to Exception Handling in Spring
 *   Boot</a>
 */
public class ApplicationException extends RuntimeException {

  private Severity severity = DANGER;

  public ApplicationException() {}

  public ApplicationException(String message) {
    super(message);
  }

  public ApplicationException(Throwable cause) {
    super(cause);
  }

  public ApplicationException(String message, Throwable cause) {
    super(message, cause);
  }

  @NonNull
  public Severity getSeverityOrDefault() {
    return severity == null ? DANGER : severity;
  }

  public ApplicationException setSeverity(Severity severity) {
    this.severity = severity;
    return this;
  }
}
