package eu.europa.ec.cc.processcentre.config;

import eu.europa.ec.cc.processcentre.babel.BabelText;
import eu.europa.ec.cc.processcentre.config.AccessRight.Right;
import java.util.List;
import java.util.Map;

public record ProcessTypeConfig(

    // the process name is superseded by the title template,
    // if title template is present, use it for the process title
    // otherwise use the type name
    BabelText name,

    BabelText titleTemplate,

    String secundaTask,

    String defaultBusinessDomainId,

    String resultCardLayout,

    Map<Right, List<AccessRight>> accessRights,

    Boolean hideSubprocessesOfTheSameType

) {
  public static String CONFIGURATION_TYPE_NAME = "processType";
  public static String CONFIGURATION_RESULT_CARD_NAME = "processResult";
  public static String PROCESS_VARIABLES_PREFIX = "processVariables.";
}
