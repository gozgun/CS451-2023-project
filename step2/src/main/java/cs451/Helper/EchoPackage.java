package cs451.Helper;

import cs451.Network.MessageBatch;

public class EchoPackage {
    private byte originalSource;
    private int batchId;

    public EchoPackage(byte originalSource, int batchId) {
        this.originalSource = originalSource;
        this.batchId = batchId;
    }

    public EchoPackage(MessageBatch messageBatch) {
        this.originalSource = messageBatch.getOriginalSource();
        this.batchId = messageBatch.batchId();
    }

    public byte getOriginalSource() {
        return originalSource;
    }

    public int batchId() {
        return batchId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        EchoPackage other = (EchoPackage) obj;
        return originalSource == other.originalSource && batchId == other.batchId;
    }

    @Override
    public int hashCode() {
        return originalSource * 31 + batchId;
    }
}
