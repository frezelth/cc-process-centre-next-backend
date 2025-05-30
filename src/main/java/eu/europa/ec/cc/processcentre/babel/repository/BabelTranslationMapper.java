package eu.europa.ec.cc.processcentre.babel.repository;

import eu.europa.ec.cc.processcentre.babel.repository.model.CreateOrUpdateBabelQueryParam;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BabelTranslationMapper {

  void insertBabelTranslation(CreateOrUpdateBabelQueryParam param);

  void updateBabelTranslation(CreateOrUpdateBabelQueryParam param);

}
