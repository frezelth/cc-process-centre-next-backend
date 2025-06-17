package eu.europa.ec.cc.processcentre.template;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateExtractor {

  private static final Pattern VARIABLE_PATTERN = Pattern.compile(
      "\\$\\{([^}]+)}" +                            // ${...} expressions
          "|<#(?:if|elseif|assign)\\s+([^>]+)>" +       // <#if>, <#elseif>, <#assign>
          "|<#list\\s+([^\\s]+)\\s+as\\s+[^>]+>"        // <#list items as item>
  );

  private static final Pattern EXTRACT_PATH_PATTERN = Pattern.compile(
      "(\\b[a-zA-Z_]\\w*(?:\\.[a-zA-Z_]\\w*)*)" +   // var.name.path
          "(?:\\?\\w+|!|\\(.*?\\))?"                    // ignore ?builtins, !defaults, method calls
  );

  /**
   * Checks if any of the given target variables are used in the template.
   * Exact or prefix match (e.g., matches "user" if "user.name" is present).
   */
  public static boolean matchAny(String templateSource, Collection<String> targetVariables) {
    Set<String> templateVars = extractVariables(templateSource);

    Set<String> targetSet = new HashSet<>(targetVariables);

    for (String fullPath : templateVars) {
      // Split path like "variables.var1" into parts: ["variables", "var1"]
      String[] parts = fullPath.split("\\.");

      for (String part : parts) {
        if (targetSet.contains(part)) {
          return true;
        }
      }
    }

    return false;
  }

  public static Set<String> extractVariables(String templateSource) {
    Set<String> variables = new TreeSet<>();
    Matcher matcher = VARIABLE_PATTERN.matcher(templateSource);

    while (matcher.find()) {
      for (int i = 1; i <= matcher.groupCount(); i++) {
        String expression = matcher.group(i);
        if (expression != null) {
          variables.addAll(parseExpression(expression));
        }
      }
    }

    return variables;
  }

  private static Set<String> parseExpression(String expression) {
    Set<String> result = new TreeSet<>();
    Matcher matcher = EXTRACT_PATH_PATTERN.matcher(expression);

    while (matcher.find()) {
      String raw = matcher.group(1);
      if (!raw.contains("(")) {  // ignore full method calls like user.getName()
        result.add(raw);
      }
    }

    return result;
  }

}
