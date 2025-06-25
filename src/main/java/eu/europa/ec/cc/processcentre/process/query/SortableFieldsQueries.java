package eu.europa.ec.cc.processcentre.process.query;

import eu.europa.ec.cc.processcentre.config.SortableField;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public interface SortableFieldsQueries {

  List<SortableField> findSortableFields(
      Collection<Map<String, String>> contexts, Locale locale);

}
