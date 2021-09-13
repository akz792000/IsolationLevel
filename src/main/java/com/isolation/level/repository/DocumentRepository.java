package com.isolation.level.repository;

import com.isolation.level.domain.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Ali Karimizandi
 * @since 2021
 */
@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity, Long> {

}
