package cs451.Network;

import java.util.Arrays;

public class Message {
    private byte source;
    private byte destination;
    private byte[] message;

    public Message(byte source, byte destination, int message) {
        this.source = source;
        this.destination = destination;
        this.message = new byte[4];
        this.message[0] = (byte) (message >> 24);
        this.message[1] = (byte) (message >> 16);
        this.message[2] = (byte) (message >> 8);
        this.message[3] = (byte) (message);
    }

    public Message(byte[] payload) {
        this.source = payload[0];
        this.destination = payload[1];
        this.message = new byte[4];
        this.message[0] = payload[2];
        this.message[1] = payload[3];
        this.message[2] = payload[4];
        this.message[3] = payload[5];
    }

    public byte[] getPayload() {
        byte[] payload = new byte[6];
        payload[0] = source;
        payload[1] = destination;
        payload[2] = message[0];
        payload[3] = message[1];
        payload[4] = message[2];
        payload[5] = message[3];
        return payload;
    }

    public byte getSource() {
        return source;
    }

    public byte getDestination() {
        return destination;
    }

    public int getMessage() {
        return (message[0] << 24) + (message[1] << 16) + (message[2] << 8) + message[3];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message m = (Message) o;
        return source == m.source &&
                destination == m.destination &&
                Arrays.equals(message, m.message);
    }

    @Override
    public int hashCode() {
        // TODO: this is not a good hash function
        return source + 128 * destination + 128 * 128 * Arrays.hashCode(message);
    }
}
