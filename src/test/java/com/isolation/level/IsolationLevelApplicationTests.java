package com.isolation.level;

import com.isolation.level.domain.DocumentEntity;
import com.isolation.level.service.DocumentService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Isolation.READ_UNCOMMITTED
 *      dirtyRead           passed
 *      nonRepeatableRead   passed
 *      phantomRead         passed
 *
 * Isolation.READ_COMMITTED
 *      dirtyRead           not-passed
 *      nonRepeatableRead   passed
 *      phantomRead         passed
 *
 * Isolation.REPEATABLE_READ
 *      dirtyRead           not-passed
 *      nonRepeatableRead   not-passed
 *      phantomRead         passed
 *
 * Isolation.SERIALIZABLE
 *      dirtyRead           not-passed
 *      nonRepeatableRead   not-passed
 *      phantomRead         not-passed
 *
 *
 * @author Ali Karimizandi
 * @since 2021
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
@RunWith(SpringRunner.class)
class IsolationLevelApplicationTests {

    final static int numberOfThreads = 2;

    @Order(1)
    @Test
    public void dirtyRead(@Autowired DocumentService service) throws InterruptedException {
        long code = 1L;
        String message = "first";
        ExecutorService executor = Executors.newCachedThreadPool();
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        // first
        executor.submit(() -> {
            try {
                service.saveWaitRollback(code, message);
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
                reference.set(service.readNotify(code));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });
        latch.await();

        // sure dirty read is already existed
        assertEquals(reference.get() == null ? null : reference.get().getMessage(), message);
    }

    @Order(2)
    @Test
    public void nonRepeatableRead(@Autowired DocumentService service) throws InterruptedException {
        long code = 2L;
        String message = "second";
        ExecutorService executor = Executors.newCachedThreadPool();
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        // prepare entity
        DocumentEntity entity = new DocumentEntity();
        entity.setCode(code);
        entity.setMessage(message);
        service.save(entity);

        // first
        AtomicBoolean result = new AtomicBoolean();
        executor.submit(() -> {
            try {
                result.set(service.readWaitRead(code));
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
                service.manipulateNotify(code, "random");
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

    @Order(3)
    @Test
    public void phantomRead(@Autowired DocumentService service) throws InterruptedException {
        ExecutorService executor = Executors.newCachedThreadPool();
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        // prepare entity
        IntStream.rangeClosed(10, 20).forEach(item -> {
            DocumentEntity entity = new DocumentEntity();
            entity.setCode(Long.valueOf(item));
            entity.setMessage(String.valueOf(item));
            service.save(entity);
        });

        // first
        AtomicBoolean result = new AtomicBoolean();
        executor.submit(() -> {
            try {
                result.set(service.readBunchWaitReadBunch(10L, 20L));
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
                Thread.sleep(4000);

                // manipulate
                service.manipulateNotify(15L, "random");
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
