package com.isolation.level.service;

import com.isolation.level.domain.DocumentEntity;
import com.isolation.level.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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

    @Transactional(readOnly = true)
    public Optional<DocumentEntity> findById(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public DocumentEntity save(DocumentEntity entity) {
        return repository.save(entity);
    }

}
