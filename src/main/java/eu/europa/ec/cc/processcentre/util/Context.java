package eu.europa.ec.cc.processcentre.util;

import static java.util.Collections.unmodifiableMap;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

/**
 * @author <a href="mailto:Octavian.NITA@ext.ec.europa.eu">Octavian NITA</a>
 * @version $Id$
 */
@Slf4j
public class Context {

  public static final String CK_PROVIDER_ID = "providerId";
  public static final String CK_DOMAIN_KEY = "domainKey";
  public static final String CK_PROCESS_TYPE_KEY = "processTypeKey";

  /** We rely on utility methods so far. */
  private Context() {}

  @NonNull
  public static Map<String, String> context(String providerId, String domainKey, String processTypeKey) {
    final var context = new LinkedHashMap<String, String>();

    if (isNotBlank(providerId)) {
      context.put(CK_PROVIDER_ID, providerId);
    }
    if (isNotBlank(domainKey)) {
      context.put(CK_DOMAIN_KEY, domainKey);
    }
    if (isNotBlank(processTypeKey)) {
      context.put(CK_PROCESS_TYPE_KEY, processTypeKey);
    }

    return unmodifiableMap(context);
  }

  public static boolean isValidContext(Map<String, String> context) {
    return //@fmt:off
      context != null &&
      !context.isEmpty() &&
      isNotBlank(context.get(CK_PROVIDER_ID)) &&
      isNotBlank(context.get(CK_DOMAIN_KEY)) &&
      isNotBlank(context.get(CK_PROCESS_TYPE_KEY)); //@fmt:on
  }

  @NonNull
  public static Map<String, String> requireValidContext(Map<String, String> context) {
    final var invalidProcessContextPrefix = "Invalid process context ";
    if (context == null || context.isEmpty()) {
      throw new InvalidContextException(invalidProcessContextPrefix + "(the context map is null or empty)");
    }
    if (isBlank(context.get(CK_PROVIDER_ID))) {
      throw new InvalidContextException(invalidProcessContextPrefix + context + ": the provider ID is null or empty");
    }
    if (isBlank(context.get(CK_DOMAIN_KEY))) {
      throw new InvalidContextException(invalidProcessContextPrefix + context + ": the domain key is null or empty");
    }
    if (isBlank(context.get(CK_PROCESS_TYPE_KEY))) {
      throw new InvalidContextException(invalidProcessContextPrefix + context + ": the process type key is null or empty");
    }
    return context;
  }

  public static String contextAsKey(Map<String, String> context) {
    return context == null
      ? null
      : context.get(CK_PROVIDER_ID) + ":" + context.get(CK_DOMAIN_KEY) + ":" + context.get(CK_PROCESS_TYPE_KEY);
  }

  public static String toJson(Map<String, String> context) {
    try {
      return context == null ? null : json.writeValueAsString(context);
    } catch (JsonProcessingException e) {
      LOG.debug("", e);
      return null;
    }
  }

  private static final ObjectMapper json = new ObjectMapper();

  public static class InvalidContextException extends RuntimeException {

    public InvalidContextException(String message) {
      super(message);
    }

  }
}
