package com.isolation.level.service;

import com.isolation.level.domain.DocumentEntity;
import com.isolation.level.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 * @author Ali Karimizandi
 * @since 2021
 */
@RequiredArgsConstructor
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Transactional(isolation = Isolation.READ_UNCOMMITTED, propagation = Propagation.REQUIRES_NEW)
public class DocumentService {

    private final EntityManager entityManager;

    private final DocumentRepository repository;

    public Optional<DocumentEntity> findByCode(Long code) {
        return repository.findByCode(code);
    }

    public DocumentEntity save(DocumentEntity entity, Runnable runnable) {
        DocumentEntity result = repository.save(entity);
        repository.flush();
        if (runnable != null) {
            runnable.run();
        }
        return result;
    }

    public DocumentEntity save(DocumentEntity entity) {
        return save(entity, null);
    }

    public boolean readWaitRead(Long code, Runnable runnable) throws InterruptedException {
        // first read
        DocumentEntity first = null;
        Optional<DocumentEntity> optional = repository.findByCode(code);
        if (optional.isPresent()) {
            first = optional.get();
        }

        // run
        runnable.run();

        // clear cache
        entityManager.clear();

        // second read
        DocumentEntity second = null;
        optional = repository.findByCode(code);
        if (optional.isPresent()) {
            second = optional.get();
        }

        // check that record has been manipulated
        return !first.getMessage().equals(second.getMessage());
    }

    public boolean readBunchWaitReadBunch(Long start, Long end, Runnable runnable) throws InterruptedException {
        // first read
        List<DocumentEntity> firstEntities = repository.getAllByCodeBetween(start, end);

        // run
        runnable.run();

        // clear cache
        entityManager.clear();

        // second read
        List<DocumentEntity> secondEntities = repository.getAllByCodeBetween(start, end);

        // check that record has been manipulated
        for (DocumentEntity entity : firstEntities) {
            Optional<DocumentEntity> optional = secondEntities.stream().filter(e -> e.getId().equals(entity.getId())).findFirst();
            if (!optional.get().getMessage().equals(entity.getMessage())) {
                return true;
            }
        }
        return false;
    }

}
