package com.isolation.level;

import com.isolation.level.domain.DocumentEntity;
import com.isolation.level.service.FirstDocumentService;
import com.isolation.level.service.SecondDocumentService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
    FirstDocumentService first;

    @Autowired
    SecondDocumentService second;

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
                    DocumentEntity entity = new DocumentEntity();
                    entity.setName(message);
                    first.save(entity);
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
        AtomicReference<Optional<DocumentEntity>> entity = null;
        service.submit(() -> {
            synchronized (lock) {
                try {
                    Thread.sleep(2000);
                    entity.set(second.findById(1L));
                    lock.notify();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            }
        });
        latch.await();
        assertEquals(entity.get(), message);

    }

}
