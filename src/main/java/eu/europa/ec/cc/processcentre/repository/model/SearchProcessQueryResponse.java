package eu.europa.ec.cc.processcentre.repository.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Data
public class SearchProcessQueryResponse{

    private String processInstanceId;
    private String title;
    private String tasks;
    private Long totalCount;

}
