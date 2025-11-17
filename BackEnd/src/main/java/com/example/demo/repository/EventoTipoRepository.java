package com.example.demo.repository;

import com.example.demo.domain.EventoTipo;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the EventoTipo entity.
 */
@SuppressWarnings("unused")
@Repository
public interface EventoTipoRepository extends JpaRepository<EventoTipo, Long> {}
