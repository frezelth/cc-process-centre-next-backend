package eu.europa.ec.cc.processcentre.translation.repository;

import eu.europa.ec.cc.processcentre.translation.TranslationAttribute;
import java.util.Collection;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TranslationMapper {

  void insertOrUpdateTranslations(Collection<InsertOrUpdateTranslationsParam> translations);

  void deleteTranslations(DeleteTranslationsParam param);

  Map<String, String> findTranslationsForAttribute(String processInstanceId, TranslationAttribute attribute);

}
