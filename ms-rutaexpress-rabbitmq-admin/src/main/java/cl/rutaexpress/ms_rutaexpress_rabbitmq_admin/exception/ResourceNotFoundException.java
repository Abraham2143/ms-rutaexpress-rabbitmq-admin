package cl.rutaexpress.ms_rutaexpress_rabbitmq_admin.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String msg) {
        super(msg);
    }
}