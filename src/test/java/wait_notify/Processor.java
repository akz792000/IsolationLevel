package wait_notify;

import java.util.Scanner;

public class Processor {

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
