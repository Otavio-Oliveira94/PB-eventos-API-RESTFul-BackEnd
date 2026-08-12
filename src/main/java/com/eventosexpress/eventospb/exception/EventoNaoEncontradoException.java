package com.eventosexpress.eventospb.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class EventoNaoEncontradoException extends RuntimeException {
    public EventoNaoEncontradoException(Long id) {
        super("Evento não encontrado para o ID: " + id);
    }
}
