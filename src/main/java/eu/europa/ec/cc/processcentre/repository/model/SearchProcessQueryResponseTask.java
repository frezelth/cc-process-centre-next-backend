package eu.europa.ec.cc.processcentre.repository.model;

import lombok.Data;

import java.util.Map;

@Data
public class SearchProcessQueryResponseTask {

    private String processInstanceId;
    private String taskInstanceId;
    private String title;

}
