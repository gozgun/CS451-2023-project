package cs451.Helper;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import cs451.Links.StubbornLinks;
import cs451.Network.MessageBatch;
import cs451.Constants;

public class Deliverer extends Thread {

    private ArrayBlockingQueue<MessageBatch> delivererQueue;
    private AtomicBoolean running;
    private StubbornLinks stubbornLink;
 
    public Deliverer(StubbornLinks stubbornLink) {
        this.delivererQueue = new ArrayBlockingQueue<MessageBatch>(Constants.DELIVERER_QUEUE_SIZE);
        this.stubbornLink = stubbornLink;
        this.running = new AtomicBoolean(true);
    }

    public void deliver(MessageBatch messageBatch) {
        try {
            while (running.get() && !delivererQueue.offer(messageBatch, 20, java.util.concurrent.TimeUnit.MILLISECONDS)) {
                System.out.println("Deliverer queue is full, waiting");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (running.get()) {
            try {
                MessageBatch messageBatch = this.delivererQueue.poll(10, java.util.concurrent.TimeUnit.MILLISECONDS);
                if (messageBatch == null) {
                    continue;
                }
                this.stubbornLink.deliver(messageBatch);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void halt() {
        this.running.set(false);
    }
}
