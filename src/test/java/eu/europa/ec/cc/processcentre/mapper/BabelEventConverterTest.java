package eu.europa.ec.cc.processcentre.mapper;

import com.google.protobuf.Timestamp;
import eu.europa.ec.cc.processcentre.process.command.converter.EventConverter;
import eu.europa.ec.cc.processcentre.process.command.repository.model.InsertProcessQueryParam;
import eu.europa.ec.cc.processcentre.util.ProtoUtils;
import eu.europa.ec.cc.provider.proto.ProcessCreated;
import java.time.Instant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

public class BabelEventConverterTest {

  private final EventConverter converter = Mappers.getMapper(EventConverter.class);

  @Test
  void testConvertUpdateCommand(){
    Instant now = Instant.now();

    ProcessCreated command = ProcessCreated.newBuilder()
        .setProcessInstanceId("1")
        .setProviderId("providerId")
        .setDomainKey("domainKey")
        .setProcessTypeKey("processTypeKey")
        .setCreatedOn(Timestamp.newBuilder().setSeconds(now.getEpochSecond()).build())
        .setUserId("frezeth")
            .setCreatedOn(ProtoUtils.instantToTimestamp(now))
        .build();

    InsertProcessQueryParam queryParam = converter.toInsertProcessQueryParam(command);

    Assertions.assertEquals(command.getProcessInstanceId(), queryParam.processInstanceId());
    Assertions.assertEquals(command.getDomainKey(), queryParam.domainKey());
    Assertions.assertEquals(command.getProcessTypeKey(), queryParam.processTypeKey());
    Assertions.assertEquals(command.getProviderId(), queryParam.providerId());
    Assertions.assertEquals(command.getProcessTypeId(), queryParam.processTypeId());
  }
}
