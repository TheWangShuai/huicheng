package com.totainfo.eap.cp.config;

import org.springframework.amqp.rabbit.AsyncRabbitTemplate;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MesRabbitMqConfig {

    @Value("${spring.rabbitmq.mes.host}")
    private String host;

    @Value("${spring.rabbitmq.mes.port}")
    private int port;

    @Value("${spring.rabbitmq.mes.username}")
    private String username;

    @Value("${spring.rabbitmq.mes.password}")
    private String password;

    @Value("${spring.rabbitmq.mes.virtualHost}")
    private String virtualHost;

    @Value("${spring.rabbitmq.mes.timeout}")
    private int rmsTimeout;

    @Bean
    @Qualifier("mesConnectionFactory")
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(host,port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setVirtualHost(virtualHost);
        return connectionFactory;
    }

    @Bean
    @Qualifier("mesRabbitTemplate")
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate template = new RabbitTemplate(connectionFactory());
        template.setReplyTimeout(rmsTimeout);
        return template;
    }

    @Bean
    @Qualifier("mesAsyncRabbitTemplate")
    public AsyncRabbitTemplate asyncRabbitTemplate(RabbitTemplate rabbitTemplate){
        return new AsyncRabbitTemplate(rabbitTemplate);
    }
}
