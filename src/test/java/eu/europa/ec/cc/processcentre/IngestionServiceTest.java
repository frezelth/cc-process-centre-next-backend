package eu.europa.ec.cc.processcentre;

import eu.europa.ec.cc.processcentre.query.ProcessQueries;
import eu.europa.ec.cc.processcentre.repository.model.FindProcessByIdQueryResponse;
import eu.europa.ec.cc.processcentre.service.IngestionService;
import eu.europa.ec.cc.provider.proto.ProcessCreated;
import eu.europa.ec.cc.provider.proto.ProcessVariablesUpdated;
import eu.europa.ec.cc.variables.proto.VariableValue;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

public class IngestionServiceTest extends ProcessCentreNextApplicationTests {

  @Autowired
  private IngestionService ingestionService;

  @Autowired
  private ProcessQueries processQueries;

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

    Optional<FindProcessByIdQueryResponse> byId = processQueries.findById("1");
    Assertions.assertTrue(byId.isPresent());
  }
}
