/*-
 * ---license-start
 * Corona-Warn-App / cwa-quick-test-backend
 * ---
 * Copyright (C) 2021 T-Systems International GmbH and all other contributors
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
import lombok.RequiredArgsConstructor;

// see app.coronawarn.quicktest.config.ArchiveEntityManagerConfig
@RequiredArgsConstructor
public class ArchiveRepository {

    private final EntityManager em;

    /**
     * Saves the entry.
     * 
     * @param archive {@link Archive}
     * @return {@link Archive}
     */
    public Archive save(Archive archive) {
        this.em.getTransaction().begin();
        this.em.persist(archive);
        this.em.getTransaction().commit();
        return archive;
    }

    /**
     * Returns all existing entries.
     * 
     * @return {@link List} of {@link Archive}
     */
    public List<Archive> findAll() {
        this.em.getTransaction().begin();
        final List<Archive> result = this.em.createQuery("SELECT a FROM Archive a", Archive.class).getResultList();
        this.em.getTransaction().commit();
        return result;
    }
}
