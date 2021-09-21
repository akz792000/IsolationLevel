package com.isolation.level;

import com.isolation.level.domain.DocumentEntity;
import com.isolation.level.service.ReadUncommittedService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Ali Karimizandi
 * @since 2021
 */
@SpringBootTest
@RunWith(SpringRunner.class)
class IsolationLevelApplicationTests {

    @Autowired
    private ReadUncommittedService service;

    @Test
    public void dirtyRead() throws InterruptedException {
        int numberOfThreads = 2;
        long id = 1L;
        String message = "first";
        ExecutorService executor = Executors.newCachedThreadPool();
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        // first
        executor.submit(() -> {
            try {
                service.saveWaitRollback(message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });

        // second
        AtomicReference<DocumentEntity> reference = new AtomicReference<>();
        executor.submit(() -> {
            try {
                // sure that this thread runs after the first one
                Thread.sleep(2000);

                // read a data that will be removed by the first due to the rollback
                reference.set(service.readNotify(id));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });
        latch.await();

        // sure dirty read is already existed
        assertEquals(reference.get().getName(), message);
    }

    @Test
    public void nonRepeatableRead() throws InterruptedException {
        int numberOfThreads = 2;
        long id = 1L;
        String message = "first";
        ExecutorService executor = Executors.newCachedThreadPool();
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        // prepare entity
        DocumentEntity entity = new DocumentEntity();
        entity.setName(message);
        service.save(entity);

        // first
        AtomicBoolean result = new AtomicBoolean();
        executor.submit(() -> {
            try {
                result.set(service.readWaitRead(id));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });

        // second
        executor.submit(() -> {
            try {
                // sure that this thread runs after the first one
                Thread.sleep(2000);

                // manipulate
                service.manipulateNotify(id, "second");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });
        latch.await();

        // sure dirty read is already existed
        assertTrue(result.get());
    }

}
