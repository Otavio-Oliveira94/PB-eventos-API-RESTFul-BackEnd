package com.eventosexpress.eventospb.controller;

import com.eventosexpress.eventospb.exception.EventoNaoEncontradoException;
import com.eventosexpress.eventospb.service.EventoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventoController.class)
public class EventoControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventoService eventoService;

    @Test
    @DisplayName("Deve retornar 404 quando o evento não existir")
    void deveRetornar404QuandoEventoNaoExistir()
            throws Exception {
        when(eventoService.buscarPorId(999L))
                .thenThrow(
                        new EventoNaoEncontradoException(
                                999L
                        )
                );

        mockMvc.perform(get("/eventos/999"))
                .andExpect(status().isNotFound());
    }
}
