package eu.europa.ec.cc.processcentre.mapper;

import com.google.protobuf.Timestamp;
import eu.europa.ec.cc.processcentre.proto.UpdateProcess;
import eu.europa.ec.cc.processcentre.repository.model.CreateProcessQueryParam;
import java.time.Instant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

public class BabelCommandConverterTest {

  private final CommandConverter converter = Mappers.getMapper(CommandConverter.class);

  @Test
  void testConvertUpdateCommand(){
    Instant now = Instant.now();

    UpdateProcess command = UpdateProcess.newBuilder()
        .setProcessInstanceId("1")
        .setProviderId("providerId")
        .setDomainKey("domainKey")
        .setProcessTypeKey("processTypeKey")
        .setCreatedOn(Timestamp.newBuilder().setSeconds(now.getEpochSecond()).build())
        .setUserId("frezeth")
        .build();

    CreateProcessQueryParam queryParam = converter.toQueryParam(command, now);

    Assertions.assertEquals(command.getProcessInstanceId(), queryParam.processInstanceId());
    Assertions.assertEquals(command.getDomainKey(), queryParam.domainKey());
    Assertions.assertEquals(command.getProcessTypeKey(), queryParam.processTypeKey());
    Assertions.assertEquals(command.getProviderId(), queryParam.providerId());
    Assertions.assertEquals(now.getEpochSecond(), queryParam.startedOn().getEpochSecond());
    Assertions.assertEquals(command.getUserId(), queryParam.startedBy());
  }
}
