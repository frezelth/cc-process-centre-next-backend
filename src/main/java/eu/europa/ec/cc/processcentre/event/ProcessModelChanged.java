package eu.europa.ec.cc.processcentre.event;

import eu.europa.ec.cc.processcentre.template.TemplateModel.Model;
import java.util.Set;

public record ProcessModelChanged(
    String processInstanceId,
    Set<Model> changes
) {

}
