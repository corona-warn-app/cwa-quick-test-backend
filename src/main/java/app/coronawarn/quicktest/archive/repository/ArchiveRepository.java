/*-
 * ---license-start
 * Corona-Warn-App / cwa-quick-test-backend
 * ---
 * Copyright (C) 2021 - 2023 T-Systems International GmbH and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package app.coronawarn.quicktest.archive.repository;

import app.coronawarn.quicktest.archive.domain.Archive;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.RollbackException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.data.domain.PageRequest;

// see app.coronawarn.quicktest.config.ArchiveEntityManagerConfig
@RequiredArgsConstructor
@Slf4j
public class ArchiveRepository {

    private final EntityManager em;

    /**
     * Saves the entry.
     *
     * @param archive {@link Archive}
     * @return {@link Archive}
     */
    public Archive save(Archive archive) {
        try {
            this.em.getTransaction().begin();
            this.em.persist(archive);
            this.em.getTransaction().commit();
        } catch (final RollbackException r) {
            log.warn("Rollback exception occured: {}", r.getMessage());
            // resume on error in case of a constraint violation (already present)
            if (r.getCause() instanceof ConstraintViolationException) {
                log.warn("Constraint violation, value already present in longterm db.");
            }
        }
        return archive;
    }

    /**
     * Returns all existing entries.
     *
     * @return {@link List} of {@link Archive}
     */
    public List<Archive> findAll() {
        this.em.getTransaction().begin();

        final List<Archive> result = em
            .createQuery("SELECT a FROM Archive a", Archive.class)
            .getResultList();

        this.em.getTransaction().commit();
        return result;
    }

    /**
     * Returns all entries by pocId.
     *
     * @return {@link List} of {@link Archive}
     */
    public List<Archive> findAllByPocId(final String pocId, final String tenantId) {
        em.getTransaction().begin();

        final List<Archive> result = em
            .createQuery("SELECT a FROM Archive a WHERE a.pocId = ?1 AND a.tenantId = ?2", Archive.class)
            .setParameter(1, pocId)
            .setParameter(2, tenantId)
            .getResultList();

        em.getTransaction().commit();
        return result;
    }

    /**
     * Returns all entries by tenantId.
     *
     * @return {@link List} of {@link Archive}
     */
    public List<Archive> findAllByTenantId(final String tenantId) {
        em.getTransaction().begin();

        final List<Archive> result = em
            .createQuery("SELECT a FROM Archive a WHERE a.tenantId = ?1", Archive.class)
            .setParameter(1, tenantId).getResultList();

        em.getTransaction().commit();
        return result;
    }

    /**
     * Returns all entries by tenantId.
     *
     * @return {@link List} of {@link Archive}
     */
    public List<Archive> findAllByTenantId(final String tenantId, PageRequest pageRequest) {
        em.getTransaction().begin();

        final List<Archive> result = em
            .createQuery("SELECT a FROM Archive a WHERE a.tenantId = ?1", Archive.class)
            .setFirstResult(pageRequest.getPageNumber() * pageRequest.getPageSize())
            .setMaxResults(pageRequest.getPageSize())
            .setParameter(1, tenantId)
            .getResultList();

        em.getTransaction().commit();
        return result;
    }

    /**
     * Delete all entries by tenantId.
     */
    public void deleteAllByTenantId(final String tenantId) {
        em.getTransaction().begin();

        em
            .createQuery("DELETE FROM Archive a WHERE a.tenantId = ?1")
            .setParameter(1, tenantId)
            .executeUpdate();

        em.getTransaction().commit();
    }

    /**
     * Returns all matching hashed guids of existing entities.
     *
     * @return {@link List} of {@link String}
     */
    public List<String> findAllHashedGuids(final List<String> search) {
        this.em.getTransaction().begin();

        final List<String> result = em
            .createQuery("SELECT a.hashedGuid FROM Archive a WHERE a.hashedGuid IN ?1", String.class)
            .setParameter(1, search)
            .getResultList();

        this.em.getTransaction().commit();
        return result;
    }

    /**
     * Count entities by tenantId.
     *
     * @param tenantId SHA256 Hash of TenantId to search for
     * @return amount of found entities.
     */
    public Integer countAllByTenantId(final String tenantId) {
        em.getTransaction().begin();

        final Integer result = em
            .createQuery("SELECT COUNT(*) FROM Archive a WHERE a.tenantId = ?1", Long.class)
            .setParameter(1, tenantId)
            .getSingleResult()
            .intValue();

        em.getTransaction().commit();
        return result;
    }
}
