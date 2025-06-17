package eu.europa.ec.cc.processcentre.messaging;

import eu.europa.ec.cc.configadmin.event.proto.RefreshConfiguration;
import eu.europa.ec.cc.message.proto.CCMessage;
import eu.europa.ec.cc.processcentre.process.command.service.ProcessConfigService;
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
      topics = "${kafka.event-topic.name}",
      id = "processcentre.ccevent.listener",
      idIsGroup = false
  )
  public void on(CCMessage record){
    if (record.getPayload().is(RefreshConfiguration.class)){

    }
  }

}