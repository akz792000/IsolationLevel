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
public class ReadUncommittedService {

    private final DocumentRepository repository;

    private Object lock = new Object();

    public Optional<DocumentEntity> findById(Long id) {
        return repository.findById(id);
    }

    public DocumentEntity save(DocumentEntity entity) {
        return repository.save(entity);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public void persist(String message) throws InterruptedException {
        synchronized (lock) {
            try {
                // prepare entity
                DocumentEntity entity = new DocumentEntity();
                entity.setName(message);
                save(entity);

                // ensure the entity will be flushed
                repository.flush();
                lock.wait();

                // cause to happening dirty read for second
                throw new UnsupportedOperationException("Rollback");
            } catch (InterruptedException e) {
                throw e;
            }
        }
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED, readOnly = true)
    public DocumentEntity read(Long id) {
        synchronized (lock) {
            DocumentEntity result = null;
            Optional<DocumentEntity> optional = findById(id);
            if (optional.isPresent()) {
                result = optional.get();
            }
            lock.notify();
            return result;
        }
    }

}
