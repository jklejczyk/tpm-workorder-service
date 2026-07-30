package pl.klejczyk.tpm.workorder.infrastructure.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.DefaultJacksonJavaTypeMapper;
import org.springframework.amqp.support.converter.JacksonJavaTypeMapper;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class RabbitConfiguration {

    static final String EXCHANGE = "tpm.events";
    static final String MACHINE_QUEUE = "q.workorder.machine";
    static final String MACHINE_DLQ = "q.workorder.machine.dlq";

    @Bean
    TopicExchange tpmEvents() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    Queue machineDeadLetterQueue() {
        return QueueBuilder.durable(MACHINE_DLQ).build();
    }

    @Bean
    Queue machineQueue() {
        return QueueBuilder.durable(MACHINE_QUEUE).deadLetterExchange("").deadLetterRoutingKey(MACHINE_DLQ).build();
    }

    @Bean
    Binding machineRegisteredBinding() {
        return BindingBuilder.bind(machineQueue()).to(tpmEvents()).with("machine.registered");
    }

    @Bean
    MessageConverter jsonMessageConverter() {
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter();
        DefaultJacksonJavaTypeMapper typeMapper = new DefaultJacksonJavaTypeMapper();
        typeMapper.setTypePrecedence(JacksonJavaTypeMapper.TypePrecedence.INFERRED);
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }
}
