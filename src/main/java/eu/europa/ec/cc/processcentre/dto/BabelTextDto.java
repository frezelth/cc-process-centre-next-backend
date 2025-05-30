package eu.europa.ec.cc.processcentre.dto;

import static eu.europa.ec.cc.babel.proto.ISO6391LanguageCode.UNRECOGNIZED;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.slf4j.LoggerFactory.getLogger;

import eu.europa.ec.cc.babel.proto.BabelText.Literal;
import eu.europa.ec.cc.babel.proto.BabelText.Literal.LiteralValue;
import eu.europa.ec.cc.babel.proto.ISO6391LanguageCode;
import eu.europa.ec.cc.processcentre.babel.BabelText;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

public record BabelTextDto(
    String urn,
    Map<String, String> translations,
    String defaultLanguage
) {

//  public boolean isEmpty(){
//    return StringUtils.isBlank(this.urn) && StringUtils.isBlank(this.defaultLanguage) && (this.translations == null || this.translations.isEmpty());
//  }
//
//  @NotNull
//  public static eu.europa.ec.cc.babel.proto.BabelText convert(BabelText babel) {
//    if (babel == null) {
//      return eu.europa.ec.cc.babel.proto.BabelText.getDefaultInstance();
//    }
//
//    final String urn = babel.getUrn();
//    final String defaultLanguage = babel.getDefaultLanguage();
//    final Map<String, String> translations = babel.getTranslations();
//
//    final eu.europa.ec.cc.babel.proto.BabelText.Builder protoBabelText =
//        eu.europa.ec.cc.babel.proto.BabelText.newBuilder();
//    if (isNotBlank(urn)) {
//
//      protoBabelText.setUrn(urn);
//
//    } else if (translations != null && !translations.isEmpty()) {
//
//      final Literal.Builder literalBuilder = Literal.newBuilder();
//      translations.forEach((language, translation) -> {
//
//        ISO6391LanguageCode iso6391Code;
//        try {
//          iso6391Code = ISO6391LanguageCode.valueOf(language);
//        } catch (Exception ex) {
//          getLogger(BabelText.class).debug("", ex);
//          iso6391Code = UNRECOGNIZED;
//        }
//
//        literalBuilder.addLiteralValues(LiteralValue
//            .newBuilder()
//            .setLanguageCode(iso6391Code)
//            .setText(translation)
//            .setDefault(defaultLanguage != null && defaultLanguage.equals(language))
//            .build());
//      });
//      protoBabelText.setLiteral(literalBuilder.build());
//
//    }
//    return protoBabelText.build();
//  }
//
//  public static BabelText convert(eu.europa.ec.cc.babel.proto.BabelText proto) {
//    return convertIfNotNull(proto);
//  }
//
//  public static BabelText convertIfNotNull(eu.europa.ec.cc.babel.proto.BabelText proto) {
//    if (proto == null) {
//      return null;
//    }
//
//    BabelText babelText = new BabelText();
//
//    if (StringUtils.isNotEmpty(proto.getUrn())){
//      babelText.setUrn(proto.getUrn());
//    }
//    if (proto.hasLiteral()){
//      Map<String, String> translations = new HashMap<>();
//      if (!CollectionUtils.isEmpty(proto.getLiteral().getLiteralValuesList())) {
//        for (LiteralValue literalValue : proto.getLiteral().getLiteralValuesList()) {
//          translations.put(literalValue.getLanguageCode().name(), literalValue.getText());
//          // set the default language
//          if (literalValue.getDefault()){
//            babelText.setDefaultLanguage(literalValue.getLanguageCode().name());
//          }
//        }
//      }
//      babelText.setTranslations(translations);
//    }
//    return babelText;
//  }
//
//  public static BabelText convert(Literal literal) {
//
//    if ((Literal.getDefaultInstance().equals(literal))) {
//      return null;
//    }
//
//    BabelText babelText = new BabelText();
//
//
//    Map<String, String> translations = new HashMap<>();
//    if (!CollectionUtils.isEmpty(literal.getLiteralValuesList())) {
//      for (LiteralValue literalValue : literal.getLiteralValuesList()) {
//        translations.put(literalValue.getLanguageCode().name(), literalValue.getText());
//        // set the default language
//        if (literalValue.getDefault()) {
//          babelText.setDefaultLanguage(literalValue.getLanguageCode().name());
//        }
//      }
//    }
//    babelText.setTranslations(translations);
//
//    return babelText;
//  }

}
