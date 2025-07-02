package eu.europa.ec.cc.processcentre;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

@Configuration
public class SpringMVCConfig {

  @Bean
  public LocaleResolver configureLocaleResolver(){
    return new AcceptHeaderLocaleResolver();
  }

}
