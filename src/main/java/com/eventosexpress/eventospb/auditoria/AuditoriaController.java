package com.eventosexpress.eventospb.auditoria;

import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

import java.util.List;

@RestController
@RequestMapping("/auditoria")
@CrossOrigin(origins = "http://localhost:5173")
public class AuditoriaController {
    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public List<JsonNode> consultar(@RequestParam(required = false) Long registroId,
                                    @RequestParam(required = false) String execucao,
                                    @RequestParam(defaultValue = "100") int limite) {
        return auditoriaService.consultar(registroId, execucao, limite);
    }
}
