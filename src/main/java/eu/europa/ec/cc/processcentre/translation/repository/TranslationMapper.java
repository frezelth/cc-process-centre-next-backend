package eu.europa.ec.cc.processcentre.translation.repository;

import java.util.Collection;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TranslationMapper {

  void insertOrUpdateTranslations(Collection<InsertOrUpdateTranslationsParam> translations);

  void deleteTranslationsForAttribute(DeleteTranslationsParam param);

  Map<String, String> findTranslationsForAttribute(FindTranslationsForAttributeQueryParam param);

}
