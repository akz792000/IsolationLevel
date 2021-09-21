package com.isolation.level.repository;

import com.isolation.level.domain.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Ali Karimizandi
 * @since 2021
 */
@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity, Long> {

    Optional<DocumentEntity> findByCode(Long code);

    @Query("select U from DocumentEntity U where U.code >= :start and U.code <= :end")
    List<DocumentEntity> getAllByCodeBetween(Long start, Long end);

}
