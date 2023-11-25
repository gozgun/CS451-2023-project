package cs451.Links;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashSet;
import java.net.DatagramSocket;

import cs451.Network.MessageBatch;
import cs451.Host;
import cs451.Broadcast.BestEffortBroadcast;
import cs451.Links.StubbornLinks;

public class PerfectLinks{
    private StubbornLinks stubbornLink;
    private AtomicBoolean running = new AtomicBoolean(true);
    private ConcurrentHashMap<Integer, HashSet<Integer>> messageMap;
    private BestEffortBroadcast beb;

    public PerfectLinks(ConcurrentHashMap<Byte, Host> hostMap, byte myId, BestEffortBroadcast beb) {
        this.beb = beb;
        this.stubbornLink = new StubbornLinks(hostMap, myId, this);
        this.messageMap = new ConcurrentHashMap<Integer, HashSet<Integer>>();
    }

    public void send(MessageBatch messageBatch) {
        this.stubbornLink.send(messageBatch);
    }

    public void deliver(MessageBatch messageBatch){
        if(messageMap.containsKey(messageBatch.generateKey()) && messageMap.get(messageBatch.generateKey()).contains(messageBatch.batchId())){
           return;
        }
        if (!messageMap.containsKey(messageBatch.generateKey())) {
            messageMap.put(messageBatch.generateKey(), new HashSet<Integer>());
        }
        messageMap.get(messageBatch.generateKey()).add(messageBatch.batchId());
        this.beb.deliver(messageBatch);
    }

    public void start() {
        this.stubbornLink.start(); 
    }

    public void halt() {
        running.set(false);
        this.stubbornLink.halt();
    }
}
