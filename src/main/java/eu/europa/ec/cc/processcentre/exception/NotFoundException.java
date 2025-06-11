package eu.europa.ec.cc.processcentre.exception;

import static java.lang.String.valueOf;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Optional;
import org.springframework.lang.NonNull;

/**
 * @author <a href="mailto:Octavian.NITA@ext.ec.europa.eu">Octavian NITA</a>
 * @version $Id$
 */
public class NotFoundException extends ApplicationException {

  public NotFoundException() {}

  public NotFoundException(String message) {
    super(message);
  }

  public NotFoundException(Throwable cause) {
    super(cause);
  }

  public NotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Convenience constructor for more uniform "Not Found" error messages.
   */
  public NotFoundException(String what, Object identifiedBy) {
    this(cannotFindMessage(what, identifiedBy).orElse(""));
  }

  @NonNull
  public static Optional<String> cannotFindMessage(String what, Object identifiedBy) {
    String message = "Cannot find";
    boolean messageBuilt = false;
    if (isNotBlank(what)) {
      message += " " + what;
      messageBuilt = true;
    }
    if (identifiedBy != null) {
      final String identifiedByStr = valueOf(identifiedBy);
      if (isNotBlank(identifiedByStr)) {
        message += " " + identifiedByStr;
        messageBuilt = true;
      }
    }
    return messageBuilt ? of(message) : empty();
  }
}
