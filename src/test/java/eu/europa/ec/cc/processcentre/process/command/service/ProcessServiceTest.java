package eu.europa.ec.cc.processcentre.process.command.service;

import eu.europa.ec.cc.processcentre.ProcessCentreNextApplicationTests;
import eu.europa.ec.cc.processcentre.process.command.repository.ProcessMapper;
import eu.europa.ec.cc.processcentre.process.command.repository.model.FindProcessByIdQueryResponse;
import eu.europa.ec.cc.processcentre.process.command.repository.model.FindProcessByIdQueryResponseTranslation;
import eu.europa.ec.cc.processcentre.proto.UpdateProcessContext;
import eu.europa.ec.cc.processcentre.translation.TranslationAttribute;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProcessServiceTest extends ProcessCentreNextApplicationTests {

  @Autowired
  private ProcessService processService;

  @Autowired
  private ProcessMapper processMapper;

  @Test
  @Sql(statements = {
          "insert into t_process (PROCESS_INSTANCE_ID, PROVIDER_ID, DOMAIN_KEY, PROCESS_TYPE_KEY) VALUES ('1', 'Flex', 'domainKey', 'processType1')",
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

    FindProcessByIdQueryResponse byId = processMapper.findById("1").orElseThrow();
    Assertions.assertNotNull(byId.getProcessInstanceId());
    Assertions.assertNotNull(byId.getProviderId());
    Assertions.assertNotNull(byId.getDomainKey());
    Assertions.assertNotNull(byId.getProcessTypeKey());
    List<FindProcessByIdQueryResponseTranslation> titles = byId.getTranslations()
            .stream()
            .collect(Collectors.groupingBy(t -> t.getAttribute()))
            .get(TranslationAttribute.PROCESS_TITLE);
    Assertions.assertNotNull(titles);
    Assertions.assertEquals(1, titles.size());
    Assertions.assertEquals("MARE - Examination of accounts 2021-2027 - 2025", titles.getFirst().getText());
    Assertions.assertEquals("en", titles.getFirst().getLanguageCode());

  }
}
