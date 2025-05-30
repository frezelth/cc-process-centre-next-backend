package eu.europa.ec.cc.processcentre;

import eu.europa.ec.cc.configuration.ConfigLibraryConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnProperty(name = { "configuration.app-name" })
@Import(ConfigLibraryConfiguration.class)
public class ConfigAdminConfig {

}
