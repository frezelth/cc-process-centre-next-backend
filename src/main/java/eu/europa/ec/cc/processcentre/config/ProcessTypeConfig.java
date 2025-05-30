package eu.europa.ec.cc.processcentre.config;

import eu.europa.ec.cc.processcentre.babel.BabelText;
import eu.europa.ec.cc.processcentre.config.AccessRight.Right;
import java.util.List;
import java.util.Map;

public record ProcessTypeConfig(

    BabelText name,

    BabelText titleTemplate,

    String secundaTask,

    String defaultBusinessDomainId,

    String resultCardLayout,

    Map<Right, List<AccessRight>> accessRights,

    Boolean hideSubprocessesOfTheSameType

) {
  public static String CONFIGURATION_TYPE_NAME = "processType";
}
