package eu.europa.ec.cc.processcentre.babel;

import static eu.europa.ec.cc.babel.proto.ISO6391LanguageCode.UNRECOGNIZED;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.slf4j.LoggerFactory.getLogger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.europa.ec.cc.babel.proto.BabelText.Literal;
import eu.europa.ec.cc.babel.proto.BabelText.Literal.LiteralValue;
import eu.europa.ec.cc.babel.proto.ISO6391LanguageCode;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

@Data
@Builder
@NoArgsConstructor
public class BabelText {

  // A reference in urn syntax identifying the text and its translations.
  //
  // Examples:
  // - 'urn:babel:53d7r1' pointing to a translated text with id '53d7r1', stored in babel
  // - 'urn:ccm2:abc123' pointing to a translated text with id 'abc123', stored in ccm2
//  private String urn;
//
//  // translations by language (ISO6391)
//  private Map<String, String> translations;
//
//  // default language (ISO6391)
//  private String defaultLanguage;
//
//  @JsonIgnore
//  public boolean isEmpty(){
//    return isEmpty(this);
//  }
//
//  public static boolean isEmpty(BabelText babelText){
//    if (babelText == null ){
//      return true;
//    }
//    return StringUtils.isBlank(babelText.urn) && StringUtils.isBlank(babelText.defaultLanguage) && (babelText.translations == null || babelText.translations.isEmpty());
//  }
//
//  public static boolean isEmpty(eu.europa.ec.cc.babel.proto.BabelText proto){
//    if (proto == null || !proto.isInitialized()){
//      return true;
//    }
//    return StringUtils.isEmpty(proto.getUrn()) && CollectionUtils.isEmpty(proto
//      .getLiteral()
//      .getLiteralValuesList());
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
//      eu.europa.ec.cc.babel.proto.BabelText.newBuilder();
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
//          .newBuilder()
//          .setLanguageCode(iso6391Code)
//          .setText(translation)
//          .setDefault(defaultLanguage != null && defaultLanguage.equals(language))
//          .build());
//      });
//      protoBabelText.setLiteral(literalBuilder.build());
//
//    }
//    return protoBabelText.build();
//  }
//
//  public static BabelText convert(eu.europa.ec.cc.babel.proto.BabelText proto) {
//
//    if (isEmpty(proto)){
//      // This is a simplification. In some cases, if the BabelText has default value, the value should be reset.
//      // If it is null, it wasn't set and the value shouldn't be changed. For the time being, this functionality is required by TaskRestored only.
//      return null;
//    }
//
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
//      Map<String, String> translations = Maps.newHashMap();
//      if (CollectionUtils.isNotEmpty(proto.getLiteral().getLiteralValuesList())) {
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
//    Map<String, String> translations = Maps.newHashMap();
//    if (CollectionUtils.isNotEmpty(literal.getLiteralValuesList())) {
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
