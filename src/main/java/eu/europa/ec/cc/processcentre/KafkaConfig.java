package eu.europa.ec.cc.processcentre;

import eu.europa.ec.cc.kafkaspringboot.CCMessageSerializer;
import eu.europa.ec.cc.message.proto.CCMessage;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaConsumerFactoryCustomizer;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaProducerFactoryCustomizer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.serializer.DelegatingByTypeSerializer;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@ConditionalOnProperty("spring.kafka.bootstrap-servers")
@EnableKafka
public class KafkaConfig {

  @Value("${kafka.event-topic.name}")
  private String eventTopicName;

  @Bean
  @Profile({"local", "docker"})
  public NewTopic eventTopic() {
    return TopicBuilder
        .name(eventTopicName)
        .partitions(10)
        .replicas(1)
        .build();
  }

  @Bean
  public DefaultKafkaConsumerFactoryCustomizer consumerFactoryCustomizer(){
    return (consumerFactory) ->
        filterSslProperties(consumerFactory::removeConfig, consumerFactory.getConfigurationProperties());
  }

  @Bean
  public DefaultKafkaProducerFactoryCustomizer producerFactoryCustomizer(){
    return (producerFactory) -> {
      // useless assignment to make the generics work
      DefaultKafkaProducerFactory f = producerFactory;
      filterSslProperties(producerFactory::removeConfig,
          producerFactory.getConfigurationProperties());
      Map<Class<?>, Serializer<?>> serializers = new HashMap<>();
      serializers.put(CCMessage.class, new CCMessageSerializer());
      serializers.put(byte[].class, new ByteArraySerializer());
      f.setValueSerializer(new DelegatingByTypeSerializer(serializers));
    };
  }

  /**
   * Need to do this stupid thing because some spring ssl properties are put as global in vault
   * if possible we need to change them to prefix them with infra.*
   * @return
   */
  private static void filterSslProperties(
      Consumer<String> consumer, Map<String, Object> configurationProperties) {
    if ("PEM".equals(configurationProperties.get(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG))) {
      consumer.accept(SslConfigs.SSL_KEY_PASSWORD_CONFIG);
      consumer.accept(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG);
      consumer.accept(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG);
    } else {
      consumer.accept(SslConfigs.SSL_KEYSTORE_KEY_CONFIG);
      consumer.accept(SslConfigs.SSL_KEYSTORE_CERTIFICATE_CHAIN_CONFIG);
    }

    if ("PEM".equals(configurationProperties.get(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG))){
      consumer.accept(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG);
      consumer.accept(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG);
    } else {
      consumer.accept(SslConfigs.SSL_TRUSTSTORE_CERTIFICATES_CONFIG);
    }
  }

  @Bean
  public KafkaTemplate<?, ?> kafkaTemplate(ProducerFactory<Object, Object> kafkaProducerFactory,
      ProducerListener<Object, Object> kafkaProducerListener,
      ObjectProvider<RecordMessageConverter> messageConverter,
      KafkaProperties properties) {
    PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
    KafkaTemplate<Object, Object> kafkaTemplate = new KafkaTemplate<>(kafkaProducerFactory);
    messageConverter.ifUnique(kafkaTemplate::setMessageConverter);
    map.from(kafkaProducerListener).to(kafkaTemplate::setProducerListener);
    map.from(properties.getTemplate().getDefaultTopic()).to(kafkaTemplate::setDefaultTopic);
    kafkaTemplate.setAllowNonTransactional(true);
    kafkaTemplate.setObservationEnabled(true);
    return kafkaTemplate;
  }

  @Bean
  public CommonErrorHandler errorHandler(){
    DefaultErrorHandler errorHandler = new DefaultErrorHandler(new FixedBackOff(5000L, 5L));
    errorHandler.setBackOffFunction(((consumerRecord, e) -> {

      // when we have a database issue we retry until the DB connection works again
      if (e instanceof KafkaException && e.getCause() instanceof DataAccessException) {
        return new FixedBackOff(10000L, FixedBackOff.UNLIMITED_ATTEMPTS);
      }

      // otherwise, go to the default backoff
      return null;
    }));
    return errorHandler;
  }
}
