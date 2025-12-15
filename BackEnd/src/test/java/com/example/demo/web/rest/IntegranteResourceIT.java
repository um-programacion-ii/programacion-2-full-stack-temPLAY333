package com.example.demo.web.rest;

import static com.example.demo.domain.IntegranteAsserts.*;
import static com.example.demo.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.demo.IntegrationTest;
import com.example.demo.domain.Integrante;
import com.example.demo.repository.IntegranteRepository;
import com.example.demo.service.dto.IntegranteDTO;
import com.example.demo.service.mapper.IntegranteMapper;
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
 * Integration tests for the {@link IntegranteResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class IntegranteResourceIT {

    private static final String DEFAULT_NOMBRE = "AAAAAAAAAA";
    private static final String UPDATED_NOMBRE = "BBBBBBBBBB";

    private static final String DEFAULT_APELLIDO = "AAAAAAAAAA";
    private static final String UPDATED_APELLIDO = "BBBBBBBBBB";

    private static final String DEFAULT_IDENTIFICACION = "AAAAAAAAAA";
    private static final String UPDATED_IDENTIFICACION = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/integrantes";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private IntegranteRepository integranteRepository;

    @Autowired
    private IntegranteMapper integranteMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restIntegranteMockMvc;

    private Integrante integrante;

    private Integrante insertedIntegrante;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Integrante createEntity() {
        return new Integrante().nombre(DEFAULT_NOMBRE).apellido(DEFAULT_APELLIDO).identificacion(DEFAULT_IDENTIFICACION);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Integrante createUpdatedEntity() {
        return new Integrante().nombre(UPDATED_NOMBRE).apellido(UPDATED_APELLIDO).identificacion(UPDATED_IDENTIFICACION);
    }

    @BeforeEach
    void initTest() {
        integrante = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedIntegrante != null) {
            integranteRepository.delete(insertedIntegrante);
            insertedIntegrante = null;
        }
    }

    @Test
    @Transactional
    void createIntegrante() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Integrante
        IntegranteDTO integranteDTO = integranteMapper.toDto(integrante);
        var returnedIntegranteDTO = om.readValue(
            restIntegranteMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(integranteDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            IntegranteDTO.class
        );

        // Validate the Integrante in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedIntegrante = integranteMapper.toEntity(returnedIntegranteDTO);
        assertIntegranteUpdatableFieldsEquals(returnedIntegrante, getPersistedIntegrante(returnedIntegrante));

        insertedIntegrante = returnedIntegrante;
    }

    @Test
    @Transactional
    void createIntegranteWithExistingId() throws Exception {
        // Create the Integrante with an existing ID
        integrante.setId(1L);
        IntegranteDTO integranteDTO = integranteMapper.toDto(integrante);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restIntegranteMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(integranteDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Integrante in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNombreIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        integrante.setNombre(null);

        // Create the Integrante, which fails.
        IntegranteDTO integranteDTO = integranteMapper.toDto(integrante);

        restIntegranteMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(integranteDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkApellidoIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        integrante.setApellido(null);

        // Create the Integrante, which fails.
        IntegranteDTO integranteDTO = integranteMapper.toDto(integrante);

        restIntegranteMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(integranteDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkIdentificacionIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        integrante.setIdentificacion(null);

        // Create the Integrante, which fails.
        IntegranteDTO integranteDTO = integranteMapper.toDto(integrante);

        restIntegranteMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(integranteDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllIntegrantes() throws Exception {
        // Initialize the database
        insertedIntegrante = integranteRepository.saveAndFlush(integrante);

        // Get all the integranteList
        restIntegranteMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(integrante.getId().intValue())))
            .andExpect(jsonPath("$.[*].nombre").value(hasItem(DEFAULT_NOMBRE)))
            .andExpect(jsonPath("$.[*].apellido").value(hasItem(DEFAULT_APELLIDO)))
            .andExpect(jsonPath("$.[*].identificacion").value(hasItem(DEFAULT_IDENTIFICACION)));
    }

    @Test
    @Transactional
    void getIntegrante() throws Exception {
        // Initialize the database
        insertedIntegrante = integranteRepository.saveAndFlush(integrante);

        // Get the integrante
        restIntegranteMockMvc
            .perform(get(ENTITY_API_URL_ID, integrante.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(integrante.getId().intValue()))
            .andExpect(jsonPath("$.nombre").value(DEFAULT_NOMBRE))
            .andExpect(jsonPath("$.apellido").value(DEFAULT_APELLIDO))
            .andExpect(jsonPath("$.identificacion").value(DEFAULT_IDENTIFICACION));
    }

    @Test
    @Transactional
    void getNonExistingIntegrante() throws Exception {
        // Get the integrante
        restIntegranteMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingIntegrante() throws Exception {
        // Initialize the database
        insertedIntegrante = integranteRepository.saveAndFlush(integrante);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the integrante
        Integrante updatedIntegrante = integranteRepository.findById(integrante.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedIntegrante are not directly saved in db
        em.detach(updatedIntegrante);
        updatedIntegrante.nombre(UPDATED_NOMBRE).apellido(UPDATED_APELLIDO).identificacion(UPDATED_IDENTIFICACION);
        IntegranteDTO integranteDTO = integranteMapper.toDto(updatedIntegrante);

        restIntegranteMockMvc
            .perform(
                put(ENTITY_API_URL_ID, integranteDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(integranteDTO))
            )
            .andExpect(status().isOk());

        // Validate the Integrante in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedIntegranteToMatchAllProperties(updatedIntegrante);
    }

    @Test
    @Transactional
    void putNonExistingIntegrante() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        integrante.setId(longCount.incrementAndGet());

        // Create the Integrante
        IntegranteDTO integranteDTO = integranteMapper.toDto(integrante);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restIntegranteMockMvc
            .perform(
                put(ENTITY_API_URL_ID, integranteDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(integranteDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Integrante in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchIntegrante() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        integrante.setId(longCount.incrementAndGet());

        // Create the Integrante
        IntegranteDTO integranteDTO = integranteMapper.toDto(integrante);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restIntegranteMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(integranteDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Integrante in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamIntegrante() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        integrante.setId(longCount.incrementAndGet());

        // Create the Integrante
        IntegranteDTO integranteDTO = integranteMapper.toDto(integrante);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restIntegranteMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(integranteDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Integrante in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateIntegranteWithPatch() throws Exception {
        // Initialize the database
        insertedIntegrante = integranteRepository.saveAndFlush(integrante);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the integrante using partial update
        Integrante partialUpdatedIntegrante = new Integrante();
        partialUpdatedIntegrante.setId(integrante.getId());

        partialUpdatedIntegrante.apellido(UPDATED_APELLIDO);

        restIntegranteMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedIntegrante.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedIntegrante))
            )
            .andExpect(status().isOk());

        // Validate the Integrante in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertIntegranteUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedIntegrante, integrante),
            getPersistedIntegrante(integrante)
        );
    }

    @Test
    @Transactional
    void fullUpdateIntegranteWithPatch() throws Exception {
        // Initialize the database
        insertedIntegrante = integranteRepository.saveAndFlush(integrante);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the integrante using partial update
        Integrante partialUpdatedIntegrante = new Integrante();
        partialUpdatedIntegrante.setId(integrante.getId());

        partialUpdatedIntegrante.nombre(UPDATED_NOMBRE).apellido(UPDATED_APELLIDO).identificacion(UPDATED_IDENTIFICACION);

        restIntegranteMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedIntegrante.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedIntegrante))
            )
            .andExpect(status().isOk());

        // Validate the Integrante in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertIntegranteUpdatableFieldsEquals(partialUpdatedIntegrante, getPersistedIntegrante(partialUpdatedIntegrante));
    }

    @Test
    @Transactional
    void patchNonExistingIntegrante() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        integrante.setId(longCount.incrementAndGet());

        // Create the Integrante
        IntegranteDTO integranteDTO = integranteMapper.toDto(integrante);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restIntegranteMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, integranteDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(integranteDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Integrante in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchIntegrante() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        integrante.setId(longCount.incrementAndGet());

        // Create the Integrante
        IntegranteDTO integranteDTO = integranteMapper.toDto(integrante);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restIntegranteMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(integranteDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Integrante in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamIntegrante() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        integrante.setId(longCount.incrementAndGet());

        // Create the Integrante
        IntegranteDTO integranteDTO = integranteMapper.toDto(integrante);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restIntegranteMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(integranteDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Integrante in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteIntegrante() throws Exception {
        // Initialize the database
        insertedIntegrante = integranteRepository.saveAndFlush(integrante);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the integrante
        restIntegranteMockMvc
            .perform(delete(ENTITY_API_URL_ID, integrante.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return integranteRepository.count();
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

    protected Integrante getPersistedIntegrante(Integrante integrante) {
        return integranteRepository.findById(integrante.getId()).orElseThrow();
    }

    protected void assertPersistedIntegranteToMatchAllProperties(Integrante expectedIntegrante) {
        assertIntegranteAllPropertiesEquals(expectedIntegrante, getPersistedIntegrante(expectedIntegrante));
    }

    protected void assertPersistedIntegranteToMatchUpdatableProperties(Integrante expectedIntegrante) {
        assertIntegranteAllUpdatablePropertiesEquals(expectedIntegrante, getPersistedIntegrante(expectedIntegrante));
    }
}
