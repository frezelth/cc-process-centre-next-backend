package eu.europa.ec.cc.processcentre.translation;

import eu.europa.ec.cc.processcentre.ProcessCentreNextApplicationTests;
import eu.europa.ec.cc.processcentre.babel.BabelText;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class TranslationServiceTest extends ProcessCentreNextApplicationTests {

  @Autowired
  private TranslationService translationService;

  @Test
  public void testInsertInternalTranslations() {
    translationService.insertOrUpdateTranslations(
        TranslationObjectType.PROCESS, "1", TranslationAttribute.PROCESS_TYPE_NAME,
        new BabelText(null, Map.of("en", "en translation"), "en")
    );
  }

}
