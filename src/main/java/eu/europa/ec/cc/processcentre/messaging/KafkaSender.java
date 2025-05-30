package eu.europa.ec.cc.processcentre.messaging;

import com.google.protobuf.Message;
import eu.europa.ec.cc.k2sse.proto.SendAddressedMessage;
import eu.europa.ec.cc.message.proto.CCMessage;
import eu.europa.ec.cc.message.proto.CCMessageFactory;
import java.util.concurrent.CompletableFuture;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty("spring.kafka.bootstrap-servers")
public class KafkaSender {

  private final KafkaTemplate<String, CCMessage> kafkaTemplate;

  public KafkaSender(KafkaTemplate<String, CCMessage> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  @Value("${kafka.command-topic.name}")
  private String commandTopic;

  @Value("${kafka.operational-command-topic.name}")
  private String operationalCommandTopic;

  @Value("${kafka.event-topic.name}")
  private String eventTopic;

  @Value("${kafka.sse-bridge-event-topic.name}")
  private String sseAddressedMessageTopic;

  /**
   * Send a message to a given topic without waiting for the ACK
   * Callback will be available on the producer's network thread to check if
   * the message has been actually sent, we DON'T use kafka transactions
   * @param topic
   * @param key
   * @param message
   * @return
   */
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public CompletableFuture<SendResult<String, CCMessage>> send(@NonNull String topic, @NonNull String key, Message message){
    return doSend(topic, key, message);
  }

  /**
   * Sends to a topic and wait for ACK, do NOT use kafka transactions for this
   * @param topic
   * @param key
   * @param message
   * @return
   */
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  @SneakyThrows
  public SendResult<String, CCMessage> sendAndWait(@NonNull String topic, @NonNull String key, Message message){
    return doSend(topic, key, message).get();
  }

  /**
   * Send a kafka message to the command topic, join a kafka transaction if it exists
   * @param key
   * @param message
   * @return
   */
  @SneakyThrows
  public SendResult<String, CCMessage> sendAndWaitToCommandTopic(@NonNull String key, Message message) {
    return doSend(commandTopic, key, message).get();
  }

  /**
   * Send a kafka message to the event topic, WITHOUT transactions
   * @param key
   * @param message
   * @return
   */
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public SendResult<String, CCMessage> sendAndWaitToEventTopic(String key, Message message) {
    return sendAndWait(eventTopic, key, message);
  }

  /**
   * Send a kafka message to the command topic, join kafka transaction if it exists
   * @param key
   * @param message
   * @return
   */
  public CompletableFuture<SendResult<String, CCMessage>> sendToCommandTopic(@NonNull String key, Message message) {
    return send(commandTopic, key, message);
  }

  /**
   * Send a kafka message to the operational command topic, join kafka transaction if it exists
   * @param key
   * @param message
   * @return
   */
  public CompletableFuture<SendResult<String, CCMessage>> sendToOperationalCommandTopic(@NonNull String key, Message message) {
    return send(operationalCommandTopic, key, message);
  }

  /**
   * Publish an addressed message, used by the front-end.
   *
   * @see <a
   *   href="https://citnet.tech.ec.europa.eu/CITnet/confluence/display/CCARCHITEC/Addressed+Messages%3A+implementation+guide">Addressed
   *   Messages: implementation guide</a>
   */
//  public CompletableFuture<SendResult<String, CCMessage>> sendToAddressedMessageTopic(
//      @NonNull String key,
//      String jsonMessage
//  ) {
//    final var addressedMessage =
//        SendAddressedMessage.newBuilder().setType(PROCESS_CHANGED.getType()).setKey(key).setMessage(jsonMessage).build();
//
//    final var ccMessage = CCMessageFactory.newMessage(addressedMessage);
//
//    return kafkaTemplate.send(sseAddressedMessageTopic, key, ccMessage);
//  }

  /**
   * Send a kafka message to the event topic, WITHOUT transactions
   * @param key
   * @param message
   * @return
   */
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public CompletableFuture<SendResult<String, CCMessage>> sendToEventTopic(String key, Message message) {
    return send(eventTopic, key, message);
  }

  /**
   * Send a spring Message to the command topic using transactions if they are available
   * @param message
   */
  @SneakyThrows
  public SendResult<String, CCMessage> sendToCommandTopic(
      org.springframework.messaging.Message<CCMessage> message) {
    return kafkaTemplate.send(message).get();
  }

  @SneakyThrows
  private CompletableFuture<SendResult<String, CCMessage>> doSend(@NonNull String topic, @NonNull String key, Message message) {
    if (message instanceof CCMessage){
      return kafkaTemplate.send(topic, key, (CCMessage)message);
    } else {
      return kafkaTemplate.send(topic, key, CCMessageFactory.newMessage(message));
    }
  }
}