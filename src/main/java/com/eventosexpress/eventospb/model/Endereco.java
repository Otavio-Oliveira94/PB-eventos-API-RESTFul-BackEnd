package com.eventosexpress.eventospb.model;

import jakarta.persistence.Embeddable;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class Endereco {
    private String rua;
    private String numero;
    private String complemento;
    private String cep;
    private String cidade;
    private String estado;
}
