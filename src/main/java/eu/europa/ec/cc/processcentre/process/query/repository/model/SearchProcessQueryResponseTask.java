package eu.europa.ec.cc.processcentre.process.query.repository.model;

import lombok.Data;

@Data
public class SearchProcessQueryResponseTask {

    private String processInstanceId;
    private String taskInstanceId;
    private String title;

}
