package com.eventosexpress.eventospb.config;

import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static com.eventosexpress.contratos.MensageriaConstants.*;


@Configuration
public class RabbitMQConfig {
    private static final Logger log = LoggerFactory.getLogger(RabbitMQConfig.class);

    @Bean
    public Declarables estruturaRabbitMQ() {
        var exchange =
                new TopicExchange(
                        EXCHANGE,
                        true,
                        false
                );

        var deadLetterExchange =
                new TopicExchange(
                        DEAD_LETTER_EXCHANGE,
                        true,
                        false
                );

        var filaValidarInscricao =
                QueueBuilder
                        .durable(FILA_VALIDAR_INSCRICAO)
                        .deadLetterExchange(
                                DEAD_LETTER_EXCHANGE
                        )
                        .deadLetterRoutingKey(
                                ROUTING_DEAD_LETTER
                        )
                        .build();

        var filaResultadoValidacao =
                QueueBuilder
                        .durable(FILA_RESULTADO_VALIDACAO)
                        .deadLetterExchange(
                                DEAD_LETTER_EXCHANGE
                        )
                        .deadLetterRoutingKey(
                                ROUTING_DEAD_LETTER
                        )
                        .build();

        var filaNotificacao =
                QueueBuilder
                        .durable(
                                FILA_NOTIFICACAO_INSCRICAO_CONFIRMADA
                        )
                        .deadLetterExchange(
                                DEAD_LETTER_EXCHANGE
                        )
                        .deadLetterRoutingKey(
                                ROUTING_DEAD_LETTER
                        )
                        .build();

        var filaMonitoramento =
                QueueBuilder
                        .durable(
                                FILA_MONITORAMENTO_INSCRICAO_CONFIRMADA
                        )
                        .deadLetterExchange(
                                DEAD_LETTER_EXCHANGE
                        )
                        .deadLetterRoutingKey(
                                ROUTING_DEAD_LETTER
                        )
                        .build();

        var filaDeadLetter =
                QueueBuilder
                        .durable(FILA_DEAD_LETTER)
                        .build();

        var bindingValidacaoSolicitada =
                BindingBuilder
                        .bind(filaValidarInscricao)
                        .to(exchange)
                        .with(
                                ROUTING_VALIDACAO_SOLICITADA
                        );

        var bindingValidacaoRespondida =
                BindingBuilder
                        .bind(filaResultadoValidacao)
                        .to(exchange)
                        .with(
                                ROUTING_VALIDACAO_RESPONDIDA
                        );

        var bindingNotificacao =
                BindingBuilder
                        .bind(filaNotificacao)
                        .to(exchange)
                        .with(
                                ROUTING_INSCRICAO_CONFIRMADA
                        );

        var bindingMonitoramento =
                BindingBuilder
                        .bind(filaMonitoramento)
                        .to(exchange)
                        .with(
                                ROUTING_INSCRICAO_CONFIRMADA
                        );

        var bindingDeadLetter =
                BindingBuilder
                        .bind(filaDeadLetter)
                        .to(deadLetterExchange)
                        .with(
                                ROUTING_DEAD_LETTER
                        );

        return new Declarables(
                exchange,
                deadLetterExchange,
                filaValidarInscricao,
                filaResultadoValidacao,
                filaNotificacao,
                filaMonitoramento,
                filaDeadLetter,
                bindingValidacaoSolicitada,
                bindingValidacaoRespondida,
                bindingNotificacao,
                bindingMonitoramento,
                bindingDeadLetter
        );
    }

    @Bean
    public MessageConverter conversorJsonRabbitMQ(
            JsonMapper jsonMapper
    ) {
        return new JacksonJsonMessageConverter(
                jsonMapper,
                "com.eventosexpress.contratos"
        );
    }

    @Bean
    @ConditionalOnProperty(
            name = "mensageria.verificar-conexao",
            havingValue = "true"
    )
    public ApplicationRunner verificarConexaoRabbitMQ(
            AmqpAdmin amqpAdmin
    ) {
        return argumentos -> {
            amqpAdmin.initialize();

            List<String> filasObrigatorias =
                    List.of(
                            FILA_VALIDAR_INSCRICAO,
                            FILA_RESULTADO_VALIDACAO,
                            FILA_NOTIFICACAO_INSCRICAO_CONFIRMADA,
                            FILA_MONITORAMENTO_INSCRICAO_CONFIRMADA,
                            FILA_DEAD_LETTER
                    );

            for (String fila : filasObrigatorias) {
                if (amqpAdmin.getQueueInfo(fila) == null) {
                    throw new IllegalStateException(
                            "A fila obrigatoria nao foi criada: "
                                    + fila
                    );
                }
            }

            log.info(
                    "RabbitMQ conectado. Exchange principal, "
                            + "DLX e {} filas verificados.",
                    filasObrigatorias.size()
            );
        };
    }
}
