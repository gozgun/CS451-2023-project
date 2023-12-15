package cs451.Broadcast;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import cs451.Network.MessageBatch;
import cs451.Broadcast.BestEffortBroadcast;
import cs451.Helper.EchoPackage;
import cs451.Broadcast.FIFOBroadcast;
import cs451.Host;

public class UniformReliableBroadcast{
    private BestEffortBroadcast beb;
    private FIFOBroadcast fifo;
    private AtomicBoolean running = new AtomicBoolean(true);
    private ConcurrentHashMap<EchoPackage, Integer> echoMap;
    private ConcurrentHashMap<EchoPackage, Boolean> deliveredMap;
    private ConcurrentHashMap<Byte, Host> hostMap;
    private byte myId;
    private EchoPackage tempBroadcast;
    private EchoPackage tempDeliver;
    private AtomicInteger sent = new AtomicInteger(0);
    private int limit;

    public UniformReliableBroadcast(ConcurrentHashMap<Byte, Host> hostMap, byte myId, FIFOBroadcast fifo) {
        this.beb = new BestEffortBroadcast(hostMap, myId, this);
        this.fifo = fifo;
        this.echoMap = new ConcurrentHashMap<EchoPackage, Integer>();
        this.deliveredMap = new ConcurrentHashMap<EchoPackage, Boolean>();
        this.hostMap = hostMap;
        this.myId = myId;
        this.limit = Math.max(10000 / (hostMap.size() * hostMap.size()), 1);
    }

    public void broadcast(MessageBatch messageBatch) {
        while (this.running.get() && this.sent.get() > this.limit) {
            try { Thread.sleep(2000); } 
            catch (Exception e) { e.printStackTrace(); }
        }
        this.tempBroadcast = new EchoPackage(messageBatch);
        echoMap.put(this.tempBroadcast, 1);
        beb.broadcast(messageBatch);
        this.sent.getAndIncrement();
        // System.out.println("URB broadcast " + messageBatch.batchId() + ", limit fullness: " + this.sent.get() + " / " + this.limit);
    }

    public void deliver(MessageBatch messageBatch) {
        this.tempDeliver = new EchoPackage(messageBatch);
        if (deliveredMap.containsKey(this.tempDeliver)) {
            return;
        }
        if (echoMap.containsKey(this.tempDeliver)) {
            echoMap.put(this.tempDeliver, echoMap.get(this.tempDeliver) + 1);
        }
        else {
            echoMap.put(this.tempDeliver, 2);
            //System.out.println("URB echo original source " + messageBatch.getOriginalSource() + " " + messageBatch.batchId() );
            MessageBatch newMessageBatch = new MessageBatch(messageBatch);
            newMessageBatch.setSource(myId);
            beb.broadcast(newMessageBatch);
        }
        if (echoMap.get(this.tempDeliver) > hostMap.size() / 2) {
            if (messageBatch.getOriginalSource() == myId) {
                this.sent.getAndDecrement();
            }
            deliveredMap.put(this.tempDeliver, true);
            echoMap.remove(this.tempDeliver);
            // System.out.println("URB deliver original source" + messageBatch.getOriginalSource() + " batch id" + messageBatch.batchId()  );
            fifo.deliver(messageBatch);
        }   
    }

    public void start() {
        this.beb.start();
    }

    public void halt() {
        running.set(false);
        this.beb.halt();
    }
}
