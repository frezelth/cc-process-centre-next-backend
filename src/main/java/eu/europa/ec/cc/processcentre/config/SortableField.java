package eu.europa.ec.cc.processcentre.config;

import static java.lang.Boolean.TRUE;
import static java.util.Locale.ENGLISH;

import java.util.List;
import java.util.Locale;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author <a href="mailto:Cezary-Lukasz.REJCZYK@ext.ec.europa.eu">Cezary Rejczyk</a>
 * @author <a href="mailto:Octavian.NITA@ext.ec.europa.eu">Octavian Nita</a>
 * @version $Id$
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SortableField {

  /**
   * The field name provides the identity of a sortable field.
   * This makes sense since we only use the name to generate
   * the sorting query clause, no matter the field's domain (or context, for that matter).
   */
  @EqualsAndHashCode.Include
  private String field;

  private int propertyOrder;

  private List<SortableFieldTranslation> label;

  public String label(Locale locale) {

    if (label == null || label.isEmpty()) {
      return field;
    }

    final var lang = (locale == null ? ENGLISH : locale).getLanguage();

    String translation = null;
    for (final var l : label) {
      if (lang.equals(l.getLanguage())) {
        translation = l.getValue();
        break;
      }
      if (TRUE.equals(l.getDefaultTranslation())) {
        translation = l.getValue();
      }
    }
    return translation == null ? field : translation;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SortableFieldTranslation {

    private String value;

    private String language;

    private Boolean defaultTranslation;
  }
}
