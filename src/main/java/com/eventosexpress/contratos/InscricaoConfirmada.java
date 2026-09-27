package com.eventosexpress.contratos;

import java.time.Instant;
import java.util.UUID;

public record InscricaoConfirmada(
        UUID mensagemId,
        int versaoMensagem,
        Instant ocorridoEm,
        UUID solicitacaoId,
        Long inscricaoId,
        Long eventoId,
        String nomeParticipante,
        String emailParticipante
) {
}
