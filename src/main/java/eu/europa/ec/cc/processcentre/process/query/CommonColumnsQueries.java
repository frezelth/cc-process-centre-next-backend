package eu.europa.ec.cc.processcentre.process.query;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CommonColumnsQueries {

  List<String> findCommonColumns(Set<Map<String, String>> contexts);

}
