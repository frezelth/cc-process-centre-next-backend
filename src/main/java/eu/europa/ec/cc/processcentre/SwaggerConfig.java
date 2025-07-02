package eu.europa.ec.cc.processcentre;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.servers.Server;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.properties.SwaggerUiConfigParameters;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springdoc.webmvc.ui.SwaggerIndexPageTransformer;
import org.springdoc.webmvc.ui.SwaggerIndexTransformer;
import org.springdoc.webmvc.ui.SwaggerWelcomeCommon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class SwaggerConfig {


  @Value("${swagger.service.url:}")
  String serviceUrl;

  /**
   * The constant PRESETS.
   */
  private static final String PRESETS = "presets: [";

  @Bean
  public OpenAPI api(@Autowired(required = false) BuildProperties buildProperties) {
    final var title = buildProperties == null ? "Process Centre" : buildProperties.getName();
    final var version = buildProperties == null ? "N/A" : buildProperties.getVersion();
    return
        new OpenAPI()
            .info(new Info()
                .title(title)
                .version(version)
                .description("Compass Corporate Process Centre REST API")
                .contact(new Contact().url("https://citnet.tech.ec.europa.eu/CITnet/confluence/display/PROCESS"))
                .summary("Process Centre is a Compass Corporate Micro-service that allows browsing, displaying, filtering and monitoring (metrolines) of processes running in different workflow engine/back-end systems (example: COMPASS, RDIS2, WAVE...). "))
            .servers(generateServer().map(List::of).orElse(null));
  }

  @Bean
  public OpenApiCustomizer customerGlobalHeaderOpenApiCustomiser() {
    return openApi -> openApi.getPaths().values().stream().flatMap(pathItem -> pathItem.readOperations().stream())
        .forEach(operation -> {
          if (operation.getParameters() != null && operation.getParameters().stream().noneMatch(parameter -> Objects.equals(parameter.getName(), "OpenIdToken"))) {
            operation.addParametersItem(
                new HeaderParameter().name("OpenIdToken").schema(new StringSchema()));
          }
        });
  }

  private Optional<Server> generateServer(){
    if (StringUtils.isNotEmpty(serviceUrl)){
      return Optional.of(new Server()
          .url(serviceUrl));
    }
    return Optional.empty();
  }

  @Bean
  public SwaggerIndexTransformer swaggerIndexTransformer(
      org.springdoc.core.properties.SwaggerUiConfigProperties a,
      org.springdoc.core.properties.SwaggerUiOAuthProperties b,
      SwaggerWelcomeCommon d, ObjectMapperProvider o) {
    return new EuLoginSwaggerIndexTransformer(a, b, d, o);
  }

  static class EuLoginSwaggerIndexTransformer extends SwaggerIndexPageTransformer {

    public EuLoginSwaggerIndexTransformer(
        org.springdoc.core.properties.SwaggerUiConfigProperties swaggerUiConfig,
        org.springdoc.core.properties.SwaggerUiOAuthProperties swaggerUiOAuthProperties,
        SwaggerWelcomeCommon swaggerWelcomeCommon,
        ObjectMapperProvider objectMapperProvider) {
      super(swaggerUiConfig, swaggerUiOAuthProperties,
          swaggerWelcomeCommon,
          objectMapperProvider);
    }

    @Override
    protected String defaultTransformations(SwaggerUiConfigParameters swaggerUiConfigParameters,
        InputStream inputStream) throws IOException {
      return changeAuthorizationHeader(super.defaultTransformations(swaggerUiConfigParameters, inputStream));
    }
//    @Override
//    protected String defaultTransformations(InputStream inputStream) throws IOException {
//      return changeAuthorizationHeader(super.defaultTransformations(inputStream));
//    }

    protected String changeAuthorizationHeader(String html) {
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append("requestInterceptor: (request) => {\n");
      stringBuilder.append("\t\t\tif (!request.headers.OpenIdToken) return request;\n");
      stringBuilder.append("request.headers['");
      stringBuilder.append("Authorization");
      stringBuilder.append("'] = request.headers.OpenIdToken;\n");
//      stringBuilder.append("request.headers.remove('OpenIdToken');");
      stringBuilder.append("\t\t\treturn request;\n");
      stringBuilder.append("\t\t},\n");
      stringBuilder.append("\t\t" + PRESETS);
      return html.replace(PRESETS, stringBuilder.toString());
    }

  }

}
