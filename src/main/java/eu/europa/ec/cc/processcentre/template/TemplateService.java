package eu.europa.ec.cc.processcentre.template;

import eu.europa.ec.cc.processcentre.repository.ProcessVariableMapper;
import eu.europa.ec.cc.processcentre.repository.model.FindProcessVariableQueryParam;
import eu.europa.ec.cc.processcentre.repository.model.FindProcessVariableQueryResponse;
import freemarker.template.Configuration;
import freemarker.template.Template;
import java.util.Map;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

@Service
public class TemplateService {

  private ProcessVariableMapper processVariableMapper;

  public TemplateService(ProcessVariableMapper processVariableMapper) {
    this.processVariableMapper = processVariableMapper;
  }

  @SneakyThrows
  @Transactional(readOnly = true)
  public String processAccessRightsScopeIdTemplate(String processInstanceId, String template) {

    if (StringUtils.isEmpty(template)) {
      return null;
    }

    if (template.indexOf('$') == 0){
      // no expression found
      return template;
    }

    // retrieve possible variable name in template
    String removeVarPrefix = StringUtils.replace(template, "${", "");
    String varName = StringUtils.replace(removeVarPrefix, "}", "");

    FindProcessVariableQueryResponse processVariable = processVariableMapper.findProcessVariable(
        new FindProcessVariableQueryParam(
            processInstanceId, varName
        ));

    return FreeMarkerTemplateUtils.processTemplateIntoString(
        Template.getPlainTextTemplate(null, template, new Configuration(Configuration.VERSION_2_3_30)),
        Map.of("processVariables", Map.of(varName, processVariable.getVariableValue()))
    );
  }

}
