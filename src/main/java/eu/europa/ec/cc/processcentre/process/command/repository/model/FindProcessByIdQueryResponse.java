package eu.europa.ec.cc.processcentre.process.command.repository.model;

import eu.europa.ec.cc.processcentre.model.ProcessAction;
import eu.europa.ec.cc.processcentre.model.ProcessStatus;
import eu.europa.ec.cc.processcentre.translation.TranslationAttribute;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Data;

@Data
public class FindProcessByIdQueryResponse {
    private String processInstanceId;
    private String providerId;
    private String domainKey;
    private String processTypeKey;
    private String processTypeId;
    private String processResponsibleOrganisation;
    private String processResponsibleOrganisationCode;

    private String processResponsibleUserId;

    private String businessDomainId;

    private ProcessStatus status;

    private String businessStatus;

    private String resultCard;

    private List<FindProcessByIdQueryResponseTranslation> translations;
    private List<FindProcessVariableQueryResponse> variables;

    // ordered by default DESC, latest logs first
    private List<FindProcessByIdQueryActionLog> runningStatusLogs;

    private List<FindProcessByIdQueryResponsePortfolioItem> portfolioItemIds;

    private String securityApplicationId;
    private String securitySecundaTask;
    private String securityScopeTypeId;
    private String securityScopeId;
    private String securityOrganisationId;

    public Map<TranslationAttribute, List<FindProcessByIdQueryResponseTranslation>> getTranslationsAsMap() {
        if (translations == null) {
            return Collections.emptyMap();
        }
        return
            getTranslations().stream()
                .collect(Collectors.groupingBy(
                    FindProcessByIdQueryResponseTranslation::getAttribute
                ));
    }

    public Instant getStartedOn(){
        // started on should be the first START action in the logs
        return this.runningStatusLogs.reversed().stream().filter(
            f -> f.getAction() == ProcessAction.START
        ).findFirst().map(FindProcessByIdQueryActionLog::getTimestamp).orElse(null);
    }

    public Instant getCompletedOn(){
        return this.runningStatusLogs.stream().filter(
            f -> f.getAction() == ProcessAction.COMPLETE
        ).findFirst().map(FindProcessByIdQueryActionLog::getTimestamp).orElse(null);
    }

    public Instant getCancelledOn(){
        return this.runningStatusLogs.stream().filter(
            f -> f.getAction() == ProcessAction.CANCEL
        ).findFirst().map(FindProcessByIdQueryActionLog::getTimestamp).orElse(null);
    }

    public Instant getPausedOn(){
        return this.runningStatusLogs.stream().filter(
            f -> f.getAction() == ProcessAction.PAUSE
        ).findFirst().map(FindProcessByIdQueryActionLog::getTimestamp).orElse(null);
    }
}
