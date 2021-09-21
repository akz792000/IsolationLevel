package com.isolation.level.service;

import com.isolation.level.domain.DocumentEntity;
import com.isolation.level.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Ali Karimizandi
 * @since 2021
 */
@RequiredArgsConstructor
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Transactional(isolation = Isolation.REPEATABLE_READ)
public class DocumentService {

    private final EntityManager entityManager;

    private final DocumentRepository repository;

    private Object lock = new Object();

    public DocumentEntity save(DocumentEntity entity) {
        return repository.save(entity);
    }

    public void saveWaitRollback(Long code, String message) throws InterruptedException {
        synchronized (lock) {
            try {
                // prepare entity
                DocumentEntity entity = new DocumentEntity();
                entity.setCode(code);
                entity.setMessage(message);
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

    public DocumentEntity readNotify(Long code) {
        synchronized (lock) {
            DocumentEntity result = null;
            Optional<DocumentEntity> optional = repository.findByCode(code);
            if (optional.isPresent()) {
                result = optional.get();
            }
            lock.notify();
            return result;
        }
    }

    public boolean readWaitRead(Long code) throws InterruptedException {
        synchronized (lock) {
            // first read
            DocumentEntity first = null;
            Optional<DocumentEntity> optional = repository.findByCode(code);
            if (optional.isPresent()) {
                first = optional.get();
            }

            // wait
            lock.wait();

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
    }

    public void manipulateNotify(Long code, String message) throws InterruptedException {
        synchronized (lock) {
            // read
            DocumentEntity entity = null;
            Optional<DocumentEntity> optional = repository.findByCode(code);
            if (optional.isPresent()) {
                entity = optional.get();
            }

            // prepare entity
            entity.setMessage(message);
            save(entity);

            // ensure the entity will be flushed
            repository.flush();
            lock.notify();
        }
    }

    public boolean readBunchWaitReadBunch(Long start, Long end) throws InterruptedException {
        synchronized (lock) {
            // first read
            List<DocumentEntity> firstEntities = repository.getAllByCodeBetween(start, end);

            // wait
            lock.wait();

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

}
