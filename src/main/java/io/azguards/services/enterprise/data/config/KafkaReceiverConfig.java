package io.azguards.services.enterprise.data.config;

import io.azguards.services.enterprise.data.common.Constant;
import io.marketplace.commons.logging.Logger;
import io.marketplace.commons.logging.LoggerFactory;
import io.marketplace.commons.utils.StringUtils;
import io.marketplace.services.pxchange.client.config.KafkaExtraProperties;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import java.util.Map;

@EnableKafka
@Configuration
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true")
public class KafkaReceiverConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaReceiverConfig.class);

    @Value("${kafka.server}")
    private String bootstrapServers;
    @Value("${kafka.consumer.maxPollRecords:10}")
    private int maxPollRecords;
    @Value("${kafka.consumer.number-processor.ent-data-loaded:1}")
    private int numberProcessorEntDataLoaded;
    @Value("${kafka.consumer.number-processor.ent-data-group-loaded:1}")
    private int numberProcessorEntDataGroupLoaded;

    private static final String KAFKA_TIMEOUT = "60000";
    private static final String KAFKA_COMMIT_INTERVAL = "5000";
    private static final String HEARTBEAT_INTERVAL_MS_CONFIG = "20000";

    private Map<String, Object> consumerConfigs(KafkaProperties kafkaProperties) {
        Map<String, Object> props = kafkaProperties.buildConsumerProperties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, KAFKA_TIMEOUT);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, KAFKA_COMMIT_INTERVAL);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, HEARTBEAT_INTERVAL_MS_CONFIG);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerEntDataLoadedContainerFactory(
        KafkaProperties kafkaProperties, KafkaExtraProperties appProps) {
        LOGGER.debug("Creating kafkaListenerEntDataLoadedContainerFactory with Receiver bootstrapServers: {}", bootstrapServers);
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        Map<String, Object> consumerConfigs = this.consumerConfigs(kafkaProperties);
        if (StringUtils.isNotEmpty(appProps.getJaasConfig())) {
            consumerConfigs.put(AdminClientConfig.SECURITY_PROTOCOL_CONFIG, Constant.SECURITY_PROTOCOL_CONFIG_KEY);
            consumerConfigs.put(Constant.SSL_ENDPOINT_ALG_CONFIG_KEY, "");
            String jaasCfg = appProps.getJaasConfig();
            consumerConfigs.put(Constant.SASL_JAAS_CONFIG_KEY, jaasCfg);
        }

        ConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerConfigs);
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(numberProcessorEntDataLoaded);
        return factory;
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerEntDataGroupLoadedContainerFactory(
        KafkaProperties kafkaProperties, KafkaExtraProperties appProps) {
        LOGGER.debug("Creating kafkaListenerEntDataGroupLoadedContainerFactory with Receiver bootstrapServers: {}", bootstrapServers);
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        Map<String, Object> consumerConfigs = this.consumerConfigs(kafkaProperties);
        if (StringUtils.isNotEmpty(appProps.getJaasConfig())) {
            consumerConfigs.put(AdminClientConfig.SECURITY_PROTOCOL_CONFIG, Constant.SECURITY_PROTOCOL_CONFIG_KEY);
            consumerConfigs.put(Constant.SSL_ENDPOINT_ALG_CONFIG_KEY, "");
            String jaasCfg = appProps.getJaasConfig();
            consumerConfigs.put(Constant.SASL_JAAS_CONFIG_KEY, jaasCfg);
        }

        ConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerConfigs);
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(numberProcessorEntDataGroupLoaded);
        return factory;
    }
}
