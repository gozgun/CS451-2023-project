package cs451.Broadcast;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import cs451.Broadcast.UniformReliableBroadcast;
import cs451.Network.MessageBatch;
import cs451.Links.PerfectLinks;
import cs451.Host;

public class BestEffortBroadcast{
    private UniformReliableBroadcast urb;
    private PerfectLinks perfectLink;
    private AtomicBoolean running = new AtomicBoolean(true);
    private ConcurrentHashMap<Byte, Host> hostMap;
    private byte myId;

    public BestEffortBroadcast(ConcurrentHashMap<Byte, Host> hostMap, byte myId, UniformReliableBroadcast urb) {
        this.urb = urb;
        this.perfectLink = new PerfectLinks(hostMap, myId, this);
        this.hostMap = hostMap;
        this.myId = myId;
    }

    public void broadcast(MessageBatch tempBatch) {
        for (byte hostId : hostMap.keySet()) {
            if (!running.get()) {
                break;
            }
            if (hostId != myId) {
                MessageBatch messageBatch = new MessageBatch(tempBatch);
                messageBatch.setDestination(hostId);
                perfectLink.send(messageBatch);
            }
        }       
    }

    public void deliver(MessageBatch messageBatch) {
        urb.deliver(messageBatch);
    }

    public void start() {
        this.perfectLink.start();
    }

    public void halt() {
        running.set(false);
        perfectLink.halt();
    }
}
