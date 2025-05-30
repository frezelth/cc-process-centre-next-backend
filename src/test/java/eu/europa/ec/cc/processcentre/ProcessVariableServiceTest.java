package eu.europa.ec.cc.processcentre;

import eu.europa.ec.cc.processcentre.repository.model.FindProcessByIdQueryResponse;
import eu.europa.ec.cc.processcentre.proto.UpdateProcessVariables;
import eu.europa.ec.cc.processcentre.query.ProcessQueries;
import eu.europa.ec.cc.processcentre.service.ProcessVariableService;
import eu.europa.ec.cc.variables.proto.VariableValue;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ProcessVariableServiceTest extends ProcessCentreNextApplicationTests {

  @Autowired
  private ProcessVariableService processVariableService;

  @Autowired
  private ProcessQueries processQueries;

  @Test
  void testInsertVariable() {
    UpdateProcessVariables variables = UpdateProcessVariables.newBuilder()
        .setProcessInstanceId("1")
        .putProcessVariables("var1", VariableValue.newBuilder()
            .setStringValue("value1")
            .build())
        .build();
    processVariableService.updateVariables(variables);

    Optional<FindProcessByIdQueryResponse> byId = processQueries.findById("1");
    Assertions.assertTrue(byId.isPresent());
  }

}
