package com.eventosexpress.eventospb.controller;

import com.eventosexpress.eventospb.dto.EventoRequestDTO;
import com.eventosexpress.eventospb.dto.EventoResponseDTO;
import com.eventosexpress.eventospb.service.EventoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<EventoResponseDTO> criarEvento(@RequestBody EventoRequestDTO dto) {
        EventoResponseDTO eventoCriado = service.criarEvento(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(eventoCriado);
    }

    @GetMapping
    public ResponseEntity<List<EventoResponseDTO>> exibirTodos() {
        return ResponseEntity.ok(service.buscarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventoResponseDTO> editar(@PathVariable Long id,@RequestBody EventoRequestDTO dto) {
        return ResponseEntity.ok(service.editar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
