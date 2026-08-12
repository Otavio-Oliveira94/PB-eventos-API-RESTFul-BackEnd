package com.eventosexpress.eventospb.service;

import com.eventosexpress.eventospb.dto.EventoRequestDTO;
import com.eventosexpress.eventospb.dto.EventoResponseDTO;
import com.eventosexpress.eventospb.exception.EventoNaoEncontradoException;
import com.eventosexpress.eventospb.mapper.EventoMapper;
import com.eventosexpress.eventospb.model.Evento;
import com.eventosexpress.eventospb.repository.EventoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventoService {
    private final EventoRepository eventoRepository;
    private final EventoMapper eventoMapper;

    public EventoService(EventoRepository eventoRepository, EventoMapper eventoMapper) {
        this.eventoRepository = eventoRepository;
        this.eventoMapper = eventoMapper;
    }

    public EventoResponseDTO criarEvento(EventoRequestDTO dto) {
        Evento evento = eventoMapper.paraEntidade(dto);
        Evento eventoSalvo = eventoRepository.save(evento);

        return eventoMapper.paraResponseDTO(eventoSalvo);
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

    public EventoResponseDTO editar(
            Long id,
            EventoRequestDTO dto
    ) {
        Evento evento = buscarEntidadePorId(id);

        eventoMapper.atualizarEntidade(evento, dto);

        Evento eventoAtualizado = eventoRepository.save(evento);
        return eventoMapper.paraResponseDTO(eventoAtualizado);
    }

    public void remover(Long id) {
        Evento evento = buscarEntidadePorId(id);
        eventoRepository.delete(evento);
    }

    private Evento buscarEntidadePorId(Long id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new EventoNaoEncontradoException(id));
    }
}
