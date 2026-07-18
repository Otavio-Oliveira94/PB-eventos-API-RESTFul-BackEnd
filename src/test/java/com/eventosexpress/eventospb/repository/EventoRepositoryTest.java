package com.eventosexpress.eventospb.repository;

import com.eventosexpress.eventospb.model.Evento;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import com.eventosexpress.eventospb.model.Endereco;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class EventoRepositoryTest {
    @Autowired
    private EventoRepository eventoRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @DisplayName("Deve salvar um evento com endereço")
    void deveSalvarEventoComEndereco() {
        Evento evento = criarEvento("Feira de Tecnologia");

        Evento eventoSalvo = eventoRepository.saveAndFlush(evento);
        Long eventoId = eventoSalvo.getId();

        entityManager.clear();

        Evento eventoEncontrado = eventoRepository.findById(eventoId)
                .orElseThrow();

        assertAll(
                () -> assertNotNull(eventoEncontrado.getId()),
                () -> assertEquals(
                        "Feira de Tecnologia",
                        eventoEncontrado.getTitulo()
                ),
                () -> assertEquals(
                        "Galway",
                        eventoEncontrado.getEndereco().getCidade()
                ),
                () -> assertEquals(
                        "Centro de Eventos",
                        eventoEncontrado.getEndereco().getComplemento()
                )
        );
    }

    @Test
    @DisplayName("Deve buscar um evento pelo ID")
    void deveBuscarEventoPorId() {
        Evento eventoSalvo = eventoRepository.saveAndFlush(
                criarEvento("Festival de Música")
        );

        Long eventoId = eventoSalvo.getId();

        entityManager.clear();

        Optional<Evento> resultado =
                eventoRepository.findById(eventoId);

        assertTrue(resultado.isPresent());
        assertEquals(
                "Festival de Música",
                resultado.get().getTitulo()
        );
    }

    @Test
    @DisplayName("Deve listar todos os eventos cadastrados")
    void deveListarTodosOsEventos() {
        Evento primeiroEvento = criarEvento("Evento de Tecnologia");
        Evento segundoEvento = criarEvento("Evento de Música");

        eventoRepository.saveAll(
                List.of(primeiroEvento, segundoEvento)
        );

        eventoRepository.flush();
        entityManager.clear();

        List<Evento> eventos = eventoRepository.findAll();

        assertEquals(2, eventos.size());

        assertTrue(
                eventos.stream().anyMatch(
                        evento -> evento.getTitulo()
                                .equals("Evento de Tecnologia")
                )
        );

        assertTrue(
                eventos.stream().anyMatch(
                        evento -> evento.getTitulo()
                                .equals("Evento de Música")
                )
        );
    }

    @Test
    @DisplayName("Deve atualizar um evento existente")
    void deveAtualizarEventoExistente() {
        Evento eventoSalvo = eventoRepository.saveAndFlush(
                criarEvento("Título Antigo")
        );

        Long eventoId = eventoSalvo.getId();

        eventoSalvo.setTitulo("Título Atualizado");
        eventoSalvo.getEndereco().setCidade("Dublin");

        eventoRepository.saveAndFlush(eventoSalvo);
        entityManager.clear();

        Evento eventoAtualizado = eventoRepository.findById(eventoId)
                .orElseThrow();

        assertEquals(
                "Título Atualizado",
                eventoAtualizado.getTitulo()
        );

        assertEquals(
                "Dublin",
                eventoAtualizado.getEndereco().getCidade()
        );
    }

    @Test
    @DisplayName("Deve excluir um evento existente")
    void deveExcluirEventoExistente() {
        Evento eventoSalvo = eventoRepository.saveAndFlush(
                criarEvento("Evento para Exclusão")
        );

        Long eventoId = eventoSalvo.getId();

        eventoRepository.deleteById(eventoId);
        eventoRepository.flush();
        entityManager.clear();

        boolean eventoExiste =
                eventoRepository.existsById(eventoId);

        assertFalse(eventoExiste);
    }

    @Test
    @DisplayName("Deve retornar vazio quando o ID não existir")
    void deveRetornarVazioQuandoIdNaoExistir() {
        Optional<Evento> resultado =
                eventoRepository.findById(999L);

        assertTrue(resultado.isEmpty());
    }

    private Evento criarEvento(String titulo) {
        Endereco endereco = new Endereco();

        endereco.setRua("Rua Principal");
        endereco.setNumero("100");
        endereco.setComplemento("Centro de Eventos");
        endereco.setCep("H91 ABC1");
        endereco.setCidade("Galway");
        endereco.setEstado("Galway");

        Evento evento = new Evento();

        evento.setTitulo(titulo);
        evento.setSubTitulo("Evento acadêmico");
        evento.setTipoEvento("Feira");
        evento.setEndereco(endereco);

        evento.setInicioEvento(
                LocalDateTime.of(2026, 8, 10, 9, 0)
        );

        evento.setTerminoEvento(
                LocalDateTime.of(2026, 8, 10, 18, 0)
        );

        return evento;
    }
}
