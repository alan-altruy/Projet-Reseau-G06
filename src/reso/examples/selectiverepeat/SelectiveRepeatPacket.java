package reso.examples.selectiverepeat;

import reso.common.Message;

public class SelectiveRepeatPacket implements Message {

    private final int payload;
    private final int sequenceNumber;

    SelectiveRepeatPacket(int payload, int sequenceNumber){
        this.payload = payload;
        this.sequenceNumber = sequenceNumber;
    }

    public int getPayload(){
        return payload;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public String toString(){
        return "Payload: " + payload+", sequence number: " + sequenceNumber;
    }

    @Override
    public int getByteLength() {
        return 512;
    }
}
