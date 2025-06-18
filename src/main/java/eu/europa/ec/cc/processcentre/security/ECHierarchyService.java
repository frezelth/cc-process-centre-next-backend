package eu.europa.ec.cc.processcentre.security;

import static eu.europa.ec.cc.processcentre.ccm.external.service.CcmRestService.CCM_CODE_TYPE_EC_HIER;
import static eu.europa.ec.cc.processcentre.ccm.external.service.CcmRestService.CCM_CONTEXT_ECFIN;
import static eu.europa.ec.cc.processcentre.ccm.external.service.CcmRestService.CCM_CONTEXT_GEN;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.sort;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.concurrent.CompletableFuture.runAsync;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import eu.europa.ec.cc.processcentre.ccm.external.service.CcmRestService;
import eu.europa.ec.cc.processcentre.ccm.external.service.model.CcmCode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:Octavian.NITA@ext.ec.europa.eu">Octavian NITA</a>
 * @version $Id$
 */
@Slf4j
@Service
public class ECHierarchyService {

  private final CcmRestService ccmRestService;

  private final AtomicBoolean cachePopulationOngoing = new AtomicBoolean(false);

  private final Map<String, String> ecHierarchyCache = new HashMap<>(CACHE_INITIAL_CAPACITY);

  // Pre-sizing slightly improves cache population speed.
  // Incidentally, the EH Hierarchy size is currently close to 4,000 entries (as of 2025).
  private static final int CACHE_INITIAL_CAPACITY = 4096;

  public ECHierarchyService(CcmRestService ccmRestService) {
    this.ccmRestService = ccmRestService;
  }

  /**
   * @see <a href="https://webgate.ec.testa.eu/ccm/#/home">Call & Codes Management (CCM2)</a>
   */
  @NonNull
  public Set<String> findTopLevelOrganisationCodes(Collection<String> orgIds) {
    if (orgIds == null || orgIds.isEmpty()) {
      return emptySet();
    }

    // Organisation codes, sorted => top-level codes appear first!
    final var orgCodes = new ArrayList<String>();
    for (final var orgId : orgIds) {
      final var orgCode = orgId == null ? null : ecHierarchyCache.get(orgId);
      if (isNotBlank(orgCode)) {
        orgCodes.add(orgCode);
      } else {
        LOG.debug("No organisation code found for (CCM2) ID {}; skipping...", orgId);
      }
    }
    if (orgCodes.isEmpty()) {
      return emptySet();
    }
    sort(orgCodes);

    // Filter the top-level codes
    final var topLevelOrgCodes = new HashSet<String>();
    String currOrgCodePrefix = null;
    for (final var orgCode : orgCodes) {
      if (currOrgCodePrefix == null || !orgCode.startsWith(currOrgCodePrefix)) {
        topLevelOrgCodes.add(orgCode);
        currOrgCodePrefix = orgCode;
      }
    }
    return unmodifiableSet(topLevelOrgCodes);
  }

  @EventListener
  public void onContextRefreshed(ContextRefreshedEvent event) {
    populateECHierarchyCache();
  }

  /**
   * @return a new {@link CompletableFuture} for the asynchronous EC-Hierarchy cache population or
   *   {@code null} if a population process is already in progress (mostly intended for testing purposes)
   */
  @NonNull
  @Scheduled(cron = "0 0 7 2,17 * *", zone = "Europe/Brussels")
  public Optional<CompletableFuture<Void>> populateECHierarchyCache() {
    if (!cachePopulationOngoing.compareAndSet(false, true)) {
      return empty();
    }

    return of(runAsync(() -> {
      try {
        LOG.debug("Populating the EC-Hierarchy cache...");

        /* First, fetch the EC-Hierarchy; should this fail, we maintain the old cache in place */
        final var ecHierarchy = fetchECHierarchy();

        if (!ecHierarchy.isEmpty()) {
          ecHierarchyCache.clear();
          ecHierarchyCache.putAll(ecHierarchy);
        }

      } catch (Exception ex) {
        LOG.error("An error has occurred while populating the EC-Hierarchy cache (the old cache is maintained)", ex);
      } finally {
        LOG.debug("Done populating the EC-Hierarchy cache");
        cachePopulationOngoing.set(false);
      }
    }));
  }

  @NonNull
  protected Map<String, String> fetchECHierarchy() {
    final var ecHierarchy = new HashMap<String, String>(CACHE_INITIAL_CAPACITY);

    final Consumer<Collection<CcmCode>> addToECHierarchy = ccmCodes -> {
      if (ccmCodes != null && !ccmCodes.isEmpty()) {
        for (final var code : ccmCodes) {
          if (code.isValid()) {
            ecHierarchy.put(code.id(), code.abbreviation());
          }
        }
      }
    };

    addToECHierarchy.accept(ccmRestService.findCodes(CCM_CONTEXT_GEN, CCM_CODE_TYPE_EC_HIER));
    addToECHierarchy.accept(ccmRestService.findCodes(CCM_CONTEXT_ECFIN, CCM_CODE_TYPE_EC_HIER));

    return ecHierarchy.isEmpty() ? emptyMap() : unmodifiableMap(ecHierarchy);
  }
}
