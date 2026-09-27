package com.eventosexpress.contratos;

public final class MensageriaConstants {
    private MensageriaConstants() {
    }

    public static final String EXCHANGE = "eventos.exchange";

    public static final String DEAD_LETTER_EXCHANGE = "eventos.dlx";

    public static final String FILA_VALIDAR_INSCRICAO = "eventos.validar-inscricao";

    public static final String FILA_RESULTADO_VALIDACAO = "inscricoes.resultado-validacao";

    public static final String FILA_NOTIFICACAO_INSCRICAO_CONFIRMADA = "notificacoes.inscricao-confirmada";

    public static final String FILA_MONITORAMENTO_INSCRICAO_CONFIRMADA = "monitoramento.inscricao-confirmada";

    public static final String FILA_DEAD_LETTER = "eventos.dead-letter";

    public static final String ROUTING_VALIDACAO_SOLICITADA = "inscricao.validacao.solicitada";

    public static final String ROUTING_VALIDACAO_RESPONDIDA = "inscricao.validacao.respondida";

    public static final String ROUTING_INSCRICAO_CONFIRMADA = "inscricao.confirmada";

    public static final String ROUTING_DEAD_LETTER = "mensagem.falha";
}

