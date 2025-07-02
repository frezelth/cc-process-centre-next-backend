package eu.europa.ec.cc.processcentre;

import brave.TracingCustomizer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class ZipkinConfig {

  @Bean
  public TracingCustomizer tracingCustomizer(Environment environment){
    return builder -> {
      String zipkinServiceName = environment.getProperty("application.zipkin.service-name");
      if (StringUtils.isNotEmpty(zipkinServiceName)) {
        builder.localServiceName(zipkinServiceName);
      }
    };
  }

}
