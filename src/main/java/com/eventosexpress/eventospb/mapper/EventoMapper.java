package com.eventosexpress.eventospb.mapper;

import com.eventosexpress.eventospb.dto.EnderecoDTO;
import com.eventosexpress.eventospb.dto.EventoRequestDTO;
import com.eventosexpress.eventospb.dto.EventoResponseDTO;
import com.eventosexpress.eventospb.model.Endereco;
import com.eventosexpress.eventospb.model.Evento;
import org.springframework.stereotype.Component;

@Component
public class EventoMapper {

    public Evento paraEntidade(EventoRequestDTO dto) {
        Evento evento = new Evento();
        atualizarEntidade(evento, dto);
        return evento;
    }

    public void atualizarEntidade(
            Evento evento,
            EventoRequestDTO dto
    ) {
        evento.setTitulo(dto.getTitulo());
        evento.setSubTitulo(dto.getSubTitulo());
        evento.setTipoEvento(dto.getTipoEvento());
        evento.setEndereco(paraEndereco(dto.getEndereco()));
        evento.setInicioEvento(dto.getInicioEvento());
        evento.setTerminoEvento(dto.getTerminoEvento());
    }

    public EventoResponseDTO paraResponseDTO(Evento evento) {
        return new EventoResponseDTO(
                evento.getId(),
                evento.getTitulo(),
                evento.getSubTitulo(),
                evento.getTipoEvento(),
                paraEnderecoDTO(evento.getEndereco()),
                evento.getInicioEvento(),
                evento.getTerminoEvento()
        );
    }

    private Endereco paraEndereco(EnderecoDTO dto) {
        if (dto == null) {
            return null;
        }

        return new Endereco(
                dto.getRua(),
                dto.getNumero(),
                dto.getComplemento(),
                dto.getCep(),
                dto.getCidade(),
                dto.getEstado()
        );
    }

    private EnderecoDTO paraEnderecoDTO(Endereco endereco) {
        if (endereco == null) {
            return null;
        }

        return new EnderecoDTO(
                endereco.getRua(),
                endereco.getNumero(),
                endereco.getComplemento(),
                endereco.getCep(),
                endereco.getCidade(),
                endereco.getEstado()
        );
    }
}
