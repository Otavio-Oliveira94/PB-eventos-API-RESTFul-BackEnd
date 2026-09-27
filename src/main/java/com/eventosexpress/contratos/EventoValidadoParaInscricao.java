package com.eventosexpress.contratos;

import java.time.Instant;
import java.util.UUID;

public record EventoValidadoParaInscricao(
        UUID mensagemId,
        int versaoMensagem,
        Instant ocorridoEm,
        UUID solicitacaoId,
        Long inscricaoId,
        Long eventoId,
        boolean eventoExiste,
        String motivo
) {
}
