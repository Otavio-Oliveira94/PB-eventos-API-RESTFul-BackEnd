package com.eventosexpress.eventospb.service;

import com.eventosexpress.eventospb.auditoria.AuditoriaService;
import com.eventosexpress.eventospb.dto.EventoRequestDTO;
import com.eventosexpress.eventospb.dto.EventoResponseDTO;
import com.eventosexpress.eventospb.exception.EventoNaoEncontradoException;
import com.eventosexpress.eventospb.mapper.EventoMapper;
import com.eventosexpress.eventospb.model.Evento;
import com.eventosexpress.eventospb.repository.EventoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EventoService {
    private final EventoRepository eventoRepository;
    private final EventoMapper eventoMapper;
    private final AuditoriaService auditoriaService;

    public EventoService(EventoRepository eventoRepository, EventoMapper eventoMapper, AuditoriaService auditoriaService) {
        this.eventoRepository = eventoRepository;
        this.eventoMapper = eventoMapper;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public EventoResponseDTO criarEvento(EventoRequestDTO dto) {
        Evento evento = eventoMapper.paraEntidade(dto);
        Evento eventoSalvo = eventoRepository.save(evento);

        EventoResponseDTO resposta = eventoMapper.paraResponseDTO(eventoSalvo);
        auditoriaService.registrar("CRIACAO", eventoSalvo.getId(), null, resposta);
        return resposta;
    }

    public List<EventoResponseDTO> buscarTodos() {
        return eventoRepository.findAll()
                .stream()
                .map(eventoMapper::paraResponseDTO)
                .toList();
    }

    public EventoResponseDTO buscarPorId(Long id) {
        Evento evento = buscarEntidadePorId(id);
        return eventoMapper.paraResponseDTO(evento);
    }

    @Transactional
    public EventoResponseDTO editar(Long id, EventoRequestDTO dto) {
        Evento evento = buscarEntidadePorId(id);

        EventoResponseDTO antes = eventoMapper.paraResponseDTO(evento);
        eventoMapper.atualizarEntidade(evento, dto);

        Evento eventoAtualizado = eventoRepository.save(evento);
        EventoResponseDTO depois = eventoMapper.paraResponseDTO(eventoAtualizado);
        auditoriaService.registrar("ATUALIZACAO", id, antes, depois);
        return depois;
    }

    @Transactional
    public void remover(Long id) {
        Evento evento = buscarEntidadePorId(id);
        EventoResponseDTO antes = eventoMapper.paraResponseDTO(evento);
        eventoRepository.delete(evento);
        auditoriaService.registrar("EXCLUSAO", id, antes, null);
    }

    private Evento buscarEntidadePorId(Long id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new EventoNaoEncontradoException(id));
    }
}
