package eu.europa.ec.cc.processcentre.template;

import static freemarker.template.Configuration.VERSION_2_3_30;
import static java.util.TimeZone.getTimeZone;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.text.StringEscapeUtils.escapeJava;

import freemarker.cache.StringTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.core.InvalidReferenceException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class TemplateEngineAbstract {

  protected final Configuration freeMarker;

  public TemplateEngineAbstract(Configuration freeMarker) {
    this.freeMarker = freeMarker;
  }

  protected Configuration getFreeMarker() {
    return freeMarker;
  }

  /** Factory method for models adapted to work well with the specific template engine used. */
  public Map<String, Object> newTemplateModel() {
    return new JsonSafeTemplateModel();
  }

  /** Factory method for models adapted to work well with the specific template engine used. */
  public Map<String, Object> newTemplateModel(String modelId) {
    return new JsonSafeTemplateModel(modelId);
  }

  public Merge newMerge() {
    return new Merge();
  }

  protected static Configuration getFreeMarkerConfiguration(boolean debugEnabled) {
    final var cfg = new Configuration(VERSION_2_3_30);

    // Set the preferred charset template files are stored in
    cfg.setDefaultEncoding("UTF-8");
    // Don't log exceptions inside FreeMarker that it will throw at you anyway
    cfg.setLogTemplateExceptions(false);
    // Wrap unchecked exceptions thrown during template processing into TemplateException-s
    cfg.setWrapUncheckedExceptions(true);
    // Do not fall back to higher scopes when reading a null loop variable
    cfg.setFallbackOnNullLoopVariable(false);

    cfg.setNumberFormat("computer");
    cfg.setBooleanFormat("true,false");
    cfg.setLocale(Locale.of("en", "BE"));
    cfg.setDateFormat("dd/MM/yyyy");
    cfg.setTimeFormat("HH:mm");
    cfg.setDateTimeFormat("dd/MM/yyyy HH:mm");
    cfg.setTimeZone(getTimeZone("Europe/Brussels"));

    // Set how errors will appear (see
    // https://freemarker.apache.org/docs/app_faq.html#faq_picky_about_missing_vars)
    cfg.setTemplateExceptionHandler((tex, env, out) -> {
      // When debugging, leave a trace in the output to hint at the error location
      if (debugEnabled && !env.isInAttemptBlock()) {
        final var pw = (out instanceof PrintWriter) ? (PrintWriter) out : new PrintWriter(out);
        pw.print("${" + tex.getBlamedExpressionString() + "}");
        pw.flush(); // to commit the HTTP response, if need be
      }
      // Log the error to have some sort of tracking
      if (!(tex instanceof InvalidReferenceException)) {
        LOG.debug("A template processing error has occurred", tex);
      }
    });

    // We load/parse templates from String values
    cfg.setTemplateLoader(new StringTemplateLoader());

    return cfg;
  }

  /**
   * Template model which retrieves the values JSON-escaped (but the forward-slash is kept as-is,
   * which is correct in JSON).
   *
   * @see <a href=
   *   "https://commons.apache.org/proper/commons-text/apidocs/org/apache/commons/text/StringEscapeUtils.html#escapeJava-java.lang.String-">The
   *   only difference between Java strings and JavaScript strings is that in JavaScript, a
   *   single quote and forward-slash (/) are escaped.</a>
   */
  @EqualsAndHashCode(callSuper = true)
  public static class JsonSafeTemplateModel extends HashMap<String, Object> {

    /**
     * Does not necessarily have to be unique among all created models. Helps when debugging
     * templates...
     */
    final String modelId;

    public JsonSafeTemplateModel() {
      this("");
    }

    public JsonSafeTemplateModel(String modelId) {
      this.modelId = modelId;
    }

    @Override
    public Object get(Object key) {
      return getOrDefault(key, null);
    }

    @Override
    public Object getOrDefault(Object key, Object defaultValue) {
      Object value = super.get(key);
      if (value == null) {
        value = defaultValue;
      }

      return value instanceof CharSequence ? escapeJava(String.valueOf(value)) : value;
    }
  }

  public class Merge {

    /** For on-the-fly, not cached templates the name can remain null. */
    private String templateName;

    private Supplier<String> templateSupplier;

    private Map<String, Object> model;

    private Consumer<Exception> exceptionHandler;

    public Merge templateName(Object... templateNameParts) {
      this.templateName = join(templateNameParts, '.');
      return this;
    }

    public Merge template(String templateContent) {
      template(() -> templateContent);
      return this;
    }

    public Merge template(Supplier<String> templateSupplier) {
      this.templateSupplier = templateSupplier;
      return this;
    }

    public Merge model(Map<String, Object> model) {
      this.model = model;
      return this;
    }

    public Merge exceptionHandler(Consumer<Exception> exceptionHandler) {
      this.exceptionHandler = exceptionHandler;
      return this;
    }

    public String process() {
      try {

        // 1. Ensure custom error handling for each merge in particular
        final Configuration freeMarker = freeMarker();

        // 2. Try to get the template from the FreeMarker Configuration cache or, if not found,
        // compile and cache it
        Template template = getOrCompileTemplate();

        // 3. Try to create the template on-the-fly (hoping that the performance hit would not be too
        // high)
        if (template == null) { // the FreeMarker Configuration might not be using StringTemplateLoader...
          final String templateContent = templateContent();
          if (templateContent != null) {
            template = new Template(templateName, templateContent, freeMarker);
          }
        }

        // 4. At this point, we should have a compiled template, ready to be merged with the model
        if (template == null) {
          return null;
        }

        final StringWriter result = new StringWriter();
        template.process(model == null ? newTemplateModel() : model, result);
        return result.toString();

      } catch (Exception ex) {
        LOG.error("Failed to merge template; falling back on the template content, if any", ex);
        return templateContent();
      }
    }

    private String templateContent() {
      return templateSupplier == null ? null : templateSupplier.get();
    }

    private Configuration freeMarker() {
      if (exceptionHandler == null) { // no custom error handling for the current merge
        return getFreeMarker();
      }

      final Configuration freeMarker =
        (Configuration) getFreeMarker().clone();
      final TemplateExceptionHandler currentTemplateExceptionHandler =
        freeMarker.getTemplateExceptionHandler();
      if (currentTemplateExceptionHandler == null) {
        freeMarker.setTemplateExceptionHandler((tex, env, out) -> exceptionHandler.accept(tex));
      } else {
        freeMarker.setTemplateExceptionHandler((tex, env, out) -> {
          currentTemplateExceptionHandler.handleTemplateException(tex, env, out);
          exceptionHandler.accept(tex);
        });
      }
      return freeMarker;
    }

    private Template getOrCompileTemplate() {
      if (templateName == null) {
        return null;
      }

      Template template = null;
      try {
        try {
          template = getFreeMarker().getTemplate(templateName);
        } catch (TemplateNotFoundException tnfe) {
          // Try to parse the template and cache the result if the Configuration uses a
          // StringTemplateLoader
          final TemplateLoader templateLoader = getFreeMarker().getTemplateLoader();
          if (templateLoader instanceof StringTemplateLoader) {
            final String templateContent = templateContent();
            if (templateContent != null) {
              ((StringTemplateLoader) templateLoader).putTemplate(templateName, templateContent);

              // Simply putting the template content in the loader does not compile it, one has to
              // retrieve the template
              // itself (see https://freemarker.apache.org/docs/pgui_config_templateloading.html)
              template = getFreeMarker().getTemplate(templateName);
            }
          }
        }
      } catch (Exception ex) {
        LOG.debug(
          "Cannot obtain (possibly not cached) template named {}; trying to generate one on-the-fly...",
          templateName,
          ex
        );
      }
      return template;
    }
  }
}
