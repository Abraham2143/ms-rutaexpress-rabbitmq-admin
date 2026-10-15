package cl.rutaexpress.ms_rutaexpress_rabbitmq_admin.controller;

import cl.rutaexpress.ms_rutaexpress_rabbitmq_admin.dto.BindingRequest;
import cl.rutaexpress.ms_rutaexpress_rabbitmq_admin.dto.ExchangeRequest;
import cl.rutaexpress.ms_rutaexpress_rabbitmq_admin.dto.QueueInfoResponse;
import cl.rutaexpress.ms_rutaexpress_rabbitmq_admin.dto.QueueRequest;
import cl.rutaexpress.ms_rutaexpress_rabbitmq_admin.service.RabbitAdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/rabbit")
public class RabbitAdminController {

    private final RabbitAdminService service;

    public RabbitAdminController(RabbitAdminService service) {
        this.service = service;
    }

    // ----- Queues -----
    @PostMapping("/queues")
    public ResponseEntity<QueueInfoResponse> createQueue(@Valid @RequestBody QueueRequest request) {
        QueueInfoResponse created = service.createQueue(request);
        return ResponseEntity.created(URI.create("/api/rabbit/queues/" + created.name())).body(created);
    }

    @GetMapping("/queues/{name}")
    public QueueInfoResponse getQueue(@PathVariable String name) {
        return service.getQueue(name);
    }

    @DeleteMapping("/queues/{name}")
    public ResponseEntity<Void> deleteQueue(@PathVariable String name) {
        service.deleteQueue(name);
        return ResponseEntity.noContent().build();
    }

    // ----- Exchanges -----
    @PostMapping("/exchanges")
    public ResponseEntity<Void> createExchange(@Valid @RequestBody ExchangeRequest request) {
        service.createExchange(request);
        return ResponseEntity.created(URI.create("/api/rabbit/exchanges/" + request.name())).build();
    }

    @DeleteMapping("/exchanges/{name}")
    public ResponseEntity<Void> deleteExchange(@PathVariable String name) {
        service.deleteExchange(name);
        return ResponseEntity.noContent().build();
    }

    // ----- Bindings -----
    @PostMapping("/bindings")
    public ResponseEntity<Void> createBinding(@Valid @RequestBody BindingRequest request) {
        service.createBinding(request);
        return ResponseEntity.status(201).build();
    }

    @DeleteMapping("/bindings")
    public ResponseEntity<Void> deleteBinding(@Valid @RequestBody BindingRequest request) {
        service.deleteBinding(request);
        return ResponseEntity.noContent().build();
    }
}