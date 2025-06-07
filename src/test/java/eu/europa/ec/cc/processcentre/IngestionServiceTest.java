package eu.europa.ec.cc.processcentre;

import eu.europa.ec.cc.babel.proto.BabelText;
import eu.europa.ec.cc.babel.proto.BabelText.Literal;
import eu.europa.ec.cc.babel.proto.BabelText.Literal.LiteralValue;
import eu.europa.ec.cc.babel.proto.ISO6391LanguageCode;
import eu.europa.ec.cc.processcentre.query.ProcessQueries;
import eu.europa.ec.cc.processcentre.repository.model.FindProcessByIdQueryResponse;
import eu.europa.ec.cc.processcentre.service.IngestionService;
import eu.europa.ec.cc.provider.proto.ProcessCreated;
import eu.europa.ec.cc.provider.proto.ProcessVariablesUpdated;
import eu.europa.ec.cc.provider.task.event.proto.TaskCreated;
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
  void testInsertTask(){
    TaskCreated taskCreated = TaskCreated.newBuilder()
        .setProcessInstanceId("1")
        .setTaskInstanceId("1")
        .setTaskTypeKey("taskTypeKey")
        .setTitle(BabelText.newBuilder()
            .setLiteral(Literal.newBuilder()
                .addLiteralValues(LiteralValue.newBuilder()
                    .setDefault(true)
                    .setText("Task 1")
                    .setLanguageCode(ISO6391LanguageCode.en)
                    .build())
                .build())
            .build())
        .build();
    ingestionService.handle(taskCreated);
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
