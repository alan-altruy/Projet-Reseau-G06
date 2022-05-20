package reso.examples.selectiverepeat;

import reso.common.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * It's a wrapper class for the payload and sequence number of a packet
 */
public class SelectiveRepeatPacket implements Message {

    private final int payload;
    private final int sequenceNumber;

    // It's a constructor for the class SelectiveRepeatPacket.
    SelectiveRepeatPacket(int payload, int sequenceNumber){
        this.payload = payload;
        this.sequenceNumber = sequenceNumber;
    }

    /**
     * It takes a message and returns a list of packets
     * 
     * @param message The message to be sent.
     * @return A list of packets.
     */
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

    /**
     * This function returns the payload of the current node
     * 
     * @return The payload of the packet.
     */
    public int getPayload(){
        return payload;
    }

    /**
     * This function returns the sequence number of the packet.
     * 
     * @return The sequence number of the packet.
     */
    public int getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * The toString() method returns a string representation of the object
     * 
     * @return The payload and sequence number of the packet.
     */
    public String toString(){
        return "Payload: " + payload+", sequence number: " + sequenceNumber;
    }

    /**
     * This function returns the number of bytes in a sector.
     * 
     * @return The number of bytes in the sector.
     */
    @Override
    public int getByteLength() {
        return 512;
    }
}
