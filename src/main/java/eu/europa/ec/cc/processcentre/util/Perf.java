package eu.europa.ec.cc.processcentre.util;

import static java.lang.System.nanoTime;
import static java.text.NumberFormat.getInstance;
import static java.util.Locale.ENGLISH;
import static org.slf4j.LoggerFactory.getLogger;

import java.text.NumberFormat;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import org.slf4j.Logger;

/**
 * Utility class for code instrumentation.
 *
 * @author <a href="mailto:Octavian.NITA@ext.ec.europa.eu">Octavian NITA</a>
 * @version $Id$
 */
public class Perf {

  private Perf() {}

  public static final Logger PERF_LOG = getLogger("app.performance");

  public static final NumberFormat PERF_NUM_FMT = getInstance(ENGLISH);

  public static void ifTraceEnabled(Consumer<Logger> action) {
    if (action != null && PERF_LOG.isTraceEnabled()) {
      action.accept(PERF_LOG);
    }
  }

  public static void trace(String format, Object... arguments) {
    PERF_LOG.trace(format, arguments);
  }

  public static <R> R trace(Callable<R> execution) throws Exception {
    return trace(execution, false, null);
  }

  public static <R> R trace(Callable<R> execution, String fmt, Object... args) throws Exception {
    return trace(execution, false, fmt, args);
  }

  public static <R> R trace(Callable<R> execution, boolean traceResult, String fmt, Object... args) throws Exception {

    if (execution == null) {
      return null;
    }

    final long nanoTimeStart;
    final long nanoTimeEnd;
    final R result;

    try {

      if (fmt != null) {
        PERF_LOG.trace(fmt, args);
      }
      nanoTimeStart = nanoTime();
      result = execution.call();
      nanoTimeEnd = nanoTime();

    } catch (final Exception exception) {

      PERF_LOG.trace("Execution ended in error (check the logs for details): {}", exception.getMessage());
      throw exception;

    }

    if (PERF_LOG.isTraceEnabled()) {
      PERF_LOG.trace(
        "Execution lasted: {} milliseconds",
        PERF_NUM_FMT.format((nanoTimeEnd - nanoTimeStart) / 1_000_000)
      );
      if (traceResult) {
        PERF_LOG.trace("Execution result: {}", result);
      }
    }
    return result;
  }
}
