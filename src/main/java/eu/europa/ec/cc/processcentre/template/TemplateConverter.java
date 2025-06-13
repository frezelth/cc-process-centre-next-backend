package eu.europa.ec.cc.processcentre.template;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Rafal Oleksiak
 *
 */
@Component
@Slf4j
public class TemplateConverter {

  final TemplateEngine templateEngine;
  static final String patternStr = "\\\\\"";
  static final Pattern pattern = Pattern.compile(patternStr);

  @Autowired
  public TemplateConverter(TemplateEngine templateEngine) {
    this.templateEngine = templateEngine;
  }

  public static String cleanSerializedTemplate(String template) {
    String replacement = "\"";
    Matcher matcher = pattern.matcher(template);

    return matcher.replaceAll(replacement).replace("\\\\", "\\");
  }

  public String convert(String processId, String template, TemplateModel templateModel) {
    if (templateModel == null || templateModel.getModel().isEmpty() || StringUtils.isEmpty(template) || !template.contains(TemplateModel.TEMPLATE_MARKER)) {
      return template;
    }

    template = cleanSerializedTemplate(template);


    CL cl = new CL();

    String val = template;
    try {
      val = templateEngine
          .newMerge()
          .templateName(processId, template)
          .template(template)
          .model(templateModel.getModel())
          .exceptionHandler(ex -> cl.addError(ex.getMessage()))
          .process();
    } catch (Exception e) {
      LOG.error("Cannot generate value from template {} for processId: {}", template,
          processId, e);
      cl.addError(e.getMessage());
    }

    if (!cl.getErrors().isEmpty()) {
      LOG.warn("Error(s) while parsing template for processId: {} \n{} ", processId,
          cl.getErrors());
    }
    return val;
  }
  
  @Data
  static class CL {
    
   List<String> errors = new ArrayList<>();
    
    public void addError(String error) {
      errors.add(error);
    }    
  }
}
