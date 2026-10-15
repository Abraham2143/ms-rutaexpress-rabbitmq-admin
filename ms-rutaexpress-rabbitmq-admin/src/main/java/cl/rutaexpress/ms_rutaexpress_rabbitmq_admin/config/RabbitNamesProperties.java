package cl.rutaexpress.ms_rutaexpress_rabbitmq_admin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rutaexpress.rabbit")
public record RabbitNamesProperties(Exchanges exchanges, Flow email, Flow warehouse, Flow label) {

    public record Exchanges(String direct, String topic, String dead) {}

    public record Flow(String queue, String dlq, String directKey, String topicKey, String dlqKey) {}
}