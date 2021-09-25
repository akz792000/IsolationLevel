package thread_sample;

import java.util.Scanner;

class Processor {

    public void produce() throws InterruptedException {
        synchronized (this) { // Intrinsic Lock
            System.out.println("Producer thread running ....");

            // Causes the current thread to wait until it is awakened, typically notified or interrupted
            // and loses control of the lock of this
            wait(); // can exist in synchronized block

            System.out.println("Resumed.");
        }
    }

    public void consume() throws InterruptedException {
        Scanner scanner = new Scanner(System.in);

        // The thread that runs this method will run after the thread which runs the produce method
        Thread.sleep(2000);

        synchronized (this) {
            System.out.println("Waiting for return key.");
            scanner.nextLine();

            // notify the first thread which was waited
            // difference between notify and notifyAll is that, notifyAll will notify all thread which were waited
            notify(); // can exist in synchronized block

            System.out.println("Returned key pressed.");
        }
    }

}

public class WaitNotify {

    public static void main(String[] args) throws InterruptedException {
        final Processor processor = new Processor();

        Thread t1 = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    processor.produce();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        });

        Thread t2 = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    processor.consume();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        });

        t1.start();
        t2.start();

        t1.join();
        t2.join();
    }

}
