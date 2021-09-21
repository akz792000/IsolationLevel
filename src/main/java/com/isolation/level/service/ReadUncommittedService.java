package com.isolation.level.service;

import com.isolation.level.domain.DocumentEntity;
import com.isolation.level.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Optional;

/**
 * @author Ali Karimizandi
 * @since 2021
 */
@RequiredArgsConstructor
@Service
public class ReadUncommittedService {

    private final EntityManager entityManager;

    private final DocumentRepository repository;

    private Object lock = new Object();

    public Optional<DocumentEntity> findById(Long id) {
        return repository.findById(id);
    }

    public DocumentEntity save(DocumentEntity entity) {
        return repository.save(entity);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public void saveWaitRollback(String message) throws InterruptedException {
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

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public DocumentEntity readNotify(Long id) {
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

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public boolean readWaitRead(Long id) throws InterruptedException {
        synchronized (lock) {
            // first read
            DocumentEntity first = null;
            Optional<DocumentEntity> optional = findById(id);
            if (optional.isPresent()) {
                first = optional.get();
            }

            // wait
            lock.wait();
            entityManager.clear();

            // second read
            DocumentEntity second = null;
            optional = findById(id);
            if (optional.isPresent()) {
                second = optional.get();
            }

            // check that record has been manipulated
            return !first.getName().equals(second.getName());
        }
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public void manipulateNotify(Long id, String message) throws InterruptedException {
        synchronized (lock) {
            // read
            DocumentEntity entity = null;
            Optional<DocumentEntity> optional = findById(id);
            if (optional.isPresent()) {
                entity = optional.get();
            }

            // prepare entity
            entity.setName(message);
            save(entity);

            // ensure the entity will be flushed
            repository.flush();
            lock.notify();
        }
    }

}
