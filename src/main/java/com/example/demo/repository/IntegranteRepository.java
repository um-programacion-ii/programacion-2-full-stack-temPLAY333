package com.example.demo.repository;

import com.example.demo.domain.Integrante;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Integrante entity.
 */
@SuppressWarnings("unused")
@Repository
public interface IntegranteRepository extends JpaRepository<Integrante, Long> {}
