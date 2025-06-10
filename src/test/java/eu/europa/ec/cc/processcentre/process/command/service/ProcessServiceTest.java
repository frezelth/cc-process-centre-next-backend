package eu.europa.ec.cc.processcentre.process.command.service;

import eu.europa.ec.cc.processcentre.ProcessCentreNextApplicationTests;
import eu.europa.ec.cc.processcentre.proto.UpdateProcessContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

public class ProcessServiceTest extends ProcessCentreNextApplicationTests {

  @Autowired
  private ProcessService processService;

  @Test
  @Sql(statements = {"insert into t_process (PROCESS_INSTANCE_ID) VALUES ('1')",
      "insert into t_process_variable (PROCESS_INSTANCE_ID, NAME, VALUE_STRING) VALUES ('1', 'YEAR', '2025')"})
  void testCreateConfig(){
    processService.updateProcessConfiguration(
        UpdateProcessContext.newBuilder()
            .setProcessInstanceId("1")
            .setProviderId("providerId")
            .setDomainKey("domainKey")
            .setProcessTypeKey("processType1")
            .build()
    );

  }
}
