package com.isolation.level.service;

import com.isolation.level.domain.DocumentEntity;
import com.isolation.level.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * @author Ali Karimizandi
 * @since 2021
 */
@RequiredArgsConstructor
@Service
public class DocumentService {

    private final DocumentRepository repository;

    @Transactional(isolation = Isolation.READ_UNCOMMITTED, readOnly = true)
    public Optional<DocumentEntity> findById(Long id) {
        return repository.findById(id);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public DocumentEntity save(DocumentEntity entity) {
        return repository.save(entity);
    }

}
