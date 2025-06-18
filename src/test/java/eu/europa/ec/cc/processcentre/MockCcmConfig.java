package eu.europa.ec.cc.processcentre;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.cc.processcentre.ccm.external.service.CcmRestService;
import eu.europa.ec.cc.processcentre.ccm.external.service.model.CcmCode;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

@Configuration
public class MockCcmConfig {

  @Bean
  public CcmRestService ccmRestService(ObjectMapper objectMapper) {
    return new Ccm2Mock(objectMapper);
  }

  public static class Ccm2Mock implements CcmRestService {

    private final Set<CcmCode> ccmCodes;

    @SneakyThrows
    public Ccm2Mock(ObjectMapper objectMapper) {
      Resource ccm2Data = new ClassPathResource("/ccm2/ccm2-echierarchy.json");
      String contentAsString = ccm2Data.getContentAsString(StandardCharsets.UTF_8);
      this.ccmCodes = objectMapper.readValue(contentAsString, new TypeReference<Set<CcmCode>>() {});
    }

    @Override
    public Set<CcmCode> findCodes(String context, String codeType) {
      return ccmCodes;
    }
  }

}
