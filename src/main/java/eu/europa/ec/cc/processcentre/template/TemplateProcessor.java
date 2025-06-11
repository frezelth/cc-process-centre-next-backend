package eu.europa.ec.cc.processcentre.template;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

public class TemplateProcessor {

  private Map<String, Object> model = new HashMap<>();

  private TemplateProcessor(TemplateModel model){

  }

  public void processTemplate(String processInstanceId, String template){

  }

}
