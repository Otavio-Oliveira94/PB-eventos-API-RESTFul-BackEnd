package com.eventosexpress.eventospb.mensageria;

import com.eventosexpress.contratos.InscricaoConfirmada;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.eventosexpress.contratos.MensageriaConstants.FILA_MONITORAMENTO_INSCRICAO_CONFIRMADA;

@Component
public class MonitoramentoInscricaoListener {
    private static final Logger log = LoggerFactory.getLogger(MonitoramentoInscricaoListener.class);

    @RabbitListener(
            queues = FILA_MONITORAMENTO_INSCRICAO_CONFIRMADA,
            ackMode = "AUTO"
    )
    public void consumir(
            InscricaoConfirmada evento
    ) {
        log.info(
                "[MONITORAMENTO] Inscricao {} confirmada "
                        + "para o evento {}. Solicitacao: {}",
                evento.inscricaoId(),
                evento.eventoId(),
                evento.solicitacaoId()
        );
    }
}
