package cl.rutaexpress.ms_rutaexpress_rabbitmq_admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BindingRequest(
        @NotBlank(message = "La cola de destino es obligatoria") String queue,
        @NotBlank(message = "El exchange es obligatorio") String exchange,
        @NotNull(message = "routingKey es obligatorio (puede ser vacío para fanout)") String routingKey
) {}