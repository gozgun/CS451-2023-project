package cs451.Network;

import java.io.InputStream;
import java.io.InputStreamReader;

public class MessageBatch {
    private byte[] payload;

    public MessageBatch(byte source, byte destination) {
        this.payload = new byte[35];
        this.payload[0] = source;
        this.payload[1] = destination;
        this.payload[2] = 0;
    }

    public MessageBatch(byte[] payload) {
        this.payload = new byte[35];
        this.payload[0] = payload[0];
        this.payload[1] = payload[1];
        this.payload[2] = payload[2];
        for (int i = 0; i < payload[2]; i++) {
            this.payload[i*4+3] = payload[i*4+3];
            this.payload[i*4+4] = payload[i*4+4];
            this.payload[i*4+5] = payload[i*4+5];
            this.payload[i*4+6] = payload[i*4+6];
        }
    }

    public byte getSource() {
        return this.payload[0];
    }

    public byte getDestination() {
        return this.payload[1];
    }

    public byte getSize() {
        return this.payload[2];
    }

    public byte[] getPayload() {
        return this.payload;
    }

    public byte[][] getMessages() {
        byte [][] messages = new byte[this.payload[2]][4];
        for (byte i = 0; i < this.payload[2]; i++) {
            messages[i] = new byte[4];
            messages[i][0] = this.payload[i*4+3];
            messages[i][1] = this.payload[i*4+4];
            messages[i][2] = this.payload[i*4+5];
            messages[i][3] = this.payload[i*4+6];
        }
        return messages;
    }

    public int getMessageInt(byte[] message) {
        return ((message[0] & 0xFF) << 24) | ((message[1] & 0xFF) << 16) | ((message[2] & 0xFF) << 8) | (message[3] & 0xFF);
    }

    public void clear() {
        this.payload[2] = 0;
    }

    public void addMessage(int message) {
        this.payload[this.payload[2]*4+3] = (byte) (message >> 24);
        this.payload[this.payload[2]*4+4] = (byte) (message >> 16);
        this.payload[this.payload[2]*4+5] = (byte) (message >> 8);
        this.payload[this.payload[2]*4+6] = (byte) (message);
        this.payload[2] += 1;       
    }

    public int batchId() {
        return (((payload[3] & 0xFF) << 24) | ((payload[4] & 0xFF) << 16) | ((payload[5] & 0xFF) << 8) | (payload[6] & 0xFF)) >> 3;
    }

    @Override
    public int hashCode() {
        //System.out.println((batchId() << 7) | (payload[1] & 0xFF));
        return (batchId() << 7) | (payload[1] & 0xFF);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        MessageBatch m = (MessageBatch) o;
        return batchId() == m.batchId() && payload[1] == m.payload[1];
    }
}
