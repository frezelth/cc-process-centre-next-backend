package eu.europa.ec.cc.processcentre.taxonomy.service;

import eu.europa.ec.cc.processcentre.ProcessCentreNextApplicationTests;
import eu.europa.ec.cc.processcentre.taxonomy.repository.TaxonomyMapper;
import eu.europa.ec.cc.processcentre.taxonomy.repository.model.SaveTaxonomyProcessTypeQueryParam;
import eu.europa.ec.cc.taxonomy.event.proto.BranchUpdated;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public class TaxonomyIngestionServiceTest extends ProcessCentreNextApplicationTests {

  @Autowired
  private TaxonomyIngestionService service;

//  @MockitoBean
//  private TaxonomyMapper taxonomyMapper;

  @Test
  void testInsertTaxonomyPath(){
    BranchUpdated event = BranchUpdated.newBuilder()
        .setFullPath("/ProcessCentre/ProgrammeManagement/Flex")
        .addRefUris("http://compass-corporate.cc.eu/process-type/providerId:domainKey:processTypeKey")
        .build();
    service.handle(event);

//    Mockito.verify(taxonomyMapper).deleteTaxonomyProcessTypes("ProgrammeManagement/Flex");
//    Mockito.verify(taxonomyMapper).saveTaxonomyProcessTypes(
//        new SaveTaxonomyProcessTypeQueryParam(
//            "ProgrammeManagement/Flex",
//            Collections.singletonList("providerId:domainKey:processTypeKey")
//        )
//    );
  }

}
