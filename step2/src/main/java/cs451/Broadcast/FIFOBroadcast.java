package cs451.Broadcast;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.TreeSet;

import cs451.Main;
import cs451.Host;
import cs451.Network.MessageBatch;
import cs451.Broadcast.UniformReliableBroadcast;

public class FIFOBroadcast {
    private UniformReliableBroadcast urb;
    private AtomicBoolean running = new AtomicBoolean(true);
    private ConcurrentHashMap<Byte, TreeSet<MessageBatch>> toDeliverMap;
    private ConcurrentHashMap<Byte, Integer> lastDeliveredMap;

    public FIFOBroadcast(ConcurrentHashMap<Byte, Host> hostMap, byte myId) {
        this.urb = new UniformReliableBroadcast(hostMap, myId, this);
        this.toDeliverMap = new ConcurrentHashMap<Byte, TreeSet<MessageBatch>>();
        this.lastDeliveredMap = new ConcurrentHashMap<Byte, Integer>();
        // Initialize lastDeliveredMap with all host IDs and values as -1
        for (byte hostId : hostMap.keySet()) {
            lastDeliveredMap.put(hostId, -1);
        }
    }

    public void deliver(MessageBatch messageBatch) {
        // Add message to toDeliverMap
        byte hostId = messageBatch.getOriginalSource();
        if (!toDeliverMap.containsKey(hostId)) {
            toDeliverMap.put(hostId, new TreeSet<MessageBatch>());            
        }
        toDeliverMap.get(hostId).add(messageBatch);

        // Deliver all messages that can be delivered
        while (!toDeliverMap.get(hostId).isEmpty() && toDeliverMap.get(hostId).first().batchId() == lastDeliveredMap.get(hostId) + 1) {
            messageBatch = toDeliverMap.get(hostId).pollFirst();
            lastDeliveredMap.put(hostId, messageBatch.batchId());
            Main.deliver(messageBatch);
        }
    }

    public void broadcast(MessageBatch messageBatch) {
        this.urb.broadcast(messageBatch);
    }

    public void start() {
        this.urb.start();
    }

    public void halt() {
        running.set(false);
        this.urb.halt();
    }
}
