package com.heightful.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.heightful.config.RabbitMqConfig;
import com.heightful.event.AccountRegistrationEvent;

@Service
public class RabbitMqService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publishAccountRegistrationEvent(AccountRegistrationEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.AUTH_EXCHANGE,
                RabbitMqConfig.AUTH_ACCOUNT_REGISTRATION_ROUTING,
                event);
    }
}
