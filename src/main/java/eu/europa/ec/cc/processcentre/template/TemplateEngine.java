package eu.europa.ec.cc.processcentre.template;

import static java.util.Objects.requireNonNull;

import freemarker.template.Configuration;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Used for template-based data generation. By default, the {@code template.engine} topic/logger
 * should be set to {@code OFF} since it can generate a lot of output that might pollute logs.
 *
 * @author <a href="mailto:Octavian.NITA@ext.ec.europa.eu">Octavian NITA</a>
 * @version $Id$
 */
@Component
@Slf4j(topic = "template.engine")
public class TemplateEngine extends TemplateEngineAbstract {

  @Autowired
  public TemplateEngine(
      @NotNull @Qualifier("defaultScopedFreeMarkerConfiguration") Configuration freeMarker) {
    super(requireNonNull(freeMarker));
  }

  /** Template engine-specific Spring configuration. */
  @org.springframework.context.annotation.Configuration
  public static class TemplateEngineConfig {

    /**
     * Limiting the scope of a FreeMarker {@link Configuration} instance to the current request:
     * <ul>
     * <li>allows caching the templates throughout the life span of the request, thus reducing
     * content generation overhead due to repeated template compilations</li>
     * <li>allows changes to an item type configuration, for example, to be made in the database
     * with immediate effect in subsequent requests (i.e. templates are not cached for hours, days,
     * etc.)</li>
     * </ul>
     *
     * @return {@link Configuration FreeMarker configuration instance} to be reused throughout the
     *         scope of a request
     * @see <a href=
     *      "https://freemarker.apache.org/docs/pgui_quickstart_createconfiguration.html">Create a
     *      configuration instance</a> for why we need so much configuration 'by default'
     */
    // @RequestScope
    @Bean("defaultScopedFreeMarkerConfiguration")
    public Configuration freeMarkerConfiguration(
        @Value("${service.debug:false}") boolean debugEnabled) {
      return getFreeMarkerConfiguration(debugEnabled);
    }

  }
}
