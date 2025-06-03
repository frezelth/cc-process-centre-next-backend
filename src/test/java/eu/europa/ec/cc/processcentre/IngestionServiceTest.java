package eu.europa.ec.cc.processcentre;

import eu.europa.ec.cc.processcentre.proto.UpdateProcessContext;
import eu.europa.ec.cc.processcentre.service.IngestionService;
import eu.europa.ec.cc.provider.proto.ProcessCreated;
import eu.europa.ec.cc.variables.proto.VariableValue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

public class IngestionServiceTest extends ProcessCentreNextApplicationTests {

  @Autowired
  private IngestionService ingestionService;

  @Test
  void testInsertProcess(){
    ProcessCreated processCreated = ProcessCreated.newBuilder()
        .setProcessInstanceId("1")
        .putProcessVariables("var1", VariableValue.newBuilder()
            .setStringValue("value1")
            .build())
        .build();
    ingestionService.handle(processCreated);
  }

}
