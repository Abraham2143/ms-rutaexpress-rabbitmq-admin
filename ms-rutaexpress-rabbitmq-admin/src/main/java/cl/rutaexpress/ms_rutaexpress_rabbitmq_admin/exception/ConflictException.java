package cl.rutaexpress.ms_rutaexpress_rabbitmq_admin.exception;

public class ConflictException extends RuntimeException {
    public ConflictException(String msg) {
        super(msg);
    }
}