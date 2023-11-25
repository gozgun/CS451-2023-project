package cs451.Network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import cs451.Network.MessageBatch;
import cs451.Host;
import cs451.Constants;


public class Client extends Thread {

    private DatagramSocket socket;
    private ConcurrentHashMap<Byte, Host> hostMap;
    private ArrayBlockingQueue<MessageBatch> messageQueue;
    private ArrayBlockingQueue<MessageBatch> ackQueue;
    private AtomicBoolean running;

    public Client(DatagramSocket socket, ConcurrentHashMap<Byte, Host> hostMap) {
        try {
            this.socket = socket;
            this.hostMap = hostMap;
            this.messageQueue = new ArrayBlockingQueue<MessageBatch>(Constants.CLIENT_QUEUE_SIZE);
            this.ackQueue = new ArrayBlockingQueue<MessageBatch>(Constants.CLIENT_ACK_QUEUE_SIZE);
            this.running = new AtomicBoolean(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send(MessageBatch messageBatch) { //add messages to the queue and send them if the queue is full
        try {
            while (running.get() && !messageQueue.offer(messageBatch, 100, java.util.concurrent.TimeUnit.MILLISECONDS)) {
                System.out.println("Client queue is full, waiting");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendAck(MessageBatch messageBatch){ // send acknowledgement to the sender
        try {
            while (running.get() && !ackQueue.offer(messageBatch, 100, java.util.concurrent.TimeUnit.MILLISECONDS)) {
                System.out.println("Ack Client queue is full, waiting");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void run() {
        try {
            while (running.get()) {
                // Send acknowledgements
                MessageBatch ackBatch = this.ackQueue.poll(10, java.util.concurrent.TimeUnit.MILLISECONDS);
                while (ackBatch != null) {
                    byte[] ackData = ackBatch.getPayload();

                    // Create a DatagramPacket to send the data to the server
                    DatagramPacket ackPacket = new DatagramPacket(
                        ackData,
                        ackData.length,
                        InetAddress.getByName(this.hostMap.get(ackBatch.getSource()).getIp()),
                        this.hostMap.get(ackBatch.getSource()).getPort()
                    );
                    // Send the packet to the server
                    this.socket.send(ackPacket);
                    ackBatch = this.ackQueue.poll(10, java.util.concurrent.TimeUnit.MILLISECONDS);
                }

                // Send messages
                MessageBatch messageBatch = this.messageQueue.poll(10, java.util.concurrent.TimeUnit.MILLISECONDS);
                while (messageBatch != null) {
                    byte[] sendData = messageBatch.getPayload();

                    // Create a DatagramPacket to send the data to the server
                    DatagramPacket sendPacket = new DatagramPacket(
                        sendData,
                        sendData.length,
                        InetAddress.getByName(this.hostMap.get(messageBatch.getDestination()).getIp()),
                        this.hostMap.get(messageBatch.getDestination()).getPort()
                    );
                    // Send the packet to the server
                    this.socket.send(sendPacket);
                    messageBatch = this.messageQueue.poll(10, java.util.concurrent.TimeUnit.MILLISECONDS);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void halt() {
        running.set(false);
    }

    
}
