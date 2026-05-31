package com.eventosexpress.eventospb.service;

import com.eventosexpress.eventospb.model.Evento;
import com.eventosexpress.eventospb.repository.EventoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventoService {
    private final EventoRepository eventoRepository;

    public EventoService(EventoRepository eventoRepository) {
        this.eventoRepository = eventoRepository;
    }

    public Evento criarEvento(Evento evento) {
        return eventoRepository.save(evento);
    }

    public List<Evento> buscarTodos() {
        return eventoRepository.findAll();
    }

    public Evento buscarPorId(Long id) {
        return eventoRepository.findById(id).orElseThrow(()-> new RuntimeException("Evento não encontrado."));
    }

    public Evento editar(Long id, Evento eventoEditado) {
        Evento evento = buscarPorId(id);

        evento.setTitulo(eventoEditado.getTitulo());
        evento.setSubTitulo(eventoEditado.getSubTitulo());
        evento.setTipoEvento(eventoEditado.getTipoEvento());

        evento.setEndereco(eventoEditado.getEndereco());

        evento.setInicioEvento(eventoEditado.getInicioEvento());
        evento.setTerminoEvento(eventoEditado.getTerminoEvento());

        return eventoRepository.save(evento);
    }

    public void remover(Long id) {
        buscarPorId(id);
        eventoRepository.deleteById(id);
    }
}
