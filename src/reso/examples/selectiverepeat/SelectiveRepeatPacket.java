package reso.examples.selectiverepeat;

import reso.common.Message;

import java.util.ArrayList;
import java.util.List;

public class SelectiveRepeatPacket implements Message {

    private final int payload;
    private final int sequenceNumber;

    SelectiveRepeatPacket(int payload, int sequenceNumber){
        this.payload = payload;
        this.sequenceNumber = sequenceNumber;
    }

    static List<SelectiveRepeatPacket> hashMessage(SelectiveRepeatMessage message){
        List<Integer> payload = message.getPayload();
        List<SelectiveRepeatPacket> packets = new ArrayList<>();
        System.out.println("Hash the message: \n ");
        for (int i = 0; i < payload.size(); i++) {
            packets.add(new SelectiveRepeatPacket(payload.get(i), i));
            System.out.println("Packet " + (i) + ", payload: " + payload.get(i));
        }
        System.out.println("");
        System.out.println("-----------------------------------------");
        System.out.println("");
        return packets;
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
