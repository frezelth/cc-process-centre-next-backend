package eu.europa.ec.cc.processcentre.config;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author <a href="mailto:Cezary-Lukasz.REJCZYK@ext.ec.europa.eu">Cezary Rejczyk</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SortableFieldConfig {

    private List<SortableField> sortableFields;

}
