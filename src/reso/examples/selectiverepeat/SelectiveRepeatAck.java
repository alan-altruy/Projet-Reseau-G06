package reso.examples.selectiverepeat;

import reso.common.Message;

/**
 * It's a ack that contains an integer (sequence number)
 */
public class SelectiveRepeatAck implements Message {

    private final int sequenceNumber;

    // It's a constructor that takes an integer as a parameter and assigns it to the sequenceNumber
    // variable.
    public SelectiveRepeatAck(int packetId){
        this.sequenceNumber = packetId;
    }

    /**
     * The function returns the sequence number of the packet
     * 
     * @return The sequence number of the packet.
     */
    public int getPayload(){
        return sequenceNumber;
    }

    /**
     * The toString() function returns a string representation of the object
     * 
     * @return The sequence number of the ack.
     */
    public String toString(){
        return "Ack: " + sequenceNumber;
    }

    /**
     * This function returns the number of bytes in an integer.
     * 
     * @return The number of bytes in an integer.
     */
    @Override
    public int getByteLength() {
        return Integer.SIZE /8;
    }
}
