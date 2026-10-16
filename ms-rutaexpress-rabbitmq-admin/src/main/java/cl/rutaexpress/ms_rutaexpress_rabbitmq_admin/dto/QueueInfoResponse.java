package cl.rutaexpress.ms_rutaexpress_rabbitmq_admin.dto;

public record QueueInfoResponse(String name, long messageCount, int consumerCount) {}