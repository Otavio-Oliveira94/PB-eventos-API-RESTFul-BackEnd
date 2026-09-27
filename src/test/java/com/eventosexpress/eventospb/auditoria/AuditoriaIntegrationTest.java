package com.eventosexpress.eventospb.auditoria;

import com.eventosexpress.eventospb.dto.EventoRequestDTO;
import com.eventosexpress.eventospb.repository.EventoRepository;
import com.eventosexpress.eventospb.service.EventoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuditoriaIntegrationTest {
    private static final Path ARQUIVO = criarArquivoTemporario();
    @Autowired
    MockMvc mvc;
    @Autowired
    JsonMapper jsonMapper;
    @Autowired
    EventoService service;
    @Autowired
    EventoRepository repository;
    @Autowired AuditoriaService auditoria;
    @Autowired
    PlatformTransactionManager transactionManager;

    private static Path criarArquivoTemporario() {
        try {
            return Files.createTempDirectory("auditoria-eventos-").resolve("historico.jsonl");
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    @DynamicPropertySource
    static void propriedades(DynamicPropertyRegistry registry) {
        registry.add("auditoria.arquivo", ARQUIVO::toString);
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:auditoriaeventos;DB_CLOSE_DELAY=-1");
    }

    @BeforeEach
    void preparar() throws IOException {
        repository.deleteAll();
        Files.writeString(ARQUIVO, "");

    }

    private String dados(String nome) {
        return """
                {"titulo":"%s","subTitulo":"Teste", "tipoEvento":"Curso",
                 "endereco":{"rua":"Rua A", "numero":"10", "cidade":"Galway"},
                 "inicioEvento":"2026-10-01T10:00:00", "terminoEvento":"2026-10-01T12:00:00"}
                """.formatted(nome);
    }

    private long criar(String nome) throws Exception {
        var resultado = mvc.perform(post("/eventos").contentType(MediaType.APPLICATION_JSON)
                        .content(dados(nome)))
                .andExpect(status().isCreated()).andReturn();
        return jsonMapper.readTree(resultado.getResponse().getContentAsString()).path("id").asLong();
    }

    @Test
    void registraCriacaoEdicaoExclusaoEPermiteConsultaAposExclusao() throws Exception {
        long id = criar("Antes");
        mvc.perform(put("/eventos/" + id).contentType(MediaType.APPLICATION_JSON).content(dados("Depois")))
                .andExpect(status().isOk());
        mvc.perform(delete("/eventos/" + id)).andExpect(status().isNoContent());
        assertFalse(repository.existsById(id));
        mvc.perform(get("/auditoria").param("registroId", Long.toString(id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].operacao").value("EXCLUSAO"))
                .andExpect(jsonPath("$[0].antes.titulo").value("Depois"))
                .andExpect(jsonPath("$[0].depois").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$[1].operacao").value("ATUALIZACAO"))
                .andExpect(jsonPath("$[1].antes.titulo").value("Antes"))
                .andExpect(jsonPath("$[1].depois.titulo").value("Depois"))
                .andExpect(jsonPath("$[2].operacao").value("CRIACAO"))
                .andExpect(jsonPath("$[2].antes").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void rollbackNaoDeixaHistorico() {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            service.criarEvento(jsonMapper.readValue(dados("Cancelado"), EventoRequestDTO.class));
            assertTrue(auditoria.consultar(null, null, 100).isEmpty());
            status.setRollbackOnly();
        });
        assertEquals(0, repository.count());
        assertTrue(auditoria.consultar(null, null, 100).isEmpty());
    }

    @Test
    void atualizacaoSemMudancaNaoDuplicaHistorico() throws Exception {
        long id = criar("Mesmo nome");
        mvc.perform(put("/eventos/" + id).contentType(MediaType.APPLICATION_JSON).content(dados("Mesmo nome")))
                .andExpect(status().isOk());
        assertEquals(1, auditoria.consultar(id, null, 100).size());
    }

    @Test
    void filtraLimitaERecuperaArquivoComNovaInstancia() throws Exception {
        long primeiro = criar("Primeiro");
        long segundo = criar("Segundo");
        var outraInstancia = new AuditoriaService(jsonMapper, ARQUIVO.toString());
        assertEquals(2, outraInstancia.consultar(null, null, 100).size());
        var ultimo = outraInstancia.consultar(null, null, 1).getFirst();
        assertEquals(segundo, ultimo.path("registroId").asLong());
        assertEquals(1, outraInstancia.consultar(primeiro, null, 100).size());
        assertEquals(2, outraInstancia.consultar(null, ultimo.path("execucao").asText(), 100).size());
        assertTrue(outraInstancia.consultar(null, "outra-execucao", 100).isEmpty());
    }

    @Test
    void validaFiltrosEPermiteCorsDoFrontend() throws Exception {
        mvc.perform(get("/auditoria").param("limite", "0")).andExpect(status().isBadRequest());
        mvc.perform(get("/auditoria").param("limite", "501")).andExpect(status().isBadRequest());
        mvc.perform(get("/auditoria").param("registroId", "-1")).andExpect(status().isBadRequest());
        mvc.perform(get("/auditoria").header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(content().json("[]"));
    }

    @Test
    void arquivoCorrompidoNaoEhApresentadoComoHistoricoVazio() throws Exception {
        Files.writeString(ARQUIVO, "json incompleto\n");
        mvc.perform(get("/auditoria")).andExpect(status().isServiceUnavailable());
    }
}
