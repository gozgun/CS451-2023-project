package cs451.Links;


import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.net.DatagramSocket;

import cs451.Helper.Deliverer;
import cs451.Network.MessageBatch;
import cs451.Network.Client;
import cs451.Network.Server;
import cs451.Host;
import cs451.Links.PerfectLinks;
import cs451.Constants;


public class StubbornLinks extends Thread{
    private Deliverer deliverer;
    private PerfectLinks perfectLink;
    private Client client;
    private Server server;
    private DatagramSocket socket;
    private ConcurrentHashMap<MessageBatch, Boolean> messageMap;
    private AtomicBoolean running = new AtomicBoolean(true);
    private byte myId;

    public StubbornLinks(ConcurrentHashMap<Byte, Host> hostMap, byte myId, PerfectLinks perfectLink) {
        try {
            this.deliverer = new Deliverer(this);
            this.perfectLink = perfectLink;
            this.socket = new DatagramSocket(hostMap.get(myId).getPort());
            this.client = new Client(this.socket, hostMap);
            this.server = new Server(this.socket, this.deliverer, myId);
            this.messageMap = new ConcurrentHashMap<MessageBatch, Boolean>();
            this.myId = myId;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send(MessageBatch messageBatch) {
        try{
            while (this.messageMap.size() > Constants.SL_MAP_SIZE) {
                Thread.sleep(3000);
            }
            this.client.send(messageBatch);
            this.messageMap.put(messageBatch, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deliver(MessageBatch messageBatch){ 
        if (messageBatch.getSource() == this.myId) {
            if (this.messageMap.containsKey(messageBatch)) { // if message is an acknowledgement
                this.messageMap.remove(messageBatch);
            }
        }
        else { // if it is a regular message
            this.client.sendAck(messageBatch);
            perfectLink.deliver(messageBatch);
        }
    }

    public void run() {
        this.client.start();
        this.server.start();
        this.deliverer.start();
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        while (running.get()) {
            try {
                Thread.sleep(1000);
                for (MessageBatch messageBatch: this.messageMap.keySet()) {
                    if (!this.messageMap.replace(messageBatch, false, true)) {
                        this.client.send(messageBatch);
                    }
                }        
            }   
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void halt() {
        try {
            running.set(false);
            this.client.halt();
            this.server.halt();
            this.deliverer.halt();
            this.socket.close();
            this.client.join();
            this.server.join();
            this.deliverer.join();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
