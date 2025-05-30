package eu.europa.ec.cc.processcentre.babel;

import eu.europa.ec.cc.babel.proto.ISO6391LanguageCode;
import eu.europa.ec.cc.babel.proto.TranslationCreated;
import eu.europa.ec.cc.babel.proto.TranslationUpdated;
import eu.europa.ec.cc.processcentre.ProcessCentreNextApplicationTests;
import eu.europa.ec.cc.processcentre.babel.service.BabelService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

public class BabelServiceTest extends ProcessCentreNextApplicationTests  {

  @Autowired
  private BabelService babelService;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Test
  void onTranslationCreated(){
    TranslationCreated command = TranslationCreated.newBuilder()
        .setUrn("urn:babel:processcentre:babel:1.0")
        .setLanguageCode(ISO6391LanguageCode.en)
        .setText("my text")
        .setDefault(true)
        .build();

    babelService.handle(command);
  }

  @Test
  @Sql(statements = "INSERT INTO T_BABEL_TRANSLATION (urn, language_code, translated_text) values "
      + "('urn:babel:processcentre:babel:1.0', 'en', 'text_translation')")
  void onTranslationUpdated(){
    TranslationUpdated command = TranslationUpdated.newBuilder()
        .setUrn("urn:babel:processcentre:babel:1.0")
        .setLanguageCode(ISO6391LanguageCode.en)
        .setText("my text")
        .setDefault(true)
        .build();

    babelService.handle(command);

    // check the update
    String s = jdbcTemplate.queryForObject(
        "select translated_text from T_BABEL_TRANSLATION where urn = 'urn:babel:processcentre:babel:1.0'",
        String.class);

    Assertions.assertEquals("my text", s);
  }

}
