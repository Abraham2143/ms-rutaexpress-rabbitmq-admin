package cl.rutaexpress.ms_rutaexpress_rabbitmq_admin.service;

import cl.rutaexpress.ms_rutaexpress_rabbitmq_admin.dto.BindingRequest;
import cl.rutaexpress.ms_rutaexpress_rabbitmq_admin.dto.ExchangeRequest;
import cl.rutaexpress.ms_rutaexpress_rabbitmq_admin.dto.QueueInfoResponse;
import cl.rutaexpress.ms_rutaexpress_rabbitmq_admin.dto.QueueRequest;
import cl.rutaexpress.ms_rutaexpress_rabbitmq_admin.exception.ConflictException;
import cl.rutaexpress.ms_rutaexpress_rabbitmq_admin.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.*;
import org.springframework.stereotype.Service;

/** Única clase que habla con RabbitMQ para administrar colas, exchanges y bindings. */
@Service
public class RabbitAdminService {

    private static final Logger log = LoggerFactory.getLogger(RabbitAdminService.class);
    private final AmqpAdmin amqpAdmin;

    public RabbitAdminService(AmqpAdmin amqpAdmin) {
        this.amqpAdmin = amqpAdmin;
    }

    // ---------- Colas ----------
    public QueueInfoResponse createQueue(QueueRequest r) {
        if (amqpAdmin.getQueueInfo(r.name()) != null) {
            throw new ConflictException("La cola '" + r.name() + "' ya existe");
        }
        QueueBuilder b = r.durable() ? QueueBuilder.durable(r.name()) : QueueBuilder.nonDurable(r.name());
        if (r.autoDelete()) b.autoDelete();
        if (r.deadLetterExchange() != null) b.deadLetterExchange(r.deadLetterExchange());
        if (r.deadLetterRoutingKey() != null) b.deadLetterRoutingKey(r.deadLetterRoutingKey());
        if (r.ttlMs() != null) b.ttl(r.ttlMs());
        if (r.maxLength() != null) b.maxLength(r.maxLength());

        try {
            amqpAdmin.declareQueue(b.build());
        } catch (AmqpException e) {
            log.error("Error creando cola {}: {}", r.name(), e.getMessage());
            throw new ConflictException("No se pudo crear la cola: " + e.getMessage());
        }
        log.info("Cola creada: {}", r.name());
        return getQueue(r.name());
    }

    public QueueInfoResponse getQueue(String name) {
        QueueInformation info = amqpAdmin.getQueueInfo(name);
        if (info == null) throw new ResourceNotFoundException("La cola '" + name + "' no existe");
        return new QueueInfoResponse(info.getName(), info.getMessageCount(), info.getConsumerCount());
    }

    public void deleteQueue(String name) {
        getQueue(name); // lanza 404 si no existe
        amqpAdmin.deleteQueue(name);
        log.info("Cola eliminada: {}", name);
    }

    // ---------- Exchanges ----------
    public void createExchange(ExchangeRequest r) {
        Exchange ex = switch (r.type()) {
            case DIRECT -> new DirectExchange(r.name(), r.durable(), r.autoDelete());
            case TOPIC -> new TopicExchange(r.name(), r.durable(), r.autoDelete());
            case FANOUT -> new FanoutExchange(r.name(), r.durable(), r.autoDelete());
            case HEADERS -> new HeadersExchange(r.name(), r.durable(), r.autoDelete());
        };
        try {
            amqpAdmin.declareExchange(ex);
        } catch (AmqpException e) {
            log.error("Error creando exchange {}: {}", r.name(), e.getMessage());
            throw new ConflictException("No se pudo crear el exchange: " + e.getMessage());
        }
        log.info("Exchange creado: {} ({})", r.name(), r.type());
    }

    public void deleteExchange(String name) {
        amqpAdmin.deleteExchange(name);
        log.info("Exchange eliminado: {}", name);
    }

    // ---------- Bindings ----------
    public void createBinding(BindingRequest r) {
        getQueue(r.queue()); // valida que la cola exista
        try {
            amqpAdmin.declareBinding(toBinding(r));
        } catch (AmqpException e) {
            log.error("Error creando binding: {}", e.getMessage());
            throw new ConflictException("No se pudo crear el binding: " + e.getMessage());
        }
        log.info("Binding creado: {} -> {} [{}]", r.exchange(), r.queue(), r.routingKey());
    }

    public void deleteBinding(BindingRequest r) {
        amqpAdmin.removeBinding(toBinding(r));
        log.info("Binding eliminado: {} -> {} [{}]", r.exchange(), r.queue(), r.routingKey());
    }

    private Binding toBinding(BindingRequest r) {
        return new Binding(r.queue(), Binding.DestinationType.QUEUE, r.exchange(), r.routingKey(), null);
    }
}