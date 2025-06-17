package eu.europa.ec.cc.processcentre.config;

import com.fasterxml.jackson.annotation.JsonAlias;

public record AccessRight(
    String permissionId,
    String applicationId,
    String scopeTypeId,
    @JsonAlias("variableName") String scopeId,
    @JsonAlias("responsibleOrganisationId") String organisationId
) {

  public enum Right {
    VIEW, CREATE, CANCEL, CLOSE, REOPEN, DELETE, EDIT,
    SKIP_SERVICE_TASK, RETRY_SERVICE_TASK,
    CREATE_SUB_PROCESS
  }

  public static final String PROCESS_CENTRE = "PROCESS_CENTRE";

}
