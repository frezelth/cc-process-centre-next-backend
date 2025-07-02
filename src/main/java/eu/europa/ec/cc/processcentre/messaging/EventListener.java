package eu.europa.ec.cc.processcentre.messaging;

import eu.europa.ec.cc.message.proto.CCMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@ConditionalOnProperty("spring.kafka.bootstrap-servers")
public class EventListener {

  public final ApplicationEventPublisher eventPublisher;

  public EventListener(ApplicationEventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;
  }

  @KafkaListener(
      topics = "${kafka.event-topic.name}",
      id = "processcentre.ccevent.listener",
      idIsGroup = false
  )
  public void on(CCMessage record){
    if (ProcessCentreEventTypeRegistry.isProcessCentreMessage(record)){
      eventPublisher.publishEvent(ProcessCentreEventTypeRegistry.unpackMessage(record));
    }
  }

}
