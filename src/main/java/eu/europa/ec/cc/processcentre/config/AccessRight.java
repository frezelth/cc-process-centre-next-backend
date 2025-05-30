package eu.europa.ec.cc.processcentre.config;

public record AccessRight(
    String permissionId,
    String applicationId,
    String scopeTypeId,
    String scopeId,
    String organisationId
) {

  public enum Right {
    VIEW, CREATE, CANCEL, CLOSE, REOPEN, DELETE, EDIT,
    SKIP_SERVICE_TASK, RETRY_SERVICE_TASK,
    CREATE_SUB_PROCESS
  }
  
  public static final String PROCESS_CENTRE = "PROCESS_CENTRE";

}
