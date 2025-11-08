package com.example.demo.web.rest;

import static com.example.demo.domain.EventoTipoAsserts.*;
import static com.example.demo.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.demo.IntegrationTest;
import com.example.demo.domain.EventoTipo;
import com.example.demo.repository.EventoTipoRepository;
import com.example.demo.service.dto.EventoTipoDTO;
import com.example.demo.service.mapper.EventoTipoMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link EventoTipoResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class EventoTipoResourceIT {

    private static final String DEFAULT_NOMBRE = "AAAAAAAAAA";
    private static final String UPDATED_NOMBRE = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPCION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPCION = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/evento-tipos";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private EventoTipoRepository eventoTipoRepository;

    @Autowired
    private EventoTipoMapper eventoTipoMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restEventoTipoMockMvc;

    private EventoTipo eventoTipo;

    private EventoTipo insertedEventoTipo;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static EventoTipo createEntity() {
        return new EventoTipo().nombre(DEFAULT_NOMBRE).descripcion(DEFAULT_DESCRIPCION);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static EventoTipo createUpdatedEntity() {
        return new EventoTipo().nombre(UPDATED_NOMBRE).descripcion(UPDATED_DESCRIPCION);
    }

    @BeforeEach
    void initTest() {
        eventoTipo = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedEventoTipo != null) {
            eventoTipoRepository.delete(insertedEventoTipo);
            insertedEventoTipo = null;
        }
    }

    @Test
    @Transactional
    void createEventoTipo() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the EventoTipo
        EventoTipoDTO eventoTipoDTO = eventoTipoMapper.toDto(eventoTipo);
        var returnedEventoTipoDTO = om.readValue(
            restEventoTipoMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(eventoTipoDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            EventoTipoDTO.class
        );

        // Validate the EventoTipo in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedEventoTipo = eventoTipoMapper.toEntity(returnedEventoTipoDTO);
        assertEventoTipoUpdatableFieldsEquals(returnedEventoTipo, getPersistedEventoTipo(returnedEventoTipo));

        insertedEventoTipo = returnedEventoTipo;
    }

    @Test
    @Transactional
    void createEventoTipoWithExistingId() throws Exception {
        // Create the EventoTipo with an existing ID
        eventoTipo.setId(1L);
        EventoTipoDTO eventoTipoDTO = eventoTipoMapper.toDto(eventoTipo);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restEventoTipoMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(eventoTipoDTO)))
            .andExpect(status().isBadRequest());

        // Validate the EventoTipo in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNombreIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        eventoTipo.setNombre(null);

        // Create the EventoTipo, which fails.
        EventoTipoDTO eventoTipoDTO = eventoTipoMapper.toDto(eventoTipo);

        restEventoTipoMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(eventoTipoDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllEventoTipos() throws Exception {
        // Initialize the database
        insertedEventoTipo = eventoTipoRepository.saveAndFlush(eventoTipo);

        // Get all the eventoTipoList
        restEventoTipoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(eventoTipo.getId().intValue())))
            .andExpect(jsonPath("$.[*].nombre").value(hasItem(DEFAULT_NOMBRE)))
            .andExpect(jsonPath("$.[*].descripcion").value(hasItem(DEFAULT_DESCRIPCION)));
    }

    @Test
    @Transactional
    void getEventoTipo() throws Exception {
        // Initialize the database
        insertedEventoTipo = eventoTipoRepository.saveAndFlush(eventoTipo);

        // Get the eventoTipo
        restEventoTipoMockMvc
            .perform(get(ENTITY_API_URL_ID, eventoTipo.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(eventoTipo.getId().intValue()))
            .andExpect(jsonPath("$.nombre").value(DEFAULT_NOMBRE))
            .andExpect(jsonPath("$.descripcion").value(DEFAULT_DESCRIPCION));
    }

    @Test
    @Transactional
    void getNonExistingEventoTipo() throws Exception {
        // Get the eventoTipo
        restEventoTipoMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingEventoTipo() throws Exception {
        // Initialize the database
        insertedEventoTipo = eventoTipoRepository.saveAndFlush(eventoTipo);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the eventoTipo
        EventoTipo updatedEventoTipo = eventoTipoRepository.findById(eventoTipo.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedEventoTipo are not directly saved in db
        em.detach(updatedEventoTipo);
        updatedEventoTipo.nombre(UPDATED_NOMBRE).descripcion(UPDATED_DESCRIPCION);
        EventoTipoDTO eventoTipoDTO = eventoTipoMapper.toDto(updatedEventoTipo);

        restEventoTipoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, eventoTipoDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(eventoTipoDTO))
            )
            .andExpect(status().isOk());

        // Validate the EventoTipo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedEventoTipoToMatchAllProperties(updatedEventoTipo);
    }

    @Test
    @Transactional
    void putNonExistingEventoTipo() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        eventoTipo.setId(longCount.incrementAndGet());

        // Create the EventoTipo
        EventoTipoDTO eventoTipoDTO = eventoTipoMapper.toDto(eventoTipo);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restEventoTipoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, eventoTipoDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(eventoTipoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the EventoTipo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchEventoTipo() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        eventoTipo.setId(longCount.incrementAndGet());

        // Create the EventoTipo
        EventoTipoDTO eventoTipoDTO = eventoTipoMapper.toDto(eventoTipo);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEventoTipoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(eventoTipoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the EventoTipo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamEventoTipo() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        eventoTipo.setId(longCount.incrementAndGet());

        // Create the EventoTipo
        EventoTipoDTO eventoTipoDTO = eventoTipoMapper.toDto(eventoTipo);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEventoTipoMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(eventoTipoDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the EventoTipo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateEventoTipoWithPatch() throws Exception {
        // Initialize the database
        insertedEventoTipo = eventoTipoRepository.saveAndFlush(eventoTipo);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the eventoTipo using partial update
        EventoTipo partialUpdatedEventoTipo = new EventoTipo();
        partialUpdatedEventoTipo.setId(eventoTipo.getId());

        partialUpdatedEventoTipo.nombre(UPDATED_NOMBRE).descripcion(UPDATED_DESCRIPCION);

        restEventoTipoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedEventoTipo.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedEventoTipo))
            )
            .andExpect(status().isOk());

        // Validate the EventoTipo in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertEventoTipoUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedEventoTipo, eventoTipo),
            getPersistedEventoTipo(eventoTipo)
        );
    }

    @Test
    @Transactional
    void fullUpdateEventoTipoWithPatch() throws Exception {
        // Initialize the database
        insertedEventoTipo = eventoTipoRepository.saveAndFlush(eventoTipo);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the eventoTipo using partial update
        EventoTipo partialUpdatedEventoTipo = new EventoTipo();
        partialUpdatedEventoTipo.setId(eventoTipo.getId());

        partialUpdatedEventoTipo.nombre(UPDATED_NOMBRE).descripcion(UPDATED_DESCRIPCION);

        restEventoTipoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedEventoTipo.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedEventoTipo))
            )
            .andExpect(status().isOk());

        // Validate the EventoTipo in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertEventoTipoUpdatableFieldsEquals(partialUpdatedEventoTipo, getPersistedEventoTipo(partialUpdatedEventoTipo));
    }

    @Test
    @Transactional
    void patchNonExistingEventoTipo() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        eventoTipo.setId(longCount.incrementAndGet());

        // Create the EventoTipo
        EventoTipoDTO eventoTipoDTO = eventoTipoMapper.toDto(eventoTipo);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restEventoTipoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, eventoTipoDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(eventoTipoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the EventoTipo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchEventoTipo() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        eventoTipo.setId(longCount.incrementAndGet());

        // Create the EventoTipo
        EventoTipoDTO eventoTipoDTO = eventoTipoMapper.toDto(eventoTipo);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEventoTipoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(eventoTipoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the EventoTipo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamEventoTipo() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        eventoTipo.setId(longCount.incrementAndGet());

        // Create the EventoTipo
        EventoTipoDTO eventoTipoDTO = eventoTipoMapper.toDto(eventoTipo);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEventoTipoMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(eventoTipoDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the EventoTipo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteEventoTipo() throws Exception {
        // Initialize the database
        insertedEventoTipo = eventoTipoRepository.saveAndFlush(eventoTipo);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the eventoTipo
        restEventoTipoMockMvc
            .perform(delete(ENTITY_API_URL_ID, eventoTipo.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return eventoTipoRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected EventoTipo getPersistedEventoTipo(EventoTipo eventoTipo) {
        return eventoTipoRepository.findById(eventoTipo.getId()).orElseThrow();
    }

    protected void assertPersistedEventoTipoToMatchAllProperties(EventoTipo expectedEventoTipo) {
        assertEventoTipoAllPropertiesEquals(expectedEventoTipo, getPersistedEventoTipo(expectedEventoTipo));
    }

    protected void assertPersistedEventoTipoToMatchUpdatableProperties(EventoTipo expectedEventoTipo) {
        assertEventoTipoAllUpdatablePropertiesEquals(expectedEventoTipo, getPersistedEventoTipo(expectedEventoTipo));
    }
}
