package com.isolation.level;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;


/**
 * @author Ali Karimizandi
 * @since 2021
 */
@SpringBootTest
@RunWith(SpringRunner.class)
class IsolationLevelApplicationTests {

    @Test
    public void contextLoad() {

    }

    public class MyCounter {
        private int count;

        /*
        public void increment() {
            int temp = count;
            count = temp + 1;
        }
        */

        public synchronized void increment() throws InterruptedException {
            int temp = count;
            wait(100);
            count = temp + 1;
        }

        public int getCount() {
            return count;
        }
    }

    @Test
    public void testCounter() throws InterruptedException {
        MyCounter counter = new MyCounter();
        for (int i = 0; i < 500; i++) {
            counter.increment();
        }
        assertEquals(500, counter.getCount());
    }

    /**
     * This test is reasonable, as we're trying to operate on shared data with several threads.
     * As we keep the number of threads low, like 10, we will notice that it passes almost all the time.
     * Interestingly, if we start increasing the number of threads, say to 100, we will see that the test starts to fail most of the time.
     *
     * @throws InterruptedException
     */
    @Test
    public void testCounterWithConcurrency() throws InterruptedException {
        int numberOfThreads = 2;
        ExecutorService service = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        MyCounter counter = new MyCounter();
        for (int i = 0; i < numberOfThreads; i++) {
            service.submit(() -> {
                try {
                    counter.increment();
                } catch (InterruptedException e) {
                    // Handle exception
                    e.printStackTrace();
                }
                latch.countDown();
            });
        }
        latch.await();
        assertEquals(numberOfThreads, counter.getCount());
    }

}
