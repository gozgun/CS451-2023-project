package cs451.Broadcast;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import cs451.Network.MessageBatch;
import cs451.Broadcast.BestEffortBroadcast;
import cs451.Helper.EchoPackage;
import cs451.Main;
import cs451.Host;

public class UniformReliableBroadcast{
    private BestEffortBroadcast beb;
    private AtomicBoolean running = new AtomicBoolean(true);
    private ConcurrentHashMap<EchoPackage, Integer> echoMap;
    private ConcurrentHashMap<EchoPackage, Boolean> deliveredMap;
    private ConcurrentHashMap<Byte, Host> hostMap;
    private byte myId;
    private EchoPackage tempBroadcast;
    private EchoPackage tempDeliver;
    private int sent = 0;
    private int limit = 10;

    public UniformReliableBroadcast(ConcurrentHashMap<Byte, Host> hostMap, byte myId) {
        this.beb = new BestEffortBroadcast(hostMap, myId, this);
        this.echoMap = new ConcurrentHashMap<EchoPackage, Integer>();
        this.deliveredMap = new ConcurrentHashMap<EchoPackage, Boolean>();
        this.hostMap = hostMap;
        this.myId = myId;
    }

    public void broadcast(MessageBatch messageBatch) {
        while (this.running.get() && this.sent > this.limit) {
            try { Thread.sleep(2000); } 
            catch (Exception e) { e.printStackTrace(); }
        }
        this.tempBroadcast = new EchoPackage(messageBatch);
        echoMap.put(this.tempBroadcast, 1);
        beb.broadcast(messageBatch);
        this.sent++;
        System.out.println("URB broadcast " + messageBatch.batchId() + ", limit fullness: " + this.sent + " / " + this.limit);
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
                this.sent--;
            }
            deliveredMap.put(this.tempDeliver, true);
            echoMap.remove(this.tempDeliver);
            System.out.println("URB deliver original source" + messageBatch.getOriginalSource() + " batch id" + messageBatch.batchId()  );
            Main.deliver(messageBatch);
        }   
    }

    public void start() {
        this.beb.start();
    }

    public void halt() {
        running.set(false);
        beb.halt();
    }
}
