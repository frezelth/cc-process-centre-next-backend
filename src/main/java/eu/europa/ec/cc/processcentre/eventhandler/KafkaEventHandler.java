package eu.europa.ec.cc.processcentre.eventhandler;

import eu.europa.ec.cc.processcentre.event.ProcessRegistered;
import eu.europa.ec.cc.processcentre.messaging.KafkaSender;
import eu.europa.ec.cc.processcentre.proto.UpdateProcessContext;
import eu.europa.ec.cc.processcentre.util.ProtoUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@ConditionalOnProperty("spring.kafka.bootstrap-servers")
@Slf4j
public class KafkaEventHandler {

  private final KafkaSender kafkaSender;

  public KafkaEventHandler(KafkaSender kafkaSender) {
    this.kafkaSender = kafkaSender;
  }

  @TransactionalEventListener
  public void on(ProcessRegistered event) {

    if (LOG.isDebugEnabled()){
      LOG.debug("Handling ProcessRegistered for process {}", event.processInstanceId());
    }

    // send a command to kafka to handle load of configuration
    // the process won't be visible until the config has been loaded
    kafkaSender.sendAndWaitToCommandTopic(
        event.processInstanceId(), UpdateProcessContext.newBuilder()
            .setProcessInstanceId(event.processInstanceId())
            .setProviderId(event.providerId())
            .setDomainKey(event.domainKey())
            .setProcessTypeKey(event.processTypeKey())
            .build()
    );

    if (LOG.isDebugEnabled()){
      LOG.debug("UpdateProcessContext sent for process {}", event.processInstanceId());
    }

    // send an event to kafka to notify the fact that the process has been registered in PC
    kafkaSender.sendToEventTopic(
        event.processInstanceId(), eu.europa.ec.cc.processcentre.event.proto.ProcessRegistered.newBuilder()
                .setProcessInstanceId(event.processInstanceId())
                .setOrClearUserId(event.userId())
                .setOrClearOnBehalfOfUserId(event.onBehalfOfUserId())
                .setTimestamp(ProtoUtils.instantToTimestamp(event.createdOn()))
            .build()
    );

    if (LOG.isDebugEnabled()){
      LOG.debug("ProcessRegistered sent for process {}", event.processInstanceId());
    }
  }

}
