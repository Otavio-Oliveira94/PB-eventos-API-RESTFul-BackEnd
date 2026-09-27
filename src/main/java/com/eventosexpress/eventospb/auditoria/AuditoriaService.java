package com.eventosexpress.eventospb.auditoria;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.List;
import java.util.UUID;

@Service
public class AuditoriaService {
    private static final Logger log = LoggerFactory.getLogger(AuditoriaService.class);
    private final JsonMapper jsonMapper;
    private final Path arquivo;
    private final String execucao = UUID.randomUUID().toString();
    private volatile boolean falhaGravacao;

    public AuditoriaService(JsonMapper jsonMapper,
                            @Value("${auditoria.arquivo}") String arquivo) throws IOException {
        this.jsonMapper = jsonMapper;
        this.arquivo = Path.of(arquivo).toAbsolutePath();
        Files.createDirectories(this.arquivo.getParent());
        // Falha na inicialização se o caminho não aceitar escrita.
        Files.writeString(this.arquivo, "", StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    public void registrar(String operacao, Long registroId, Object antes, Object depois) {
        JsonNode dadosAntes = jsonMapper.valueToTree(antes);
        JsonNode dadosDepois = jsonMapper.valueToTree(depois);
        if ("ATUALIZACAO".equals(operacao) && dadosAntes.equals(dadosDepois)) {
            return;
        }
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || !TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("A auditoria requer uma transação ativa.");
        }

        var registro = jsonMapper.createObjectNode();
        registro.put("id", UUID.randomUUID().toString());
        registro.put("instante", Instant.now().toString());
        registro.put("execucao", execucao);
        registro.put("operacao", operacao);
        registro.put("registroId", registroId);
        registro.set("antes", dadosAntes);
        registro.set("depois", dadosDepois);
        String linha = jsonMapper.writer().without(SerializationFeature.INDENT_OUTPUT)
                .writeValueAsString(registro);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_COMMITTED) {
                    gravar(linha);
                }
            }
        });
    }

    private synchronized void gravar(String linha) {
        try {
            Files.writeString(arquivo, linha + System.lineSeparator(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException exception) {
            falhaGravacao = true;
            log.error("FALHA DE AUDITORIA: alteração confirmada, mas o histórico não foi gravado em {}",
                    arquivo, exception);
        }
    }

    public synchronized List<JsonNode> consultar(Long registroId, String execucao, int limite) {
        if (limite < 1 || limite > 500 || (registroId != null && registroId < 1)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Informe um ID positivo e um limite entre 1 e 500.");
        }
        if (falhaGravacao) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "O histórico está incompleto após uma falha de gravação. Consulte o log do serviço.");
        }

        var ultimos = new ArrayDeque<JsonNode>();
        try (var leitor = Files.newBufferedReader(arquivo, StandardCharsets.UTF_8)) {
            String linha;
            while ((linha = leitor.readLine()) != null) {
                if (linha.isBlank()) {
                    continue;
                }
                JsonNode registro = jsonMapper.readTree(linha);
                if (registro == null || !registro.isObject() || !registro.hasNonNull("registroId")
                        || !registro.hasNonNull("execucao") || !registro.hasNonNull("operacao")) {
                    throw new IOException("Registro de auditoria inválido.");
                }
                if (registroId != null && registro.path("registroId").asLong() != registroId) {
                    continue;
                }
                if (execucao != null && !execucao.isBlank()
                        && !execucao.equals(registro.path("execucao").asText())) {
                    continue;
                }
                ultimos.addFirst(registro);
                if (ultimos.size() > limite) {
                    ultimos.removeLast();
                }
            }
            return List.copyOf(ultimos);
        } catch (IOException | tools.jackson.core.JacksonException exception) {
            log.error("Não foi possível ler o arquivo de auditoria {}", arquivo, exception);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Não foi possível consultar o histórico de alterações.", exception);
        }
    }
}
