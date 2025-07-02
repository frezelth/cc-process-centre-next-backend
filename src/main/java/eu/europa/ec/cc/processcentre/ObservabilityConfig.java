package eu.europa.ec.cc.processcentre;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Configuration
public class ObservabilityConfig {

  @Controller
  static class ObservabilityEndpointForwarder {

    @GetMapping("/metrics")
    public ModelAndView forwardToMetrics(ModelMap model) {
      return new ModelAndView("forward:/actuator/prometheus", model);
    }

    @GetMapping("/health")
    public ModelAndView forwardToHealth(ModelMap model) {
      return new ModelAndView("forward:/actuator/health", model);
    }
  }
}
