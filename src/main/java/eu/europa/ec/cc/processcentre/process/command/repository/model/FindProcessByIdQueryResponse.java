package eu.europa.ec.cc.processcentre.process.command.repository.model;

import eu.europa.ec.cc.processcentre.model.ProcessRunningStatus;
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

    private String businessStatus;

    private List<FindProcessByIdQueryResponseTranslation> translations;
    private List<FindProcessVariableQueryResponse> variables;

    // ordered by default DESC, latest logs first
    private List<FindProcessByIdQueryRunningStatusLog> runningStatusLogs;

    private List<String> portfolioItemIds;

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

    public ProcessRunningStatus getStatus() {
        return this.runningStatusLogs.stream().findFirst()
            .map(FindProcessByIdQueryRunningStatusLog::getStatus)
            .orElse(null);
    }

    public Instant getStartedOn(){
        // started on should be the first entry in the logs
        return this.runningStatusLogs.reversed().stream().filter(
            f -> f.getStatus() == ProcessRunningStatus.ONGOING
        ).findFirst().map(FindProcessByIdQueryRunningStatusLog::getTimestamp).orElse(null);
    }

    public Instant getCompletedOn(){
        return this.runningStatusLogs.stream().filter(
            f -> f.getStatus() == ProcessRunningStatus.COMPLETED
        ).findFirst().map(FindProcessByIdQueryRunningStatusLog::getTimestamp).orElse(null);
    }

    public Instant getCancelledOn(){
        return this.runningStatusLogs.stream().filter(
            f -> f.getStatus() == ProcessRunningStatus.CANCELLED
        ).findFirst().map(FindProcessByIdQueryRunningStatusLog::getTimestamp).orElse(null);
    }

    public Instant getPausedOn(){
        return this.runningStatusLogs.stream().filter(
            f -> f.getStatus() == ProcessRunningStatus.PAUSED
        ).findFirst().map(FindProcessByIdQueryRunningStatusLog::getTimestamp).orElse(null);
    }
}
