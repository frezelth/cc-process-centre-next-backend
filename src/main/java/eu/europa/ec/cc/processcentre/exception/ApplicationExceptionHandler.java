package eu.europa.ec.cc.processcentre.exception;

import static eu.europa.ec.cc.processcentre.exception.Severity.DANGER;
import static eu.europa.ec.cc.processcentre.exception.Severity.WARNING;
import static java.util.Arrays.copyOf;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.ResponseEntity.status;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * @author <a href="mailto:Octavian.NITA@ext.ec.europa.eu">Octavian NITA</a>
 * @version $Id$
 */
@ControllerAdvice
@Slf4j(topic = "ApplicationException")
public class ApplicationExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleAnyUncaughtException(Exception exception, HttpServletRequest request) {

    // Determine an appropriate HTTP error status
    final var status = switch (exception) {
      case InvalidInputException ignored -> BAD_REQUEST;
      case UnauthorizedException ignored -> UNAUTHORIZED;
      case ForbiddenException ignored -> FORBIDDEN;
      case NotFoundException ignored -> NOT_FOUND;
      case null, default -> INTERNAL_SERVER_ERROR;
    };

    // Determine the severity (could be used by the UI) and a proper error message while logging the exception
    final var sam = log(exception);

    // Return an appropriate error response
    return status(status.value()).body(new ErrorResponse()
                                         // HTTP-wise
                                         .setStatus(status.value()).setError(status.toString())
                                         // My Workplace components can use this
                                         .setSeverity(sam.severity)
                                         // Instant feedback in browser developer tools
                                         .setMessage(sam.messageOrDefault())
                                         // Etc.
                                         .setPath(request == null ? "" : request.getRequestURI()));
  }

  @NonNull
  public static SeverityAndMessage log(
    Exception exception, boolean ignoreStackTraceForWarn, String prefixFmt, Object... prefixArgs
  ) {

    final var sam = new SeverityAndMessage(exception);
    final var ignoreStackTrace = sam.severity == WARNING && ignoreStackTraceForWarn;

    String fmt;
    Object[] args;
    if (isBlank(prefixFmt)) {

      fmt = sam.message;
      args = ignoreStackTrace ? null : new Object[]{ exception };

    } else {

      fmt = prefixFmt + ": " + sam.message;
      if (ignoreStackTrace) {
        args = prefixArgs;
      } else if (prefixArgs == null) {
        args = new Object[]{ exception };
      } else {
        args = copyOf(prefixArgs, prefixArgs.length + 1);
        args[args.length - 1] = exception;
      }
    }

    if (exception instanceof NotFoundException) {
      // Avoid polluting the log with "404 Not Found", the message in the client is usually enough
      LOG.trace(fmt, args);
    } else if (exception instanceof ForbiddenException || sam.severity == WARNING) {
      LOG.warn(fmt, args);
    } else {
      LOG.error(fmt, args);
    }

    return sam;
  }

  @NonNull
  public static SeverityAndMessage log(Exception exception) {
    return log(exception, false, null);
  }

  public static final class SeverityAndMessage {

    @NonNull
    public final Severity severity;

    public final String message;

    public SeverityAndMessage(Exception exception) {

      severity = exception instanceof ApplicationException appEx ? appEx.getSeverityOrDefault() : DANGER;

      var msg = exception == null ? null : exception.getMessage();
      if (msg == null && exception != null) {
        // Single (i.e. don't go up the full stack) attempt to find a more meaningful error message
        final var cause = exception.getCause();
        if (cause != null) {
          msg = cause.getMessage();
        }
      }
      message = msg;
    }

    @NonNull
    public String messageOrDefault() {
      return isBlank(message) ? "An error has occurred but no additional details are currently available" : message;
    }
  }
}
