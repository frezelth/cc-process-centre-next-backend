package eu.europa.ec.cc.processcentre.taxonomy.model;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public interface Sanitizable {

  String TAXONOMY_ROOT = "ProcessCentre/";


  static String sanitize(String taxonomyPath){
    String sanitized = taxonomyPath;
    if (StringUtils.isNotEmpty(sanitized)){
      // remove the first / if needed
      if (sanitized.startsWith("/")){
        sanitized = sanitized.substring(1);
      }
      // remove the root / if needed
      if (sanitized.startsWith(TAXONOMY_ROOT)){
        sanitized = sanitized.substring(TAXONOMY_ROOT.length());
      }
    }
    return sanitized;
  }


  static List<String> sanitize(List<String> taxonomyPaths) {
    return taxonomyPaths.stream().map(Sanitizable::sanitize).distinct().collect(Collectors.toList());
  }

//  static String sanitize(@NonNull String taxonomyPaths) {
//    return taxonomyPaths.map(Sanitizable::sanitize).distinct().collect(Collectors.toList());
//  }

}
