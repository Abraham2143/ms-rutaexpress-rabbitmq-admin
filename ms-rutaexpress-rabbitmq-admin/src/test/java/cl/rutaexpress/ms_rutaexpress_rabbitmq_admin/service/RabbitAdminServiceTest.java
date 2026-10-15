package cl.rutaexpress.ms_rutaexpress_rabbitmq_admin.service;
import cl.rutaexpress.ms_rutaexpress_rabbitmq_admin.dto.QueueInfoResponse;
import cl.rutaexpress.ms_rutaexpress_rabbitmq_admin.dto.QueueRequest;
import cl.rutaexpress.ms_rutaexpress_rabbitmq_admin.exception.ConflictException;
import cl.rutaexpress.ms_rutaexpress_rabbitmq_admin.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueInformation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RabbitAdminServiceTest {

    @Mock
    private AmqpAdmin amqpAdmin;

    @InjectMocks
    private RabbitAdminService service;

    private QueueRequest request(String name) {
        return new QueueRequest(name, true, false, null, null, null, null);
    }

    @Test
    void createQueue_declaraLaColaCuandoNoExiste() {
        when(amqpAdmin.getQueueInfo("q.test"))
                .thenReturn(null, new QueueInformation("q.test", 0, 0));

        QueueInfoResponse res = service.createQueue(request("q.test"));

        assertEquals("q.test", res.name());
        verify(amqpAdmin).declareQueue(any(Queue.class));
    }

    @Test
    void createQueue_lanzaConflictSiYaExiste() {
        when(amqpAdmin.getQueueInfo("q.test")).thenReturn(new QueueInformation("q.test", 0, 0));

        assertThrows(ConflictException.class, () -> service.createQueue(request("q.test")));
        verify(amqpAdmin, never()).declareQueue(any());
    }

    @Test
    void getQueue_lanzaNotFoundSiNoExiste() {
        when(amqpAdmin.getQueueInfo("q.nada")).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> service.getQueue("q.nada"));
    }

    @Test
    void deleteQueue_eliminaLaColaExistente() {
        when(amqpAdmin.getQueueInfo("q.test")).thenReturn(new QueueInformation("q.test", 0, 0));

        service.deleteQueue("q.test");

        verify(amqpAdmin).deleteQueue("q.test");
    }
}