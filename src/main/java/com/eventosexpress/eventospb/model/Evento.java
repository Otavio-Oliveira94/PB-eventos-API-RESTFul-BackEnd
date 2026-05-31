package com.eventosexpress.eventospb.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "eventos")
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;
    private String subTitulo;
    private String tipoEvento;

    @Embedded
    private Endereco endereco;

    private LocalDateTime inicioEvento;
    private LocalDateTime terminoEvento;
}
