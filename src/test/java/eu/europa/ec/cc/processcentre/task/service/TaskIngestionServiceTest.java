package eu.europa.ec.cc.processcentre.task.service;

import eu.europa.ec.cc.babel.proto.BabelText;
import eu.europa.ec.cc.babel.proto.BabelText.Literal;
import eu.europa.ec.cc.babel.proto.BabelText.Literal.LiteralValue;
import eu.europa.ec.cc.babel.proto.ISO6391LanguageCode;
import eu.europa.ec.cc.processcentre.ProcessCentreNextApplicationTests;
import eu.europa.ec.cc.provider.task.event.proto.TaskCreated;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class TaskIngestionServiceTest extends ProcessCentreNextApplicationTests {

  @Autowired
  private IngestionTaskService ingestionService;

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

}
