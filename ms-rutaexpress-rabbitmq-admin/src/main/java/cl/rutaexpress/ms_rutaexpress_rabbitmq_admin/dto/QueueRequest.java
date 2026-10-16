package cl.rutaexpress.ms_rutaexpress_rabbitmq_admin.dto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record QueueRequest(
        @NotBlank(message = "El nombre de la cola es obligatorio")
        @Pattern(regexp = "^[A-Za-z0-9._-]{1,255}$",
                message = "Nombre inválido: solo letras, números, '.', '_' o '-' (máx. 255)")
        String name,
        boolean durable,
        boolean autoDelete,
        @Pattern(regexp = "^[A-Za-z0-9._-]{1,255}$", message = "deadLetterExchange inválido")
        String deadLetterExchange,
        String deadLetterRoutingKey,
        @Min(value = 1, message = "ttlMs debe ser mayor a 0")
        Integer ttlMs,
        @Min(value = 1, message = "maxLength debe ser mayor a 0")
        Integer maxLength
) {}