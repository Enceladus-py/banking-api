package com.example.demo.common.infrastructure.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@EnableKafka
@Configuration
public class KafkaConfig {

	@Value("${spring.kafka.bootstrap-servers:localhost:9092}")
	private String bootstrapServers;

	@Bean
	public ProducerFactory<String, String> producerFactory() {
		Map<String, Object> configProps = new HashMap<>();
		configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		return new DefaultKafkaProducerFactory<>(configProps);
	}

	@Bean
	public KafkaTemplate<String, String> kafkaTemplate() {
		return new KafkaTemplate<>(producerFactory());
	}

	@Bean
	public org.springframework.kafka.core.ConsumerFactory<String, String> consumerFactory() {
		Map<String, Object> configProps = new HashMap<>();
		configProps.put(org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		configProps.put(org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
				org.apache.kafka.common.serialization.StringDeserializer.class);
		configProps.put(org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
				org.apache.kafka.common.serialization.StringDeserializer.class);
		configProps.put(org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG, "transaction-group");
		configProps.put(org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		return new org.springframework.kafka.core.DefaultKafkaConsumerFactory<>(configProps);
	}

	@Bean
	public org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
		org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory<String, String> factory = new org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory());
		return factory;
	}
}
