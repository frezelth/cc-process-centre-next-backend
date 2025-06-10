package eu.europa.ec.cc.processcentre.process.query.repository.model;

import lombok.Data;

@Data
public class SearchProcessQueryResponse{

    private String processInstanceId;
    private String title;
    private String tasks;
    private Long totalCount;

}
