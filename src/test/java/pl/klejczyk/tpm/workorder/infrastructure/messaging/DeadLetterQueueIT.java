package pl.klejczyk.tpm.workorder.infrastructure.messaging;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import pl.klejczyk.tpm.workorder.TestcontainersConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class DeadLetterQueueIT {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    void parksAnUnprocessableMessageInsteadOfRetryingItForever() {
        rabbitTemplate.convertAndSend(
                RabbitConfiguration.EXCHANGE,
                "machine.registered",
                "this is not an event envelope");

        Message parked = rabbitTemplate.receive(RabbitConfiguration.MACHINE_DLQ, 20_000);

        assertThat(parked).isNotNull();
        assertThat(new String(parked.getBody())).contains("not an event envelope");
    }
}
