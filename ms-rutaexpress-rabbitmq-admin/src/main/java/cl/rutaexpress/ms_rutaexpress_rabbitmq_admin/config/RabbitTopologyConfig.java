package cl.rutaexpress.ms_rutaexpress_rabbitmq_admin.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RabbitNamesProperties.class)
public class RabbitTopologyConfig {

    // ---------- Exchanges ----------
    @Bean
    public DirectExchange cmdDirectExchange(RabbitNamesProperties p) {
        return new DirectExchange(p.exchanges().direct(), true, false);
    }

    @Bean
    public TopicExchange cmdTopicExchange(RabbitNamesProperties p) {
        return new TopicExchange(p.exchanges().topic(), true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange(RabbitNamesProperties p) {
        return new DirectExchange(p.exchanges().dead(), true, false);
    }

    // ---------- Flujo 1: Email / push al destinatario ----------
    @Bean
    public Declarables emailTopology(RabbitNamesProperties p, DirectExchange cmdDirectExchange,
                                     TopicExchange cmdTopicExchange, DirectExchange deadLetterExchange) {
        return buildFlow(p.email(), p.exchanges().dead(), cmdDirectExchange, cmdTopicExchange, deadLetterExchange);
    }

    // ---------- Flujo 2: Ticket de picking / bodega ----------
    @Bean
    public Declarables warehouseTopology(RabbitNamesProperties p, DirectExchange cmdDirectExchange,
                                         TopicExchange cmdTopicExchange, DirectExchange deadLetterExchange) {
        return buildFlow(p.warehouse(), p.exchanges().dead(), cmdDirectExchange, cmdTopicExchange, deadLetterExchange);
    }

    // ---------- Flujo 3: Generación de PDF (guía / etiqueta) ----------
    @Bean
    public Declarables labelTopology(RabbitNamesProperties p, DirectExchange cmdDirectExchange,
                                     TopicExchange cmdTopicExchange, DirectExchange deadLetterExchange) {
        return buildFlow(p.label(), p.exchanges().dead(), cmdDirectExchange, cmdTopicExchange, deadLetterExchange);
    }

    // ---------- Forzar la conexión al iniciar para que se declare la topología ----------
    @Bean
    public ApplicationRunner declareTopologyOnStartup(ConnectionFactory connectionFactory) {
        return args -> {
            Connection connection = connectionFactory.createConnection();
            connection.close();
        };
    }

    private Declarables buildFlow(RabbitNamesProperties.Flow f, String deadExchangeName,
                                  DirectExchange direct, TopicExchange topic, DirectExchange dead) {
        Queue main = QueueBuilder.durable(f.queue())
                .deadLetterExchange(deadExchangeName)
                .deadLetterRoutingKey(f.dlqKey())
                .build();
        Queue dlq = QueueBuilder.durable(f.dlq()).build();

        return new Declarables(
                main, dlq,
                BindingBuilder.bind(main).to(direct).with(f.directKey()),
                BindingBuilder.bind(main).to(topic).with(f.topicKey()),
                BindingBuilder.bind(dlq).to(dead).with(f.dlqKey())
        );
    }
}