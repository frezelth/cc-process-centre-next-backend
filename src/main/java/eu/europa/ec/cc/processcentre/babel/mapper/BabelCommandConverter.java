package eu.europa.ec.cc.processcentre.babel.mapper;

import eu.europa.ec.cc.babel.proto.TranslationCreated;
import eu.europa.ec.cc.babel.proto.TranslationUpdated;
import eu.europa.ec.cc.processcentre.babel.repository.model.CreateOrUpdateBabelQueryParam;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BabelCommandConverter {

  @Mapping(source = "default", target = "defaultTranslation")
  @Mapping(source = "text", target = "translatedText")
  CreateOrUpdateBabelQueryParam toQueryParam(TranslationCreated createTranslation);

  @Mapping(source = "default", target = "defaultTranslation")
  @Mapping(source = "text", target = "translatedText")
  CreateOrUpdateBabelQueryParam toQueryParam(TranslationUpdated createTranslation);

}
