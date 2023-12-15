package cs451;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

import java.net.DatagramSocket;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.FileWriter;
import java.io.PrintWriter;

import cs451.Broadcast.FIFOBroadcast;
import cs451.Network.MessageBatch;
import cs451.Constants;


public class Main {
    private static ArrayBlockingQueue<String> logs;
    private static PrintWriter writer;
    private static Parser parser;
    private static FIFOBroadcast fifo;

    private static void handleSignal() {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");

        fifo.halt();

        //write/flush output file if necessary
        System.out.println("Writing output.");

        write();
    }

    private static void initSignalHandlers() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                handleSignal();
            }
        });
    }

    public static void deliver(MessageBatch messageBatch){
        try{
                byte[][] messages = messageBatch.getMessages();
                String source = Integer.toString((int)messageBatch.getOriginalSource());
                for (int i = 0; i < messageBatch.getSize(); i++) {
                    while (!logs.offer("d " + source + " " + Integer.toString(messageBatch.getMessageInt(messages[i])))) {
                        write();
                    }
                } 
        }
        catch (Exception e) { System.out.println(e); }    
    }

    public static void write() {
        try {
            synchronized (logs) {
                writer = new PrintWriter(new FileWriter(parser.output(), true));
                while (!logs.isEmpty()) {
                    writer.println(logs.poll());
                }
                writer.close();
            }
        } 
        catch (Exception e) { e.printStackTrace(); }
    }
    
    public static void main(String[] args) throws InterruptedException {
        logs = new ArrayBlockingQueue<String>(1000);
        parser = new Parser(args);
        parser.parse();

        initSignalHandlers();

        // example
        long pid = ProcessHandle.current().pid();
        System.out.println("My PID: " + pid + "\n");
        System.out.println("From a new terminal type `kill -SIGINT " + pid + "` or `kill -SIGTERM " + pid + "` to stop processing packets\n");

        System.out.println("My ID: " + parser.myId() + "\n");
        System.out.println("List of resolved hosts is:");
        System.out.println("==========================");
        for (Host host: parser.hosts()) {
            System.out.println(host.getId());
            System.out.println("Human-readable IP: " + host.getIp());
            System.out.println("Human-readable Port: " + host.getPort());
            System.out.println();
        }
        System.out.println();

        System.out.println("Path to output:");
        System.out.println("===============");
        System.out.println(parser.output() + "\n");

        System.out.println("Path to config:");
        System.out.println("===============");
        System.out.println(parser.config() + "\n");

        System.out.println("Doing some initialization\n");

        System.out.println("Broadcasting and delivering messages...\n");

        List<Host> hosts = parser.hosts();
        String data = "";

        try {data = new String(Files.readAllBytes(Paths.get(parser.config())));}
        catch (Exception e) { System.out.println(e);}
        
        int num_messages = Integer.parseInt(data.split(" ")[0].trim());

        byte myId = (byte) parser.myId();

        ConcurrentHashMap <Byte, Host> hostMap = new ConcurrentHashMap<Byte, Host>();

        for (Host host : hosts) {
            hostMap.put((byte) host.getId(), host);
        }

        fifo = new FIFOBroadcast(hostMap, myId);
        fifo.start();

        MessageBatch messageBatch = new MessageBatch(myId, myId);
        for (int i = 1; i <= num_messages; i++) { 
            if (messageBatch.getSize() == 8) {
                fifo.broadcast(messageBatch);
                messageBatch = new MessageBatch(myId, myId);
            }
            try {
                messageBatch.addMessage(i);
                synchronized (logs) {
                    while (!logs.offer("b " + Integer.toString(i))) {
                        write();
                    }
                }
            } 
            catch (Exception e) { 
                System.out.println(e);
            }
        }
        fifo.broadcast(messageBatch);


        // After a process finishes broadcasting,
        // it waits forever for the delivery of messages.
        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }
    }
}
