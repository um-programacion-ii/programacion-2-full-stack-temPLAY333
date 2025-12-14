package com.example.demo.repository;

import com.example.demo.domain.Venta;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Venta entity.
 */
@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
    @Query("select venta from Venta venta where venta.usuario.login = ?#{authentication.name}")
    List<Venta> findByUsuarioIsCurrentUser();

    default Optional<Venta> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Venta> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Venta> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(value = "select venta from Venta venta left join fetch venta.usuario", countQuery = "select count(venta) from Venta venta")
    Page<Venta> findAllWithToOneRelationships(Pageable pageable);

    @Query("select venta from Venta venta left join fetch venta.usuario")
    List<Venta> findAllWithToOneRelationships();

    @Query("select venta from Venta venta left join fetch venta.usuario where venta.id =:id")
    Optional<Venta> findOneWithToOneRelationships(@Param("id") Long id);
}
