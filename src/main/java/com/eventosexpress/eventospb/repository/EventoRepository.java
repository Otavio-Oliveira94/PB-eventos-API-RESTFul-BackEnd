package com.eventosexpress.eventospb.repository;

import com.eventosexpress.eventospb.model.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {
}
