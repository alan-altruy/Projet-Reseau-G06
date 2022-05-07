package reso.examples.selectiverepeat;

import reso.common.Message;

public class SelectiveRepeatAck implements Message {

    private final int sequenceNumber;

    public SelectiveRepeatAck(int packetId){
        this.sequenceNumber = packetId;
    }

    public int getPayload(){
        return sequenceNumber;
    }

    public String toString(){
        return "Ack: " + sequenceNumber;
    }

    @Override
    public int getByteLength() {
        return Integer.SIZE /8;
    }
}
