package com.example.demo.repository;

import com.example.demo.domain.Evento;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

/**
 * Utility repository to load bag relationships based on https://vladmihalcea.com/hibernate-multiplebagfetchexception/
 */
public class EventoRepositoryWithBagRelationshipsImpl implements EventoRepositoryWithBagRelationships {

    private static final String ID_PARAMETER = "id";
    private static final String EVENTOS_PARAMETER = "eventos";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Evento> fetchBagRelationships(Optional<Evento> evento) {
        return evento.map(this::fetchIntegrantes);
    }

    @Override
    public Page<Evento> fetchBagRelationships(Page<Evento> eventos) {
        return new PageImpl<>(fetchBagRelationships(eventos.getContent()), eventos.getPageable(), eventos.getTotalElements());
    }

    @Override
    public List<Evento> fetchBagRelationships(List<Evento> eventos) {
        return Optional.of(eventos).map(this::fetchIntegrantes).orElse(Collections.emptyList());
    }

    Evento fetchIntegrantes(Evento result) {
        return entityManager
            .createQuery("select evento from Evento evento left join fetch evento.integrantes where evento.id = :id", Evento.class)
            .setParameter(ID_PARAMETER, result.getId())
            .getSingleResult();
    }

    List<Evento> fetchIntegrantes(List<Evento> eventos) {
        HashMap<Object, Integer> order = new HashMap<>();
        IntStream.range(0, eventos.size()).forEach(index -> order.put(eventos.get(index).getId(), index));
        List<Evento> result = entityManager
            .createQuery("select evento from Evento evento left join fetch evento.integrantes where evento in :eventos", Evento.class)
            .setParameter(EVENTOS_PARAMETER, eventos)
            .getResultList();
        Collections.sort(result, (o1, o2) -> Integer.compare(order.get(o1.getId()), order.get(o2.getId())));
        return result;
    }
}
