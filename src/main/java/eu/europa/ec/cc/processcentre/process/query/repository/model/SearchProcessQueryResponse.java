package eu.europa.ec.cc.processcentre.process.query.repository.model;

import eu.europa.ec.cc.processcentre.model.ProcessStatus;
import lombok.Data;

@Data
public class SearchProcessQueryResponse{

    private String processInstanceId;
    private String title;
    private String activeTasks;
    private Long totalCount;
    private ProcessStatus status;
    private String cardLayout;
}
