package com.eventosexpress.eventospb.controller;

import com.eventosexpress.eventospb.model.Evento;
import com.eventosexpress.eventospb.service.EventoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/eventos")
@CrossOrigin(origins = "http://localhost:5173")
public class EventoController {

    private final EventoService service;

    public EventoController(EventoService service) {
        this.service = service;
    }

    @PostMapping
    public Evento criar(@RequestBody Evento evento) {
        return service.criarEvento(evento);
    }

    @GetMapping
    public List<Evento> exibirTodos(){
        return service.buscarTodos();
    }

    @GetMapping("/{id}")
    public Evento exibirPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public Evento editar(@PathVariable Long id, @RequestBody Evento evento) {
        return service.editar(id, evento);
    }

    @DeleteMapping("/{id}")
    public void remover(@PathVariable Long id) {
        service.remover(id);
    }
}
