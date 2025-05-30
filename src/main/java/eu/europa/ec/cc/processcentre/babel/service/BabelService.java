package eu.europa.ec.cc.processcentre.babel.service;

import eu.europa.ec.cc.babel.proto.ISO6391LanguageCode;
import eu.europa.ec.cc.babel.proto.TranslationCreated;
import eu.europa.ec.cc.babel.proto.TranslationUpdated;
import eu.europa.ec.cc.processcentre.babel.mapper.BabelCommandConverter;
import eu.europa.ec.cc.processcentre.babel.repository.BabelTranslationMapper;
import eu.europa.ec.cc.processcentre.babel.repository.model.CreateOrUpdateBabelQueryParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class BabelService {

  private final BabelTranslationMapper mapper;
  private final BabelCommandConverter converter;

  public BabelService(BabelTranslationMapper mapper,
      BabelCommandConverter converter) {
    this.mapper = mapper;
    this.converter = converter;
  }

  @Transactional
  public void handle(TranslationCreated command){
    if (command.getLanguageCode().equals(ISO6391LanguageCode.UNRECOGNIZED)) {
      LOG.debug("On TranslationCreated - no language code for urn: {}", command.getUrn());
      return;
    }

    CreateOrUpdateBabelQueryParam queryParam = converter.toQueryParam(command);

    if (LOG.isDebugEnabled()){
      LOG.debug("Inserting babel translation urn={}, languageCode={}, defaultTranslation={}",
          queryParam.urn(), queryParam.languageCode(), queryParam.defaultTranslation());
    }

    mapper.insertBabelTranslation(queryParam);
  }

  @Transactional
  public void handle(TranslationUpdated command){
    if (command.getLanguageCode().equals(ISO6391LanguageCode.UNRECOGNIZED)) {
      LOG.debug("On TranslationUpdated - no language code for urn: {}", command.getUrn());
      return;
    }

    CreateOrUpdateBabelQueryParam queryParam = converter.toQueryParam(command);

    if (LOG.isDebugEnabled()){
      LOG.debug("Updating babel translation urn={}, languageCode={}, defaultTranslation={}",
          queryParam.urn(), queryParam.languageCode(), queryParam.defaultTranslation());
    }

    mapper.updateBabelTranslation(queryParam);
  }

}
