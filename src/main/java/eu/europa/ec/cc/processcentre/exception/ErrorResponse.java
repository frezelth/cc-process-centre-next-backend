package eu.europa.ec.cc.processcentre.exception;

import static eu.europa.ec.cc.processcentre.exception.Severity.DANGER;
import static java.time.Instant.now;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Along with {@link Severity}, supports Compass Corporate/My Workplace error payloads for better reporting in the UI.
 *
 * @author <a href="mailto:Octavian.NITA@ext.ec.europa.eu">Octavian NITA</a>
 * @version $Id$
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ErrorResponse {

  public static final DateTimeFormatter DEFAULT_ER_DATE_TIME_FORMATTER = RFC_1123_DATE_TIME.withZone(
    ZoneId.of("Europe/Brussels"));

  private String timestamp = DEFAULT_ER_DATE_TIME_FORMATTER.format(now());

  private int status = INTERNAL_SERVER_ERROR.value();

  /**
   * Usually, the textual description (reason phrase) of the error code/status.
   */
  private String error = INTERNAL_SERVER_ERROR.getReasonPhrase();

  private Severity severity = DANGER;

  private String summary = severity.getSummary();

  private String message = "An error has occurred but not additional details are currently available";

  private String path = "";

  public String getSummary() {
    return isBlank(summary) && severity != null ? severity.getSummary() : summary;
  }
}
