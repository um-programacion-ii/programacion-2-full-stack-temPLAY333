package um.prog2.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:192.168.194.250:9092}")
    private String bootstrapServers;

    /**
     * Consumer factory para escuchar el topic de Kafka de Cátedra.
     * El Proxy NO publica en Kafka, solo consume.
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        // Forzar el valor correcto si viene mal configurado
        String kafkaServers = bootstrapServers;
        if (kafkaServers == null || kafkaServers.equals("kafka:9092") || kafkaServers.contains("kafka")) {
            kafkaServers = "192.168.194.250:9092";
        }
        System.out.println("Kafka bootstrap-servers configurado: " + kafkaServers);

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        // Configuración para resolver el problema de "kafka:9092" cuando el broker anuncia un hostname
        // En lugar de intentar resolver "kafka", usa directamente la IP configurada en bootstrap-servers
        props.put(ConsumerConfig.CLIENT_DNS_LOOKUP_CONFIG, "use_all_dns_ips");

        // groupId se define en el @KafkaListener para permitir múltiples grupos
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}
