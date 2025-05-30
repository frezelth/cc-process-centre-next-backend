package eu.europa.ec.cc.processcentre.repository;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TranslationMapper {

  void insertOrUpdateTranslation();

}
