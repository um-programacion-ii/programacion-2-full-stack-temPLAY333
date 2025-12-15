package com.example.demo.repository;

import com.example.demo.domain.EventoTipo;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the EventoTipo entity.
 */
@SuppressWarnings("unused")
@Repository
public interface EventoTipoRepository extends JpaRepository<EventoTipo, Long> {
    /**
     * Busca un EventoTipo por nombre (case-insensitive).
     * @param nombre el nombre del tipo de evento
     * @return Optional con el EventoTipo encontrado
     */
    Optional<EventoTipo> findByNombreIgnoreCase(String nombre);
}
