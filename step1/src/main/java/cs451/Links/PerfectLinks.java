package cs451.Links;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashSet;
import java.net.DatagramSocket;

import cs451.Network.MessageBatch;
import cs451.Host;
import cs451.Main;
import cs451.Links.StubbornLinks;

public class PerfectLinks{
    private StubbornLinks stubbornLink;
    private AtomicBoolean running = new AtomicBoolean(true);
    private ConcurrentHashMap<Byte, HashSet<Integer>> messageMap;

    public PerfectLinks(ConcurrentHashMap<Byte, Host> hostMap, byte myId) {
        this.stubbornLink = new StubbornLinks(hostMap, myId, this);
        this.messageMap = new ConcurrentHashMap<Byte, HashSet<Integer>>();
    }

    public void send(MessageBatch messageBatch) {
        this.stubbornLink.send(messageBatch);
    }

    public void deliver(MessageBatch messageBatch){
        if(messageMap.containsKey(messageBatch.getSource()) && messageMap.get(messageBatch.getSource()).contains(messageBatch.batchId())){
           return;
        }
        if (!messageMap.containsKey(messageBatch.getSource())) {
            messageMap.put(messageBatch.getSource(), new HashSet<Integer>());
        }
        messageMap.get(messageBatch.getSource()).add(messageBatch.batchId());
        Main.deliver(messageBatch);
    }

    public void run() {
        this.stubbornLink.start();
        
    }

    public void halt() {
        running.set(false);
        this.stubbornLink.halt();
    }
}
