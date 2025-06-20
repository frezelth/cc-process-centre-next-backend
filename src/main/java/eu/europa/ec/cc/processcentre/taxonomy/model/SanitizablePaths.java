package eu.europa.ec.cc.processcentre.taxonomy.model;

import java.util.List;

public interface SanitizablePaths {

  String TAXONOMY_ROOT = "ProcessCentre/";

  List<String> getTaxonomyPaths();

  void setTaxonomyPaths(List<String> taxonomyPath);

  static String sanitize(String taxonomyPath) {
    return Sanitizable.sanitize(taxonomyPath);
  }

  static List<String> sanitize(List<String> taxonomyPaths) {
    return Sanitizable.sanitize(taxonomyPaths);
  }

  static <S extends SanitizablePaths> S sanitize(S s) {
    if (s == null || s.getTaxonomyPaths() == null) {
      return s;
    }
    s.setTaxonomyPaths(sanitize(s.getTaxonomyPaths()));
    return s;
  }
}
