package eu.europa.ec.cc.processcentre;

import eu.europa.ec.cc.processcentre.proto.UpdateProcess;
import eu.europa.ec.cc.processcentre.proto.UpdateProcessContext;
import eu.europa.ec.cc.processcentre.service.ProcessService;
import eu.europa.ec.cc.variables.proto.VariableValue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

public class ProcessServiceTest extends ProcessCentreNextApplicationTests {

  @Autowired
  private ProcessService processService;

  @Test
  void testInsertProcess(){
    UpdateProcess updateProcess = UpdateProcess.newBuilder()
        .setProcessInstanceId("1")
        .putProcessVariables("var1", VariableValue.newBuilder()
            .setStringValue("value1")
            .build())
        .build();
    processService.createProcess(updateProcess);
  }

  @Test
  @Sql(statements = {"insert into t_process (PROCESS_INSTANCE_ID) VALUES ('1')",
  "insert into t_process_variable (PROCESS_INSTANCE_ID, NAME, VALUE_STRING) VALUES ('1', 'var1', 'value1')"})
  void testCreateConfig(){
    processService.updateProcessConfiguration(
        UpdateProcessContext.newBuilder()
            .setProviderId("providerId")
            .setDomainKey("domainKey")
            .setProcessTypeKey("processType1")
            .build()
    );

  }
}
