package com.example.demo.repository;

import com.example.demo.domain.AlumnoProfile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the AlumnoProfile entity.
 */
@Repository
public interface AlumnoProfileRepository extends JpaRepository<AlumnoProfile, Long> {
    default Optional<AlumnoProfile> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<AlumnoProfile> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<AlumnoProfile> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select alumnoProfile from AlumnoProfile alumnoProfile left join fetch alumnoProfile.user",
        countQuery = "select count(alumnoProfile) from AlumnoProfile alumnoProfile"
    )
    Page<AlumnoProfile> findAllWithToOneRelationships(Pageable pageable);

    @Query("select alumnoProfile from AlumnoProfile alumnoProfile left join fetch alumnoProfile.user")
    List<AlumnoProfile> findAllWithToOneRelationships();

    @Query("select alumnoProfile from AlumnoProfile alumnoProfile left join fetch alumnoProfile.user where alumnoProfile.id =:id")
    Optional<AlumnoProfile> findOneWithToOneRelationships(@Param("id") Long id);
}
