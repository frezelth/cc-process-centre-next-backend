package eu.europa.ec.cc.processcentre.config;

import static java.lang.System.lineSeparator;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.fasterxml.jackson.core.JsonParseException;
import eu.europa.ec.cc.processcentre.exception.ApplicationException;
import jakarta.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;

/**
 * Base class for all problems encountered when fetching or processing configuration from the external configuration
 * repository. Subclass of {@link RuntimeException} for convenience. If no explicit {@link #getMessage() message} is
 * provided upon instance creation but a {@link #getCause() cause} exception is wrapped, the {@link #getMessage()
 * message} in the cause is exposed.
 *
 * @author <a href="mailto:Octavian.NITA@ext.ec.europa.eu">Octavian NITA</a>
 * @version $Id$
 */
public class ConfigException extends ApplicationException {

  public ConfigException(String message) {
    super(message);
  }

  public ConfigException(Throwable cause) {
    super(cause);
  }

  public ConfigException(String message, Throwable cause) {
    super(message, cause);
  }

  @Override
  public String getMessage() {
    var message = super.getMessage();
    if (message == null) {
      final var cause = getCause();
      if (cause != null) {
        message = cause.getMessage();
      }
    }
    return message;
  }

  /**
   * Translates the provided {@code exception} argument to an appropriate {@link ConfigException} instance.
   * <p>
   * Tries to work around <a
   * href="https://citnet.tech.ec.europa.eu/CITnet/stash/projects/CSDR/repos/cc-embedded-configuration-lib/browse/src/main/java/eu/europa/ec/cc/configuration/service/CustomizableDomainConfigService.java">
   * dependency code</a> such as:
   * <pre>
   * try {
   *   // Load and parse JSON config file
   *   // ...
   * } catch (Exception e) {
   *   LOG.error("...");
   *   throw new RuntimeException(e);
   * }
   * </pre>
   * that makes detecting invalid configurations (that users may be able to correct themselves) difficult.
   */
  @NotNull
  public static ConfigException translate(Exception exception) {
    switch (exception) {
    case null -> {
      return new ConfigException((String) null);
    }
    case ConfigException configException -> {
      return configException;
    }
    case JsonParseException jsonParseException -> {
      return translate(null, jsonParseException);
    }
    default -> {
    }
    }

    if (RuntimeException.class.equals(exception.getClass())) {
      final var rteMessage = exception.getMessage();
      final var rteCause = exception.getCause();

      if (rteCause != null && JsonParseException.class.equals(rteCause.getClass())) {
        return translate(rteMessage, (JsonParseException) rteCause);
      } else {
        return new ConfigException(rteMessage, rteCause);
      }
    }

    return new ConfigException(exception);
  }

  @NotNull
  public static InvalidConfigException translate(String headerMessage, JsonParseException jsonParseException) {
    if (jsonParseException == null) {
      return new InvalidConfigException(headerMessage);
    }

    var message = jsonParseException.getOriginalMessage();
    if (headerMessage != null && !headerMessage.equals(jsonParseException.toString())) {
      message = headerMessage + lineSeparator() + message;
    }

    final var jsonLoc = jsonParseException.getLocation();
    if (jsonLoc != null) {
      message += " at line " + jsonLoc.getLineNr() + ", column " + jsonLoc.getColumnNr();

      final var jsonRef = jsonLoc.contentReference() == null ? null : jsonLoc.contentReference().getRawContent();
      if (jsonRef instanceof File) {
        String jsonPath;
        try {
          jsonPath = ((File) jsonRef).getCanonicalPath();
        } catch (IOException e) {
          jsonPath = ((File) jsonRef).getPath();
        }
        if (isNotBlank(jsonPath)) {
          message += " in file " + jsonPath;
        }
      }
    }

    return new InvalidConfigException(message, jsonParseException);
  }
}
