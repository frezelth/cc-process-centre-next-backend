package eu.europa.ec.cc.processcentre.messaging;

import com.google.protobuf.InvalidProtocolBufferException;
import eu.europa.ec.cc.message.proto.CCMessage;
import eu.europa.ec.cc.processcentre.process.command.service.ProcessConfigService;
import eu.europa.ec.cc.processcentre.proto.command.RefreshProcessConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@ConditionalOnProperty("spring.kafka.bootstrap-servers")
public class CommandListener {

  public final ProcessConfigService processConfigService;

  public CommandListener(ProcessConfigService processConfigService) {
    this.processConfigService = processConfigService;
  }

  @KafkaListener(
      topics = "${kafka.command-topic.name}",
      id = "processcentre.command.listener",
      idIsGroup = false
  )
  public void on(CCMessage record) throws InvalidProtocolBufferException {
    if (record.getPayload().is(RefreshProcessConfig.class)){
      RefreshProcessConfig refreshProcessConfig = record.getPayload().unpack(RefreshProcessConfig.class);
      processConfigService.handle(refreshProcessConfig);
    }
  }

}