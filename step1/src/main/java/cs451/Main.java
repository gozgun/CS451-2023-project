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

import cs451.Links.PerfectLinks;
import cs451.Network.Message;
import cs451.Network.MessageBatch;
import cs451.Constants;


public class Main {

    private static ArrayBlockingQueue<String> logs;
    private static PrintWriter writer;
    private static Parser parser;
    private static PerfectLinks perfectLink;

    private static void handleSignal() {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");

        perfectLink.halt();

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
                String source = Integer.toString((int)messageBatch.getSource());
                for (int i = 0; i < messageBatch.getSize(); i++) {
                    while (!logs.offer("d " + source + " " + Integer.toString(messageBatch.getMessageInt(messages[i])))) {
                        write();
                    }
                } 
        }
        catch (Exception e) { System.out.println(e);

        }
        
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
        byte target_id = (byte) Integer.parseInt(data.split(" ")[1].trim());

        byte myId = (byte) parser.myId();
        Host target = null; // the host with the target_id
        int port = -1; // my port

        ConcurrentHashMap <Byte, Host> hostMap = new ConcurrentHashMap<Byte, Host>();

        for (Host host : hosts) {
            if ((byte) host.getId() == target_id) { // the target is the host with the target_id
                target = host;
            }
            if ((byte) host.getId() == myId) { // my port is the port of the host with myId
                port = host.getPort();
            }
            hostMap.put((byte) host.getId(), host);
        }

        perfectLink = new PerfectLinks(hostMap, myId);
        perfectLink.run();

        if (myId == target_id) { //if I am the receiving process
            return; //don't send any messages
        }

        MessageBatch messageBatch = new MessageBatch(myId, target_id);
        for (int i = 1; i <= num_messages; i++) { 
            if (messageBatch.getSize() == 8) {
                perfectLink.send(messageBatch);
                messageBatch = new MessageBatch(myId, target_id);
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
        perfectLink.send(messageBatch);


        // After a process finishes broadcasting,
        // it waits forever for the delivery of messages.
        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }
    }
}
