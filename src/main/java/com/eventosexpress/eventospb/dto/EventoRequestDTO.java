package com.eventosexpress.eventospb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventoRequestDTO {
    private String titulo;
    private String subTitulo;
    private String tipoEvento;
    private EnderecoDTO endereco;
    private LocalDateTime inicioEvento;
    private LocalDateTime terminoEvento;
}
