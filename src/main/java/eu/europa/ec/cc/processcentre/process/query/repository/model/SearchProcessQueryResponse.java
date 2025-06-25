package eu.europa.ec.cc.processcentre.process.query.repository.model;

import lombok.Data;

@Data
public class SearchProcessQueryResponse {

    private SearchProcessQueryResponseType type;
    private String payload;

    public enum SearchProcessQueryResponseType {
        agg, data, count
    }

}
