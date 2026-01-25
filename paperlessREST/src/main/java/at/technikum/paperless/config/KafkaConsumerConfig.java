package at.technikum.paperless.config;

import at.technikum.paperless.messaging.OcrResultEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
@Profile("!test")
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, OcrResultEvent> consumerFactory() {
        Map<String, Object> config = new HashMap<>();

        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "paperless-rest-group");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                org.springframework.kafka.support.serializer.JsonDeserializer.class);

        // Decondigierung erlauben und Standard-Typ setzen
        config.put("spring.json.trusted.packages", "at.technikum.paperless.messaging");
        config.put("spring.json.value.default.type", OcrResultEvent.class.getName());

        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OcrResultEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, OcrResultEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory());

        factory.setRecordMessageConverter(new StringJsonMessageConverter());

        return factory;
    }
}
