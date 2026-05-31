package rikser123.bundle.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import rikser123.bundle.component.KafkaConsumerInterceptor;
import rikser123.bundle.component.KafkaProducerInterceptor;
import rikser123.bundle.feign.SecurityClient;
import rikser123.bundle.service.PublicKeyLoaderService;
import rikser123.bundle.service.UserDetailService;

import java.util.Collections;
import java.util.HashMap;

@Configuration
public class KafkaConfig {

  @Bean
  @ConditionalOnProperty(name = "bundle.kafka.enabled", havingValue = "true")
  public KafkaProducerInterceptor producerInterceptor(SecurityClient securityClient, PublicKeyLoaderService publicKeyLoaderService) {
    return new KafkaProducerInterceptor(securityClient, publicKeyLoaderService);
  }

  @Bean
  @ConditionalOnProperty(name = "bundle.kafka.enabled", havingValue = "true")
  public ProducerFactory<String, String> producerFactory(KafkaProducerInterceptor producerInterceptor) {
    var config = new HashMap<String, Object>();
    config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    config.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG,
      Collections.singletonList(producerInterceptor.getClass()));

    return new DefaultKafkaProducerFactory<>(config);
  }

  @Bean
  @ConditionalOnProperty(name = "bundle.kafka.enabled", havingValue = "true")
  public KafkaConsumerInterceptor kafkaConsumerInterceptor(UserDetailService userDetailService) {
    return new KafkaConsumerInterceptor(userDetailService);
  }

  @Bean
  @ConditionalOnProperty(name = "bundle.kafka.enabled", havingValue = "true")
  public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory producerFactory) {
    return new KafkaTemplate<String, String>(producerFactory);
  }

  @Bean
  @ConditionalOnProperty(name = "bundle.kafka.enabled", havingValue = "true")
  public ConsumerFactory<String, String> consumerFactory(KafkaConsumerInterceptor interceptor) {
    var props = new HashMap<String, Object>();
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG,
      Collections.singletonList(interceptor.getClass()));

    return new DefaultKafkaConsumerFactory<>(props);
  }

  @Bean
  @ConditionalOnProperty(name = "bundle.kafka.enabled", havingValue = "true")
  public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
    ConsumerFactory<String, String> consumerFactory) {
    ConcurrentKafkaListenerContainerFactory<String, String> factory =
      new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory);
    return factory;
  }
}
