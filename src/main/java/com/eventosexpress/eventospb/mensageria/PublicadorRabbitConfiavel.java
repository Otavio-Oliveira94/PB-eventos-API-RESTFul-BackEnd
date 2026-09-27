package com.eventosexpress.eventospb.mensageria;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.eventosexpress.contratos.MensageriaConstants.EXCHANGE;

@Component
public class PublicadorRabbitConfiavel {

    private static final Logger log =
            LoggerFactory.getLogger(
                    PublicadorRabbitConfiavel.class
            );

    private static final long TEMPO_LIMITE_CONFIRMACAO_SEGUNDOS =
            5L;

    private final RabbitTemplate rabbitTemplate;

    public PublicadorRabbitConfiavel(
            RabbitTemplate rabbitTemplate
    ) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicar(
            String routingKey,
            Object conteudo,
            UUID mensagemId,
            UUID correlacaoId
    ) {
        if (routingKey == null || routingKey.isBlank()) {
            throw new IllegalArgumentException(
                    "A routing key da publicacao e obrigatoria."
            );
        }

        if (conteudo == null) {
            throw new IllegalArgumentException(
                    "O conteudo da publicacao e obrigatorio."
            );
        }

        if (mensagemId == null) {
            throw new IllegalArgumentException(
                    "O mensagemId da publicacao e obrigatorio."
            );
        }

        if (correlacaoId == null) {
            throw new IllegalArgumentException(
                    "O correlacaoId da publicacao e obrigatorio."
            );
        }

        CorrelationData dadosCorrelacao =
                new CorrelationData(
                        mensagemId.toString()
                );

        try {
            rabbitTemplate.convertAndSend(
                    EXCHANGE,
                    routingKey,
                    conteudo,
                    mensagem -> {
                        var propriedades =
                                mensagem.getMessageProperties();

                        propriedades.setMessageId(
                                mensagemId.toString()
                        );

                        propriedades.setCorrelationId(
                                correlacaoId.toString()
                        );

                        propriedades.setDeliveryMode(
                                MessageDeliveryMode.PERSISTENT
                        );

                        return mensagem;
                    },
                    dadosCorrelacao
            );

            CorrelationData.Confirm confirmacao =
                    dadosCorrelacao
                            .getFuture()
                            .get(
                                    TEMPO_LIMITE_CONFIRMACAO_SEGUNDOS,
                                    TimeUnit.SECONDS
                            );

            if (dadosCorrelacao.getReturned() != null) {
                throw new IllegalStateException(
                        "A mensagem "
                                + mensagemId
                                + " nao foi roteada para nenhuma fila. "
                                + "Routing key: "
                                + routingKey
                );
            }

            if (!confirmacao.ack()) {
                throw new IllegalStateException(
                        "O RabbitMQ recusou a mensagem "
                                + mensagemId
                                + ". Motivo: "
                                + confirmacao.reason()
                );
            }

            log.info(
                    "Publicacao confirmada pelo RabbitMQ. "
                            + "Mensagem: {}, routing key: {}",
                    mensagemId,
                    routingKey
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "A espera pela confirmacao da mensagem "
                            + mensagemId
                            + " foi interrompida.",
                    exception
            );
        } catch (
                ExecutionException
                | TimeoutException exception
        ) {
            throw new IllegalStateException(
                    "Nao foi possivel confirmar a mensagem "
                            + mensagemId
                            + " dentro do tempo limite.",
                    exception
            );
        }
    }
}
