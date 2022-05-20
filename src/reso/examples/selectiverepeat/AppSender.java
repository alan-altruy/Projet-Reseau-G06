package reso.examples.selectiverepeat;

import reso.common.AbstractApplication;
import reso.ip.IPAddress;
import reso.ip.IPHost;

/**
 * This class represents the application that will send a message to the receiver application.
 * It extends the abstract class AbstractApplication and implements the method start().
 */
public class AppSender extends AbstractApplication {

    private final TransportLayer transportLayer;
    private final int numberOfPackets;

    // The constructor of the class AppSender. It is called when the object is created.
    public AppSender(IPHost host, IPAddress dst, Double rate, int numberOfPackets) {
    	super(host, "sender");
        transportLayer = new TransportLayer(host, dst, rate);
        this.numberOfPackets = numberOfPackets;
    }

    /**
     * Sends a message to the other side of the connection, and then wait to receive an ack back.
     */
    public void start() throws Exception {
        transportLayer.sendMessage(new SelectiveRepeatMessage(numberOfPackets));
    }
    
    // A method that is called when the application is stopped.
    public void stop() {}
}
