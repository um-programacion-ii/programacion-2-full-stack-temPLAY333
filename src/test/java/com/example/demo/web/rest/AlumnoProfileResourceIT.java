package com.example.demo.web.rest;

import static com.example.demo.domain.AlumnoProfileAsserts.*;
import static com.example.demo.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.demo.IntegrationTest;
import com.example.demo.domain.AlumnoProfile;
import com.example.demo.domain.User;
import com.example.demo.repository.AlumnoProfileRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AlumnoProfileService;
import com.example.demo.service.dto.AlumnoProfileDTO;
import com.example.demo.service.mapper.AlumnoProfileMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link AlumnoProfileResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class AlumnoProfileResourceIT {

    private static final String DEFAULT_NOMBRE_ALUMNO = "AAAAAAAAAA";
    private static final String UPDATED_NOMBRE_ALUMNO = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPCION_PROYECTO = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPCION_PROYECTO = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/alumno-profiles";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private AlumnoProfileRepository alumnoProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private AlumnoProfileRepository alumnoProfileRepositoryMock;

    @Autowired
    private AlumnoProfileMapper alumnoProfileMapper;

    @Mock
    private AlumnoProfileService alumnoProfileServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restAlumnoProfileMockMvc;

    private AlumnoProfile alumnoProfile;

    private AlumnoProfile insertedAlumnoProfile;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AlumnoProfile createEntity(EntityManager em) {
        AlumnoProfile alumnoProfile = new AlumnoProfile()
            .nombreAlumno(DEFAULT_NOMBRE_ALUMNO)
            .descripcionProyecto(DEFAULT_DESCRIPCION_PROYECTO);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        alumnoProfile.setUser(user);
        return alumnoProfile;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AlumnoProfile createUpdatedEntity(EntityManager em) {
        AlumnoProfile updatedAlumnoProfile = new AlumnoProfile()
            .nombreAlumno(UPDATED_NOMBRE_ALUMNO)
            .descripcionProyecto(UPDATED_DESCRIPCION_PROYECTO);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        updatedAlumnoProfile.setUser(user);
        return updatedAlumnoProfile;
    }

    @BeforeEach
    void initTest() {
        alumnoProfile = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedAlumnoProfile != null) {
            alumnoProfileRepository.delete(insertedAlumnoProfile);
            insertedAlumnoProfile = null;
        }
    }

    @Test
    @Transactional
    void createAlumnoProfile() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the AlumnoProfile
        AlumnoProfileDTO alumnoProfileDTO = alumnoProfileMapper.toDto(alumnoProfile);
        var returnedAlumnoProfileDTO = om.readValue(
            restAlumnoProfileMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(alumnoProfileDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            AlumnoProfileDTO.class
        );

        // Validate the AlumnoProfile in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedAlumnoProfile = alumnoProfileMapper.toEntity(returnedAlumnoProfileDTO);
        assertAlumnoProfileUpdatableFieldsEquals(returnedAlumnoProfile, getPersistedAlumnoProfile(returnedAlumnoProfile));

        insertedAlumnoProfile = returnedAlumnoProfile;
    }

    @Test
    @Transactional
    void createAlumnoProfileWithExistingId() throws Exception {
        // Create the AlumnoProfile with an existing ID
        alumnoProfile.setId(1L);
        AlumnoProfileDTO alumnoProfileDTO = alumnoProfileMapper.toDto(alumnoProfile);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restAlumnoProfileMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(alumnoProfileDTO)))
            .andExpect(status().isBadRequest());

        // Validate the AlumnoProfile in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNombreAlumnoIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        alumnoProfile.setNombreAlumno(null);

        // Create the AlumnoProfile, which fails.
        AlumnoProfileDTO alumnoProfileDTO = alumnoProfileMapper.toDto(alumnoProfile);

        restAlumnoProfileMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(alumnoProfileDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllAlumnoProfiles() throws Exception {
        // Initialize the database
        insertedAlumnoProfile = alumnoProfileRepository.saveAndFlush(alumnoProfile);

        // Get all the alumnoProfileList
        restAlumnoProfileMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(alumnoProfile.getId().intValue())))
            .andExpect(jsonPath("$.[*].nombreAlumno").value(hasItem(DEFAULT_NOMBRE_ALUMNO)))
            .andExpect(jsonPath("$.[*].descripcionProyecto").value(hasItem(DEFAULT_DESCRIPCION_PROYECTO)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllAlumnoProfilesWithEagerRelationshipsIsEnabled() throws Exception {
        when(alumnoProfileServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restAlumnoProfileMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(alumnoProfileServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllAlumnoProfilesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(alumnoProfileServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restAlumnoProfileMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(alumnoProfileRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getAlumnoProfile() throws Exception {
        // Initialize the database
        insertedAlumnoProfile = alumnoProfileRepository.saveAndFlush(alumnoProfile);

        // Get the alumnoProfile
        restAlumnoProfileMockMvc
            .perform(get(ENTITY_API_URL_ID, alumnoProfile.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(alumnoProfile.getId().intValue()))
            .andExpect(jsonPath("$.nombreAlumno").value(DEFAULT_NOMBRE_ALUMNO))
            .andExpect(jsonPath("$.descripcionProyecto").value(DEFAULT_DESCRIPCION_PROYECTO));
    }

    @Test
    @Transactional
    void getNonExistingAlumnoProfile() throws Exception {
        // Get the alumnoProfile
        restAlumnoProfileMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingAlumnoProfile() throws Exception {
        // Initialize the database
        insertedAlumnoProfile = alumnoProfileRepository.saveAndFlush(alumnoProfile);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the alumnoProfile
        AlumnoProfile updatedAlumnoProfile = alumnoProfileRepository.findById(alumnoProfile.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedAlumnoProfile are not directly saved in db
        em.detach(updatedAlumnoProfile);
        updatedAlumnoProfile.nombreAlumno(UPDATED_NOMBRE_ALUMNO).descripcionProyecto(UPDATED_DESCRIPCION_PROYECTO);
        AlumnoProfileDTO alumnoProfileDTO = alumnoProfileMapper.toDto(updatedAlumnoProfile);

        restAlumnoProfileMockMvc
            .perform(
                put(ENTITY_API_URL_ID, alumnoProfileDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(alumnoProfileDTO))
            )
            .andExpect(status().isOk());

        // Validate the AlumnoProfile in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedAlumnoProfileToMatchAllProperties(updatedAlumnoProfile);
    }

    @Test
    @Transactional
    void putNonExistingAlumnoProfile() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        alumnoProfile.setId(longCount.incrementAndGet());

        // Create the AlumnoProfile
        AlumnoProfileDTO alumnoProfileDTO = alumnoProfileMapper.toDto(alumnoProfile);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAlumnoProfileMockMvc
            .perform(
                put(ENTITY_API_URL_ID, alumnoProfileDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(alumnoProfileDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AlumnoProfile in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchAlumnoProfile() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        alumnoProfile.setId(longCount.incrementAndGet());

        // Create the AlumnoProfile
        AlumnoProfileDTO alumnoProfileDTO = alumnoProfileMapper.toDto(alumnoProfile);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlumnoProfileMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(alumnoProfileDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AlumnoProfile in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamAlumnoProfile() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        alumnoProfile.setId(longCount.incrementAndGet());

        // Create the AlumnoProfile
        AlumnoProfileDTO alumnoProfileDTO = alumnoProfileMapper.toDto(alumnoProfile);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlumnoProfileMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(alumnoProfileDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the AlumnoProfile in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateAlumnoProfileWithPatch() throws Exception {
        // Initialize the database
        insertedAlumnoProfile = alumnoProfileRepository.saveAndFlush(alumnoProfile);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the alumnoProfile using partial update
        AlumnoProfile partialUpdatedAlumnoProfile = new AlumnoProfile();
        partialUpdatedAlumnoProfile.setId(alumnoProfile.getId());

        partialUpdatedAlumnoProfile.descripcionProyecto(UPDATED_DESCRIPCION_PROYECTO);

        restAlumnoProfileMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAlumnoProfile.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAlumnoProfile))
            )
            .andExpect(status().isOk());

        // Validate the AlumnoProfile in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAlumnoProfileUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedAlumnoProfile, alumnoProfile),
            getPersistedAlumnoProfile(alumnoProfile)
        );
    }

    @Test
    @Transactional
    void fullUpdateAlumnoProfileWithPatch() throws Exception {
        // Initialize the database
        insertedAlumnoProfile = alumnoProfileRepository.saveAndFlush(alumnoProfile);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the alumnoProfile using partial update
        AlumnoProfile partialUpdatedAlumnoProfile = new AlumnoProfile();
        partialUpdatedAlumnoProfile.setId(alumnoProfile.getId());

        partialUpdatedAlumnoProfile.nombreAlumno(UPDATED_NOMBRE_ALUMNO).descripcionProyecto(UPDATED_DESCRIPCION_PROYECTO);

        restAlumnoProfileMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAlumnoProfile.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAlumnoProfile))
            )
            .andExpect(status().isOk());

        // Validate the AlumnoProfile in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAlumnoProfileUpdatableFieldsEquals(partialUpdatedAlumnoProfile, getPersistedAlumnoProfile(partialUpdatedAlumnoProfile));
    }

    @Test
    @Transactional
    void patchNonExistingAlumnoProfile() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        alumnoProfile.setId(longCount.incrementAndGet());

        // Create the AlumnoProfile
        AlumnoProfileDTO alumnoProfileDTO = alumnoProfileMapper.toDto(alumnoProfile);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAlumnoProfileMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, alumnoProfileDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(alumnoProfileDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AlumnoProfile in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchAlumnoProfile() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        alumnoProfile.setId(longCount.incrementAndGet());

        // Create the AlumnoProfile
        AlumnoProfileDTO alumnoProfileDTO = alumnoProfileMapper.toDto(alumnoProfile);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlumnoProfileMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(alumnoProfileDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AlumnoProfile in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamAlumnoProfile() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        alumnoProfile.setId(longCount.incrementAndGet());

        // Create the AlumnoProfile
        AlumnoProfileDTO alumnoProfileDTO = alumnoProfileMapper.toDto(alumnoProfile);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlumnoProfileMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(alumnoProfileDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the AlumnoProfile in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteAlumnoProfile() throws Exception {
        // Initialize the database
        insertedAlumnoProfile = alumnoProfileRepository.saveAndFlush(alumnoProfile);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the alumnoProfile
        restAlumnoProfileMockMvc
            .perform(delete(ENTITY_API_URL_ID, alumnoProfile.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return alumnoProfileRepository.count();
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

    protected AlumnoProfile getPersistedAlumnoProfile(AlumnoProfile alumnoProfile) {
        return alumnoProfileRepository.findById(alumnoProfile.getId()).orElseThrow();
    }

    protected void assertPersistedAlumnoProfileToMatchAllProperties(AlumnoProfile expectedAlumnoProfile) {
        assertAlumnoProfileAllPropertiesEquals(expectedAlumnoProfile, getPersistedAlumnoProfile(expectedAlumnoProfile));
    }

    protected void assertPersistedAlumnoProfileToMatchUpdatableProperties(AlumnoProfile expectedAlumnoProfile) {
        assertAlumnoProfileAllUpdatablePropertiesEquals(expectedAlumnoProfile, getPersistedAlumnoProfile(expectedAlumnoProfile));
    }
}
