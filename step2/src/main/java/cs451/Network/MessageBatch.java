package cs451.Network;

import java.io.InputStream;
import java.io.InputStreamReader;

public class MessageBatch {
    private byte[] payload;

    public MessageBatch(byte source, byte destination) {
        this.payload = new byte[36];
        this.payload[0] = source;
        this.payload[1] = source;
        this.payload[2] = destination;
        this.payload[3] = 0; // size
    }

    public MessageBatch(byte[] payload) {
        this.payload = new byte[36];
        this.payload[0] = payload[0];
        this.payload[1] = payload[1];
        this.payload[2] = payload[2];
        this.payload[3] = payload[3];
        for (int i = 0; i < payload[3]; i++) {
            this.payload[i*4+4] = payload[i*4+4];
            this.payload[i*4+5] = payload[i*4+5];
            this.payload[i*4+6] = payload[i*4+6];
            this.payload[i*4+7] = payload[i*4+7];
        }
    }

    public MessageBatch(MessageBatch messageBatch) {
        this.payload = new byte[36];
        this.payload[0] = messageBatch.getOriginalSource();
        this.payload[1] = messageBatch.getSource();
        this.payload[2] = messageBatch.getDestination();
        this.payload[3] = messageBatch.getSize();
        for (int i = 0; i < messageBatch.getSize(); i++) {
            this.payload[i*4+4] = messageBatch.getPayload()[i*4+4];
            this.payload[i*4+5] = messageBatch.getPayload()[i*4+5];
            this.payload[i*4+6] = messageBatch.getPayload()[i*4+6];
            this.payload[i*4+7] = messageBatch.getPayload()[i*4+7];
        }
    }

    public byte getOriginalSource() {
        return this.payload[0];
    }

    public byte getSource() {
        return this.payload[1];
    }

    public byte getDestination() {
        return this.payload[2];
    }

    public void setSource(byte source) {
        this.payload[1] = source;
    }

    public void setDestination(byte destination) {
        this.payload[2] = destination;
    }

    public byte getSize() {
        return this.payload[3];
    }

    public byte[] getPayload() {
        return this.payload;
    }

    public byte[][] getMessages() {
        byte [][] messages = new byte[this.payload[3]][4];
        for (byte i = 0; i < this.payload[3]; i++) {
            messages[i] = new byte[4];
            messages[i][0] = this.payload[i*4+4];
            messages[i][1] = this.payload[i*4+5];
            messages[i][2] = this.payload[i*4+6];
            messages[i][3] = this.payload[i*4+7];
        }
        return messages;
    }

    public int getMessageInt(byte[] message) {
        return ((message[0] & 0xFF) << 24) | ((message[1] & 0xFF) << 16) | ((message[2] & 0xFF) << 8) | (message[3] & 0xFF);
    }

    public void clear() {
        this.payload[3] = 0;
    }

    public void addMessage(int message) {
        this.payload[this.payload[3]*4+4] = (byte) (message >> 24);
        this.payload[this.payload[3]*4+5] = (byte) (message >> 16);
        this.payload[this.payload[3]*4+6] = (byte) (message >> 8);
        this.payload[this.payload[3]*4+7] = (byte) (message);
        this.payload[3] += 1;       
    }

    public int batchId() {
        return (((payload[4] & 0xFF) << 24) | ((payload[5] & 0xFF) << 16) | ((payload[6] & 0xFF) << 8) | (payload[7] & 0xFF)) >> 3;
    }

    public int generateKey() {
        return ((this.payload[0] & 0x7F) << 8) | (this.payload[1] & 0x7F);
    }

    @Override
    public int hashCode() {
        //System.out.println((batchId() << 7) | (payload[1] & 0xFF));
        return (batchId() << 14) | ((payload[0] & 0x7F) << 7) | (payload[2] & 0x7F);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        MessageBatch m = (MessageBatch) o;
        return batchId() == m.batchId() && payload[0] == m.payload[0] && payload[2] == m.payload[2];
    }
}
