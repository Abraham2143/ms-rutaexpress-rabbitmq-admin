package cl.rutaexpress.ms_rutaexpress_rabbitmq_admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ExchangeRequest(
        @NotBlank(message = "El nombre del exchange es obligatorio")
        @Pattern(regexp = "^[A-Za-z0-9._-]{1,255}$", message = "Nombre inválido")
        String name,
        @NotNull(message = "El tipo es obligatorio (DIRECT, TOPIC, FANOUT, HEADERS)")
        Type type,
        boolean durable,
        boolean autoDelete
) {
    public enum Type { DIRECT, TOPIC, FANOUT, HEADERS }
}