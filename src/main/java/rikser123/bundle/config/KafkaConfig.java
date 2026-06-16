package rikser123.bundle.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.zalando.logbook.BodyFilter;
import rikser123.bundle.component.KafkaConsumerInterceptor;
import rikser123.bundle.component.KafkaLogger;
import rikser123.bundle.component.KafkaProducerInterceptor;
import rikser123.bundle.feign.SecurityClient;
import rikser123.bundle.service.PublicKeyLoaderService;
import rikser123.bundle.service.UserDetailService;

import java.util.HashMap;

@Configuration
public class KafkaConfig {
  private String bootstrapServers;

  @Value("${bundle.kafka.kafkaUrl}")
  @Autowired(required = false)
  public void setBootstrapServers(String bootstrapServers) {
    this.bootstrapServers = bootstrapServers;
  }

  @Bean
  public KafkaLogger kafkaLogger(BodyFilter bodyFilter) {
    return new KafkaLogger(bodyFilter);
  }

  @Bean
  @ConditionalOnProperty(name = "bundle.kafka.enabled", havingValue = "true")
  public KafkaProducerInterceptor producerInterceptor(
    SecurityClient securityClient,
    PublicKeyLoaderService publicKeyLoaderService,
    KafkaLogger kafkaLogger
  ) {
    return new KafkaProducerInterceptor(kafkaLogger, securityClient, publicKeyLoaderService);
  }

  @Bean
  @ConditionalOnProperty(name = "bundle.kafka.enabled", havingValue = "true")
  public ProducerFactory<String, String> producerFactory() {
    var config = new HashMap<String, Object>();
    config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

    return new DefaultKafkaProducerFactory<>(config);
  }

  @Bean
  @ConditionalOnProperty(name = "bundle.kafka.enabled", havingValue = "true")
  public KafkaConsumerInterceptor kafkaConsumerInterceptor(UserDetailService userDetailService, KafkaLogger kafkaLogger) {
    return new KafkaConsumerInterceptor(userDetailService, kafkaLogger);
  }

  @Bean
  @ConditionalOnProperty(name = "bundle.kafka.enabled", havingValue = "true")
  public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory producerFactory, KafkaProducerInterceptor producerInterceptor) {
    var template = new KafkaTemplate<String, String>(producerFactory);
    template.setProducerInterceptor(producerInterceptor);
    template.setObservationEnabled(true);
    return template;
  }

  @Bean
  @ConditionalOnProperty(name = "bundle.kafka.enabled", havingValue = "true")
  public ConsumerFactory<String, String> consumerFactory() {
    var props = new HashMap<String, Object>();
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

    return new DefaultKafkaConsumerFactory<>(props);
  }

  @Bean
  @ConditionalOnProperty(name = "bundle.kafka.enabled", havingValue = "true")
  public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
    ConsumerFactory<String, String> consumerFactory,
    KafkaConsumerInterceptor kafkaConsumerInterceptor
  ) {
    ConcurrentKafkaListenerContainerFactory<String, String> factory =
      new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory);
    factory.setRecordInterceptor(kafkaConsumerInterceptor);
    factory.getContainerProperties().setObservationEnabled(true);
    return factory;
  }
}
