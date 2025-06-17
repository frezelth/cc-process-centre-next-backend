package eu.europa.ec.cc.processcentre.template;

import java.util.Collection;
import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TemplateExtractorTest {

  @Test
  void testTemplateExtractor_match_normal(){
    String template = "freemarker normal template with var1 : ${var1} in the middle";
    boolean match = TemplateExtractor.matchAny(template, Collections.singleton("var1"));
    Assertions.assertTrue(match);
  }

  @Test
  void testTemplateExtractor_match_nested(){
    String template = "freemarker nested variable template with var1 : ${processVariables.var1} in the middle";
    boolean match = TemplateExtractor.matchAny(template, Collections.singleton("var1"));
    Assertions.assertTrue(match);
  }

  @Test
  void testTemplateExtractor_extract_normal(){
    String template = "freemarker nested variable template with var1 : ${var1} in the middle";
    Collection<String> variables = TemplateExtractor.extractVariables(template);
    Assertions.assertEquals(Collections.singleton("var1"), variables);
  }

  @Test
  void testTemplateExtractor_extract_nested(){
    String template = "freemarker nested variable template with var1 : ${processVariables.var1} in the middle";
    Collection<String> variables = TemplateExtractor.extractVariables(template);
    Assertions.assertEquals(Collections.singleton("processVariables.var1"), variables);
  }
}
