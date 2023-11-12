package cs451.Network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.net.InetAddress;
import java.util.Arrays;

import cs451.Network.Message;
import cs451.Network.MessageBatch;
import cs451.Helper.Deliverer;

public class Server extends Thread {

    private static final int BUFFER_SIZE = 35;
    private DatagramSocket socket;
    private Deliverer deliverer;
    private byte myId;
    private AtomicBoolean running;
    private byte[] buffer;

    public Server(DatagramSocket socket, Deliverer deliverer, byte myId) {
        this.socket = socket;
        this.deliverer = deliverer;
        this.myId = myId;
        this.running = new AtomicBoolean(true);
        this.buffer = new byte[BUFFER_SIZE];

    }

    public void run() {
        try {
            // Receive incoming packets in a loop
            while (running.get()) {
                DatagramPacket packet = new DatagramPacket(this.buffer, this.buffer.length);
                this.socket.receive(packet);
                
                // Extract the message from the packet
                MessageBatch messageBatch = new MessageBatch(Arrays.copyOf(this.buffer, this.buffer.length));
                deliverer.deliver(messageBatch);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void halt() {
        running.set(false);
    }
}
