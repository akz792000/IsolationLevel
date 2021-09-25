package thread_sample;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class Sample implements Runnable {

    private int id;

    public Sample(int id) {
        this.id = id;
    }

    public void run() {
        System.out.println("Starting: " + id);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
        }

        System.out.println("Completed: " + id);
    }

}

public class ThreadPool {

    public static void main(String[] args) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        for (int i = 0; i < 5; i++) {
            executor.submit(new Sample(i));
        }

        // this will terminate the program after all task submitted
        // otherwise it will continue forever
        executor.shutdown();
        System.out.println("All tasks submitted.");

        // the current thread will be blocked for 1 second
        executor.awaitTermination(1, TimeUnit.SECONDS);

        System.out.println("All Task completed.");
    }

}
