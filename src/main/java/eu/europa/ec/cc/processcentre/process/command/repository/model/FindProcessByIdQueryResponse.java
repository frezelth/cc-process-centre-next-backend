package eu.europa.ec.cc.processcentre.process.command.repository.model;

import java.util.List;
import lombok.Data;

@Data
public class FindProcessByIdQueryResponse {
    private String processInstanceId;
    private String providerId;
    private String domainKey;
    private String processTypeKey;
    private String status;

    private List<FindProcessByIdQueryResponseTranslation> translations;
    private List<FindProcessVariableQueryResponse> variables;

}
