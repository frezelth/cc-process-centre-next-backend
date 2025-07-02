package eu.europa.ec.cc.processcentre.util;

import static eu.europa.ec.cc.processcentre.util.AuthDetails.fromAccessToken;
import static java.lang.String.join;
import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import eu.europa.ec.cc.processcentre.exception.ForbiddenException;
import eu.europa.ec.cc.processcentre.security.SecurityRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * An authorization {@link HandlerInterceptor handler interceptor} to control access to more sensitive endpoints, like
 * the support ones.
 *
 * @author <a href="mailto:Octavian.NITA@ext.ec.europa.eu">Octavian NITA</a>
 * @version $Id$
 */
@Slf4j
public class AuthInterceptor implements HandlerInterceptor {

  /**
   * If {@code null}, we intercept all incoming requests.
   */
  private final Predicate<String> interceptsUris;

  /**
   * If {@code null}, we fetch the {@link AuthDetails#fromAccessToken() auth details from the current access token}.
   */
  private final Supplier<AuthDetails> authDetails;

  @NotNull
  private final SecurityRepository secundaRepository;

  @NotEmpty
  private final Set<String> requiredPermissions;

  public AuthInterceptor(@NotNull SecurityRepository secundaRepository, @NotEmpty Set<String> requiredPermissions) {
    this(null, null, secundaRepository, requiredPermissions);
  }

  public AuthInterceptor(Predicate<String> interceptsUris, @NotNull SecurityRepository secundaRepository,
                         @NotEmpty Set<String> requiredPermissions) {
    this(interceptsUris, null, secundaRepository, requiredPermissions);
  }

  public AuthInterceptor(Supplier<AuthDetails> authDetails, @NotNull SecurityRepository secundaRepository,
                         @NotEmpty Set<String> requiredPermissions) {
    this(null, authDetails, secundaRepository, requiredPermissions);
  }

  public AuthInterceptor(Predicate<String> interceptsUris, Supplier<AuthDetails> authDetails,
                         @NotNull SecurityRepository secundaRepository, @NotEmpty Set<String> requiredPermissions) {

    requireNonNull(secundaRepository, "An authorization interceptor requires Secunda+ configuration");
    final Set<String> reqPerms = requiredPermissions == null
      ? emptySet()
      : requiredPermissions
        .stream()
        .map(StringUtils::trimToNull)
        .filter(StringUtils::isNotEmpty)
        .collect(toUnmodifiableSet());
    if (reqPerms.isEmpty()) {
      throw new IllegalArgumentException("An authorization interceptor requires Secunda+ permissions to check");
    }

    this.interceptsUris = interceptsUris;
    this.authDetails = authDetails;
    this.secundaRepository = secundaRepository;
    this.requiredPermissions = reqPerms;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

    final var requestUri = request.getRequestURI();
    if (interceptsUris == null || interceptsUris.test(requestUri)) {
      LOG.trace("Request {} intercepted for authorization", requestUri);
      doIntercept(requestUri);
    }

    return true;
  }

  private void doIntercept(String requestUri) {

    // Injecting and using an AuthDetails Supplier significantly eases testing:
    final var authDetails = this.authDetails == null ? fromAccessToken() : this.authDetails.get();

    if (isWhitelistedUser(authDetails.loggedUser())) {
      LOG.trace("Whitelisted logged-in user '{}' authorized to {}", authDetails.loggedUser(), requestUri);
      return;
    }

    if (isBlank(authDetails.actingUser())) {
      throw new ForbiddenException(
        "User '" + authDetails.actingUser() + "' is not authorized: cannot determine an acting user for " + requestUri);
    }

    if (!CollectionUtils.containsAny(secundaRepository.findTasks(authDetails.actingUser()), requiredPermissions)) {
      throw new ForbiddenException(
        "User '" + authDetails.actingUser() + "' is not authorized: none of any required permissions for " +
        requestUri + ", [" + join(", ", requiredPermissions) + "], granted");
    }

    LOG.trace("User '{}' authorized to {}", authDetails.actingUser(), requestUri);
  }

  private static boolean isWhitelistedUser(String userId) {
    userId = trimToNull(userId);
    return isNotEmpty(userId) && CC_AGRI_ADMINS.contains(userId);
  }

  private static final Set<String> CC_AGRI_ADMINS = Set.of( //@fmt:off
    "bucurmi",
    "cartobe",
    "catareu",
    "clapoli",
    "devrigu",
    "frezeth",
    "hriskal",
    "lempaja",
    "mariliv",
    "moralce",
    "nitanoc",
    "oleksra",
    "oliveth",
    "thomadi",
    "voinecl",
    "wisland"
  ); //@fmt:on
}
