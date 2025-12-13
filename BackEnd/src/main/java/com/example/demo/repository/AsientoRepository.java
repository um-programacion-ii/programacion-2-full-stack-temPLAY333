package com.example.demo.repository;

import com.example.demo.domain.Asiento;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Asiento entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AsientoRepository extends JpaRepository<Asiento, Long> {}
