package com.eventosexpress.eventospb.mensageria;

import com.eventosexpress.contratos.EventoValidadoParaInscricao;
import com.eventosexpress.contratos.ValidarEventoParaInscricao;
import com.eventosexpress.eventospb.repository.EventoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

import static com.eventosexpress.contratos.MensageriaConstants.FILA_VALIDAR_INSCRICAO;

@Component
public class ValidacaoInscricaoListener {
    private static final Logger log = LoggerFactory.getLogger(ValidacaoInscricaoListener.class);

    private final EventoRepository eventoRepository;
    private final ResultadoValidacaoPublisher resultadoPublisher;

    public ValidacaoInscricaoListener(
            EventoRepository eventoRepository,
            ResultadoValidacaoPublisher resultadoPublisher
    ) {
        this.eventoRepository = eventoRepository;
        this.resultadoPublisher = resultadoPublisher;
    }

    @RabbitListener(queues = FILA_VALIDAR_INSCRICAO)
    public void consumir(ValidarEventoParaInscricao solicitacao) {
        log.info(
                "Validando evento {} para inscricao {}. Solicitacao: {}",
                solicitacao.eventoId(),
                solicitacao.inscricaoId(),
                solicitacao.solicitacaoId()
        );

        boolean eventoExiste =
                eventoRepository.existsById(solicitacao.eventoId());

        String motivo = eventoExiste
                ? null
                : "Evento nao encontrado.";

        var resultado = new EventoValidadoParaInscricao(
                UUID.randomUUID(),
                1,
                Instant.now(),
                solicitacao.solicitacaoId(),
                solicitacao.inscricaoId(),
                solicitacao.eventoId(),
                eventoExiste,
                motivo
        );

        resultadoPublisher.publicar(resultado);

        log.info(
                "Resultado encaminhado para publicacao. "
                        + "Inscricao: {}, eventoExiste: {}, solicitacao: {}",
                resultado.inscricaoId(),
                resultado.eventoExiste(),
                resultado.solicitacaoId()
        );
    }
}
