package eu.europa.ec.cc.processcentre;

import eu.europa.ec.cc.message.proto.CCMessageFactory;
import eu.europa.ec.cc.message.proto.ULID;
import eu.europa.ec.cc.processcentre.messaging.KafkaSender;
import eu.europa.ec.cc.processcentre.util.ProtoUtils;
import eu.europa.ec.cc.provider.proto.ProcessCreated;
import eu.europa.ec.cc.variables.proto.VariableValue;
import java.time.Instant;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Initializes the local DB with some processes
 */
@Profile("cc-local")
@Component
@Slf4j
public class Localdev implements InitializingBean {

  private final KafkaSender kafkaSender;

  public Localdev(KafkaSender kafkaSender) {
    this.kafkaSender = kafkaSender;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    LOG.debug("Creating processes");
    ProcessCreated processCreated = ProcessCreated.newBuilder()
        .setProcessInstanceId(UUID.randomUUID().toString())
        .setDomainKey("PM_REGIO")
        .setProviderId("camundapm")
        .setProcessTypeKey("ACOA2021_MARE")
        .setProcessTypeId("camundapm:PM_REGIO:ACOA2021_MARE")
        .setCreatedOn(ProtoUtils.instantToTimestamp(Instant.now()))
        .putProcessVariables("CCI", VariableValue.newBuilder()
            .setStringValue("TheCCI")
            .build())
        .putProcessVariables("accountingYear", VariableValue.newBuilder()
            .setStringValue("2025")
            .build())
        .build();
    kafkaSender.sendToEventTopic("any", CCMessageFactory.newMessage(processCreated));
  }
}
