package eu.europa.ec.cc.processcentre.config;

import static java.util.EnumSet.copyOf;
import static java.util.EnumSet.noneOf;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.trimToNull;
import static org.apache.commons.text.CaseUtils.toCamelCase;

import eu.europa.ec.cc.configuration.domain.ConfigurationSet;
import eu.europa.ec.cc.configuration.domain.ConfigurationSet.ConfigurationSetMetadata;
import jakarta.validation.constraints.NotNull;
import java.util.EnumSet;
import lombok.Getter;

/**
 * @author <a href="mailto:Octavian.NITA@ext.ec.europa.eu">Octavian NITA</a>
 * @version $Id$
 */
@Getter
public enum ConfigType {

  PROCESS_TYPE(true, true),
  PROCESS_RESULT("Result card", true),
  PROCESS_DETAIL(true),
  PROCESS_LAUNCHER,
  PROCESS_LAUNCHER_V2,
  PROCESS_LOCAL_MENU,
  PROCESS_FLOW_OVERVIEW,
  PROCESS_PLANNING,
  PROCESS_PLANNING_SCENARIO,
  PROCESS_HISTORY,
  PROCESS_SORTABLE_FIELDS,
  WORKSPACE;

  @NotNull
  private final String title;

  /**
   * {@code true} if updating in the configuration repository requires process type refresh in the PC database
   */
  private final boolean updatable;

  /**
   * {@code true} if updating in the configuration repository requires process instance refresh in the PC database
   */
  private final boolean updatableInProcess;

  private static final EnumSet<ConfigType> updatableConfigTypes = noneOf(ConfigType.class);

  private static final EnumSet<ConfigType> updatableInProcessConfigTypes = noneOf(ConfigType.class);

  static {
    for (final var configType : values()) {
      if (configType.isUpdatable()) {
        updatableConfigTypes.add(configType);
      }
      if (configType.isUpdatableInProcess()) {
        updatableInProcessConfigTypes.add(configType);
      }
    }
  }

  ConfigType(String title, boolean updatable, boolean updatableInProcess) {

    title = trimToNull(title);
    if (isEmpty(title)) {
      var name = name();
      if (name.startsWith("PROCESS_")) {
        name = name.substring(8);
      }
      this.title = capitalize(name.toLowerCase().replace('_', ' ').trim());
    } else {
      this.title = title;
    }

    this.updatable = updatable;
    this.updatableInProcess = updatableInProcess;
  }

  ConfigType(String title, boolean updatableInProcess) {
    this(title, false, updatableInProcess);
  }

  ConfigType(boolean updatable, boolean updatableInProcess) {
    this(null, updatable, updatableInProcess);
  }

  ConfigType(boolean updatableInProcess) {
    this(null, false, updatableInProcess);
  }

  ConfigType() {
    this(null, false, false);
  }

  @Override
  public String toString() {
    return toCamelCase(name(), false, '_');
  }

  @NotNull
  public static EnumSet<ConfigType> getUpdatables() {
    return copyOf(updatableConfigTypes);
  }

  @NotNull
  public static EnumSet<ConfigType> getUpdatablesInProcess() {
    return copyOf(updatableInProcessConfigTypes);
  }

  public boolean typeOf(String configType) {
    return toString().equals(configType);
  }

  public boolean typeOf(ConfigurationSetMetadata configMeta) {
    return configMeta != null && typeOf(configMeta.getConfigurationType());
  }

  public boolean typeOf(ConfigurationSet configSet) {
    return configSet != null && typeOf(configSet.getMetadata());
  }
}
