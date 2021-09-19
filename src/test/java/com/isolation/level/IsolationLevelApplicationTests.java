package com.isolation.level;

import com.isolation.level.domain.DocumentEntity;
import com.isolation.level.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;


/**
 * @author Ali Karimizandi
 * @since 2021
 */
@SpringBootTest
@RunWith(SpringRunner.class)
class IsolationLevelApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    private Object lock = new Object();

    @Test
    public void readUncommitted() throws InterruptedException {
        int numberOfThreads = 2;
        String message = "first";
        ExecutorService service = Executors.newCachedThreadPool();
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        // first
        service.submit(() -> {
            synchronized (lock) {
                try {
                    // prepare entity
                    DocumentEntity entity = new DocumentEntity();
                    entity.setName(message);

                    // save
                    DocumentService documentService = applicationContext.getAutowireCapableBeanFactory().getBean(DocumentService.class);
                    documentService.save(entity);

                    // wait
                    lock.wait();
                    throw new UnsupportedOperationException("Rollback");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            }
        });

        // second
        AtomicReference<DocumentEntity> reference = new AtomicReference<>();
        service.submit(() -> {
            synchronized (lock) {
                try {
                    // sleep
                    Thread.sleep(2000);

                    // set reference
                    DocumentService documentService = applicationContext.getAutowireCapableBeanFactory().getBean(DocumentService.class);
                    Optional<DocumentEntity> optional = documentService.findById(1L);
                    reference.set(optional.get());

                    // notify
                    lock.notify();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            }
        });
        latch.await();
        assertEquals(reference.get().getName(), message);
    }

}
