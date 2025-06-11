package eu.europa.ec.cc.processcentre.process.command.service;

import eu.europa.ec.cc.babel.proto.BabelText;
import eu.europa.ec.cc.babel.proto.BabelText.Literal;
import eu.europa.ec.cc.babel.proto.BabelText.Literal.LiteralValue;
import eu.europa.ec.cc.babel.proto.ISO6391LanguageCode;
import eu.europa.ec.cc.processcentre.ProcessCentreNextApplicationTests;
import eu.europa.ec.cc.processcentre.process.command.repository.ProcessMapper;
import eu.europa.ec.cc.processcentre.process.command.repository.model.FindProcessByIdQueryResponse;
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
  private ProcessMapper processMapper;

  @Test
  void testInsertProcess(){
    ProcessCreated processCreated = ProcessCreated.newBuilder()
        .setProcessInstanceId("1")
        .setDomainKey("PM_AGRI")
        .setProviderId("Flex")
        .setProcessTypeKey("processType1")
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

    Optional<FindProcessByIdQueryResponse> byId = processMapper.findById("1");
    Assertions.assertTrue(byId.isPresent());
  }
}
