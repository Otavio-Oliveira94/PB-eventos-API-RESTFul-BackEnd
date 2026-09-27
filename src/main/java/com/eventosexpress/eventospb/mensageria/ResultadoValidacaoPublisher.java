package com.eventosexpress.eventospb.mensageria;

import com.eventosexpress.contratos.EventoValidadoParaInscricao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static com.eventosexpress.contratos.MensageriaConstants.ROUTING_VALIDACAO_RESPONDIDA;

@Component
public class ResultadoValidacaoPublisher {

    private static final Logger log = LoggerFactory.getLogger(ResultadoValidacaoPublisher.class);

    private final PublicadorRabbitConfiavel publicadorRabbit;

    public ResultadoValidacaoPublisher(
            PublicadorRabbitConfiavel publicadorRabbit
    ) {
        this.publicadorRabbit = publicadorRabbit;
    }

    public void publicar(
            EventoValidadoParaInscricao resultado
    ) {
        try {
            publicadorRabbit.publicar(
                    ROUTING_VALIDACAO_RESPONDIDA,
                    resultado,
                    resultado.mensagemId(),
                    resultado.solicitacaoId()
            );

            log.info(
                    "Resultado de validacao publicado. "
                            + "Inscricao: {}, eventoExiste: {}, "
                            + "solicitacao: {}",
                    resultado.inscricaoId(),
                    resultado.eventoExiste(),
                    resultado.solicitacaoId()
            );
        } catch (RuntimeException exception) {
            log.error(
                    "Falha ao publicar o resultado da validacao "
                            + "da inscricao {}.",
                    resultado.inscricaoId(),
                    exception
            );

            throw exception;
        }
    }
}