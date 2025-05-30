package eu.europa.ec.cc.processcentre;

import eu.europa.ec.cc.processcentre.proto.UpdateProcess;
import eu.europa.ec.cc.processcentre.service.ProcessService;
import eu.europa.ec.cc.variables.proto.VariableValue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

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

}
