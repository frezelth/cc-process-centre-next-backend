package eu.europa.ec.cc.processcentre.template;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;

/**
 * 
 * @author oleksra
 *
 */
@Data
public class TemplateModel {

  public static final String TEMPLATE_MARKER = "${";
//
//  private static final String MODEL_MUST_NOT_BE_NULL = "model must not be null";
//  private static final String NAME_MUST_NOT_BE_NULL = "name must not be null";
//  private static final String DOT = ".";
//
//  public enum ModelGroup {
//    PROCESS_VARIABLES("processVariables");
//
//    public final String tag;
//
//    ModelGroup(String tag) {
//      this.tag = tag;
//    }
//  }
//
  private Map<String, Object> model = new HashMap<>();
//
//  void add(@NotNull String key, Object val) {
//    Objects.requireNonNull(key, "key must not be null");
//    model.put(key, val);
//  }
//
//  public void addProcessVariable(@NotNull String name, Object val) {
//    Objects.requireNonNull(name, NAME_MUST_NOT_BE_NULL);
//
//    @SuppressWarnings("unchecked")
//    Map<String, Object> processVariablesModel = (Map<String, Object>) model.get(ModelGroup.PROCESS_VARIABLES.tag);
//    if (processVariablesModel == null) {
//      processVariablesModel = new HashMap<>();
//      add(ModelGroup.PROCESS_VARIABLES.tag, processVariablesModel);
//    }
//    processVariablesModel.put(name, val);
//  }
//
//  public void addProcessVariables(Map<String, ProcessVariableValue> specificAttributes) {
//    if (specificAttributes != null) {
//      for (Entry<String, ProcessVariableValue> entry : specificAttributes.entrySet()) {
//        if (entry.getValue() != null) {
//          addProcessVariable(entry.getKey(), entry.getValue().getValue());
//        }
//      }
//    }
//  }
//
//  public static boolean isUsedInTemplate(String template, @NotNull Model model) {
//    Objects.requireNonNull(model, MODEL_MUST_NOT_BE_NULL);
//
//    if (StringUtils.isBlank(template) || !template.contains(TEMPLATE_MARKER)) {
//      return false;
//    }
//    return template.contains(TEMPLATE_MARKER.concat(model.tag));
//  }
//
//  public static boolean isUsedInTemplate(String template, @NotNull ModelGroup model,
//      @NotNull String name) {
//    Objects.requireNonNull(name, NAME_MUST_NOT_BE_NULL);
//    Objects.requireNonNull(model, MODEL_MUST_NOT_BE_NULL);
//
//    if (StringUtils.isBlank(template) || !template.contains(TEMPLATE_MARKER)) {
//      return false;
//    }
//    return template.contains(TEMPLATE_MARKER.concat(model.tag).concat(DOT).concat(name));
//  }
//
//  public static boolean isAnyUsedInTemplate(String template, @NotNull ModelGroup modelGroup,
//      @NotNull Collection<String> names) {
//    if (StringUtils.isEmpty(template)) {
//      return false;
//    }
//    return names.stream().anyMatch(name -> isUsedInTemplate(template, modelGroup, name));
//  }
//
//  public static boolean isUsedInTemplate(BabelText babelText, @NotNull Model model) {
//    Objects.requireNonNull(model, MODEL_MUST_NOT_BE_NULL);
//
//    String template = getDefaultTemplate(babelText);
//
//    if (StringUtils.isBlank(template) || !template.contains(TEMPLATE_MARKER)) {
//      return false;
//    }
//    return template.contains(TEMPLATE_MARKER.concat(model.name()));
//  }
//
//  private static String getDefaultTemplate(BabelText babelText) {
//    if (babelText == null || babelText.getTranslations() == null || babelText.getTranslations().size() == 0) {
//      return null;
//    }
//    String defaultLanguage = babelText.getDefaultLanguage();
//    if (StringUtils.isEmpty(defaultLanguage)) {
//      defaultLanguage = "en";
//    }
//    return babelText.getTranslations().get(defaultLanguage);
//  }
//
//  public static boolean isUsedInTemplate(BabelText babelText, @NotNull ModelGroup model,
//      @NotNull String name) {
//    Objects.requireNonNull(name, NAME_MUST_NOT_BE_NULL);
//    Objects.requireNonNull(model, MODEL_MUST_NOT_BE_NULL);
//
//    String template = getDefaultTemplate(babelText);
//
//    if (StringUtils.isBlank(template) || !template.contains(TEMPLATE_MARKER)) {
//      return false;
//    }
//    return template.contains(TEMPLATE_MARKER.concat(model.name()).concat(DOT).concat(name));
//  }


}
