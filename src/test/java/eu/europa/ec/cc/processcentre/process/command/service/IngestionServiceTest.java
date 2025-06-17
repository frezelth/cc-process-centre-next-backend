package eu.europa.ec.cc.processcentre.process.command.service;

import eu.europa.ec.cc.processcentre.ProcessCentreNextApplicationTests;
import eu.europa.ec.cc.processcentre.config.AccessRight;
import eu.europa.ec.cc.processcentre.model.ProcessStatus;
import eu.europa.ec.cc.processcentre.process.command.repository.ProcessMapper;
import eu.europa.ec.cc.processcentre.process.command.repository.model.FindProcessByIdQueryResponse;
import eu.europa.ec.cc.processcentre.process.command.repository.model.FindProcessConfigByIdQueryResponse;
import eu.europa.ec.cc.processcentre.translation.TranslationAttribute;
import eu.europa.ec.cc.processcentre.util.ProtoUtils;
import eu.europa.ec.cc.provider.proto.ProcessCancelled;
import eu.europa.ec.cc.provider.proto.ProcessCreated;
import eu.europa.ec.cc.provider.proto.ProcessRunningStatusChanged;
import eu.europa.ec.cc.provider.proto.ProcessRunningStatusChanged.Status;
import eu.europa.ec.cc.provider.proto.ProcessVariablesUpdated;
import eu.europa.ec.cc.variables.proto.VariableValue;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

public class IngestionServiceTest extends ProcessCentreNextApplicationTests {

  @Autowired
  private IngestionService ingestionService;

  @Autowired
  private ProcessMapper processMapper;

  @Test
  void testInsertProcessNoConfig(){
    Instant now = Instant.now();
    ProcessCreated processCreated = ProcessCreated.newBuilder()
        .setProcessInstanceId("1")
        .setDomainKey("PM_AGRI")
        .setProviderId("Flex")
        .setProcessTypeKey("processTypeNoConfig")
        .setProcessTypeId("processTypeId")
        .setBusinessDomainId("businessDomainId")
        .setBusinessStatus("businessStatus")
        .setCreatedOn(ProtoUtils.instantToTimestamp(now))
        .setParentProcessInstanceId("parentProcessInstanceId")
        .setUserId("userId")
        .setOnBehalfOfUserId("onBehalfOfUserId")
        .setResponsibleUserId("responsibleUserId")
        .setResponsibleOrganisationId("responsibleOrganisationId")
        .addAssociatedPortfolioItemIds("associatedPortfolioItemIds")
        .putProcessVariables("var1", VariableValue.newBuilder()
            .setStringValue("value1")
            .build())
        .build();
    ingestionService.handle(processCreated);

    Optional<FindProcessByIdQueryResponse> byId = processMapper.findById(
        processCreated.getProcessInstanceId());
    Assertions.assertTrue(byId.isPresent());

    FindProcessByIdQueryResponse process = byId.get();
    Assertions.assertEquals(processCreated.getProcessInstanceId(), process.getProcessInstanceId());
    Assertions.assertEquals(processCreated.getDomainKey(), process.getDomainKey());
    Assertions.assertEquals(processCreated.getProviderId(), process.getProviderId());
    Assertions.assertEquals(processCreated.getProcessTypeKey(), process.getProcessTypeKey());
    Assertions.assertEquals(processCreated.getProcessTypeId(), process.getProcessTypeId());
    Assertions.assertEquals(processCreated.getResponsibleOrganisationId(), process.getProcessResponsibleOrganisation());
    Assertions.assertEquals(processCreated.getResponsibleUserId(), process.getProcessResponsibleUserId());
    Assertions.assertEquals(processCreated.getBusinessStatus(), process.getBusinessStatus());
    Assertions.assertEquals(processCreated.getBusinessDomainId(), process.getBusinessDomainId());

    Assertions.assertEquals(processCreated.getProcessVariablesMap().size(), process.getVariables().size());
    Assertions.assertEquals("var1", process.getVariables().getFirst().getName());
    Assertions.assertEquals("value1", process.getVariables().getFirst().getValueString());
    Assertions.assertEquals(processCreated.getAssociatedPortfolioItemIdsList().size(), process.getPortfolioItemIds().size());
    Assertions.assertEquals(processCreated.getAssociatedPortfolioItemIdsList().getFirst(), process.getPortfolioItemIds().getFirst().getPortfolioItemId());

    Optional<FindProcessConfigByIdQueryResponse> processConfigById = processMapper.findProcessConfigById(
        processCreated.getProcessInstanceId());
    Assertions.assertTrue(processConfigById.isEmpty());
  }

  @Test
  void testInsertProcessConfigPresent(){
    Instant now = Instant.now();
    ProcessCreated processCreated = ProcessCreated.newBuilder()
        .setProcessInstanceId("1")
        .setDomainKey("PM_AGRI")
        .setProviderId("Flex")
        .setProcessTypeKey("processType1")
        .setProcessTypeId("processTypeId")
        .setBusinessDomainId("businessDomainId")
        .setBusinessStatus("businessStatus")
        .setCreatedOn(ProtoUtils.instantToTimestamp(now))
        .setParentProcessInstanceId("parentProcessInstanceId")
        .setUserId("userId")
        .setOnBehalfOfUserId("onBehalfOfUserId")
        .setResponsibleUserId("responsibleUserId")
        .setResponsibleOrganisationId("responsibleOrganisationId")
        .addAssociatedPortfolioItemIds("associatedPortfolioItemIds")
        .putProcessVariables("YEAR", VariableValue.newBuilder()
            .setStringValue("2025")
            .build())
        .putProcessVariables("SCOPE_ID", VariableValue.newBuilder()
            .setStringValue("scopeId")
            .build())
        .build();
    ingestionService.handle(processCreated);

    Optional<FindProcessByIdQueryResponse> byId = processMapper.findById(
        processCreated.getProcessInstanceId());
    Assertions.assertTrue(byId.isPresent());

    FindProcessByIdQueryResponse process = byId.get();
    Assertions.assertEquals(processCreated.getProcessInstanceId(), process.getProcessInstanceId());
    Assertions.assertEquals(processCreated.getDomainKey(), process.getDomainKey());
    Assertions.assertEquals(processCreated.getProviderId(), process.getProviderId());
    Assertions.assertEquals(processCreated.getProcessTypeKey(), process.getProcessTypeKey());
    Assertions.assertEquals(processCreated.getProcessTypeId(), process.getProcessTypeId());
    Assertions.assertEquals(processCreated.getResponsibleOrganisationId(), process.getProcessResponsibleOrganisation());
    Assertions.assertEquals(processCreated.getResponsibleUserId(), process.getProcessResponsibleUserId());
    Assertions.assertEquals(processCreated.getBusinessStatus(), process.getBusinessStatus());
    Assertions.assertEquals(processCreated.getBusinessDomainId(), process.getBusinessDomainId());

    Assertions.assertEquals(processCreated.getProcessVariablesMap().size(), process.getVariables().size());
    Assertions.assertEquals("YEAR", process.getVariables().stream().filter(f -> f.getName().equals("YEAR")).findFirst().get().getName());
    Assertions.assertEquals("2025", process.getVariables().stream().filter(f -> f.getName().equals("YEAR")).findFirst().get().getValueString());
    Assertions.assertEquals(processCreated.getAssociatedPortfolioItemIdsList().size(), process.getPortfolioItemIds().size());
    Assertions.assertEquals(processCreated.getAssociatedPortfolioItemIdsList().getFirst(), process.getPortfolioItemIds().getFirst().getPortfolioItemId());

    Assertions.assertEquals("MARE - Examination of accounts 2021-2027 - 2025", process.getTranslationsAsMap().get(TranslationAttribute.PROCESS_TITLE).getFirst().getText());
    Assertions.assertEquals("MARE - Examination of accounts 2021-2027", process.getTranslationsAsMap().get(TranslationAttribute.PROCESS_TYPE_NAME).getFirst().getText());

    Assertions.assertEquals(AccessRight.PROCESS_CENTRE, process.getSecurityApplicationId());
    Assertions.assertEquals("PC_PM_REGIO_ViewProcess", process.getSecuritySecundaTask());
    Assertions.assertEquals("SCOPE_TYPE", process.getSecurityScopeTypeId());
    Assertions.assertEquals("scopeId", process.getSecurityScopeId());
    Assertions.assertEquals("responsibleOrganisationId", process.getSecurityOrganisationId());
  }

  @Test
  @Sql(statements = "insert into t_process (process_instance_id) values ('1')")
  void testInsertVariable() {
    ProcessVariablesUpdated variables = ProcessVariablesUpdated.newBuilder()
            .setProcessId("1")
            .putProcessVariables("var1", VariableValue.newBuilder()
                    .setStringValue("value1")
                    .build())
            .build();
    ingestionService.handle(variables);

    FindProcessByIdQueryResponse findProcessByIdQueryResponse = processMapper.findById("1")
        .orElseThrow();
    Assertions.assertEquals(1, findProcessByIdQueryResponse.getVariables().size());
    Assertions.assertEquals("var1", findProcessByIdQueryResponse.getVariables().getFirst().getName());
    Assertions.assertEquals("value1", findProcessByIdQueryResponse.getVariables().getFirst().getVariableValue());
  }

  @Test
  @Sql(statements = "insert into t_process (process_instance_id) values ('1')")
  void testInsertVariableAsDate() {
    Instant now = Instant.now();
    ProcessVariablesUpdated variables = ProcessVariablesUpdated.newBuilder()
        .setProcessId("1")
        .putProcessVariables("var1", VariableValue.newBuilder()
            .setTimeValue(ProtoUtils.instantToTimestamp(now))
            .build())
        .build();
    ingestionService.handle(variables);

    FindProcessByIdQueryResponse findProcessByIdQueryResponse = processMapper.findById("1")
        .orElseThrow();
    Assertions.assertEquals(1, findProcessByIdQueryResponse.getVariables().size());
    Assertions.assertEquals("var1", findProcessByIdQueryResponse.getVariables().getFirst().getName());
    Assertions.assertEquals(now.getEpochSecond(), findProcessByIdQueryResponse.getVariables().getFirst().getValueDate().getEpochSecond());
  }

  @Test
  @Sql(statements = "insert into t_process (process_instance_id) values ('1')")
  void testInsertVariableAsLong() {
    ProcessVariablesUpdated variables = ProcessVariablesUpdated.newBuilder()
        .setProcessId("1")
        .putProcessVariables("var1", VariableValue.newBuilder()
            .setLongValue(10L)
            .build())
        .build();
    ingestionService.handle(variables);

    FindProcessByIdQueryResponse findProcessByIdQueryResponse = processMapper.findById("1")
        .orElseThrow();
    Assertions.assertEquals(1, findProcessByIdQueryResponse.getVariables().size());
    Assertions.assertEquals("var1", findProcessByIdQueryResponse.getVariables().getFirst().getName());
    Assertions.assertEquals(10L, findProcessByIdQueryResponse.getVariables().getFirst().getValueLong());
  }

  @Test
  @Sql(statements = "insert into t_process (process_instance_id) values ('1')")
  void testInsertVariableAsDouble() {
    ProcessVariablesUpdated variables = ProcessVariablesUpdated.newBuilder()
        .setProcessId("1")
        .putProcessVariables("var1", VariableValue.newBuilder()
            .setDoubleValue(10.5)
            .build())
        .build();
    ingestionService.handle(variables);

    FindProcessByIdQueryResponse findProcessByIdQueryResponse = processMapper.findById("1")
        .orElseThrow();
    Assertions.assertEquals(1, findProcessByIdQueryResponse.getVariables().size());
    Assertions.assertEquals("var1", findProcessByIdQueryResponse.getVariables().getFirst().getName());
    Assertions.assertEquals(10.5, findProcessByIdQueryResponse.getVariables().getFirst().getValueDouble());
  }

  @Test
  @Sql(statements = "insert into t_process (process_instance_id) values ('1')")
  void testInsertVariableAsBoolean() {
    ProcessVariablesUpdated variables = ProcessVariablesUpdated.newBuilder()
        .setProcessId("1")
        .putProcessVariables("var1", VariableValue.newBuilder()
            .setBooleanValue(true)
            .build())
        .build();
    ingestionService.handle(variables);

    FindProcessByIdQueryResponse findProcessByIdQueryResponse = processMapper.findById("1")
        .orElseThrow();
    Assertions.assertEquals(1, findProcessByIdQueryResponse.getVariables().size());
    Assertions.assertEquals("var1", findProcessByIdQueryResponse.getVariables().getFirst().getName());
    Assertions.assertEquals(true, findProcessByIdQueryResponse.getVariables().getFirst().getValueBoolean());
  }

  @Test
  @Sql(statements = {
      "insert into t_process (process_instance_id) values ('1')"
  })
  void testProcessRunningStatusChanged_completed() {
    Instant now = Instant.now();
    ProcessRunningStatusChanged statusChanged = ProcessRunningStatusChanged.newBuilder()
        .setProcessInstanceId("1")
        .setChangedOn(ProtoUtils.instantToTimestamp(now))
        .setNewStatus(Status.ENDED)
        .setUserId("userId")
        .setOnBehalfOfUserId("onBehalfOfUserId")
        .build();
    ingestionService.handle(statusChanged);

    FindProcessByIdQueryResponse findProcessByIdQueryResponse = processMapper.findById("1")
        .orElseThrow();
    Assertions.assertEquals(ProcessStatus.COMPLETED, findProcessByIdQueryResponse.getStatus());
  }

  @Test
  @Sql(statements = {
      "insert into t_process (process_instance_id) values ('1')"
  })
  void testProcessCancelled() {
    Instant now = Instant.now();
    ProcessCancelled event = ProcessCancelled.newBuilder()
        .setProcessInstanceId("1")
        .setCancelledOn(ProtoUtils.instantToTimestamp(now))
        .setUserId("userId")
        .setOnBehalfOfUserId("onBehalfOfUserId")
        .build();
    ingestionService.handle(event);

    FindProcessByIdQueryResponse findProcessByIdQueryResponse = processMapper.findById("1")
        .orElseThrow();
    Assertions.assertEquals(ProcessStatus.CANCELLED, findProcessByIdQueryResponse.getStatus());
  }

}
