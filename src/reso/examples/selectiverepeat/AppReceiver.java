package reso.examples.selectiverepeat;

import reso.common.AbstractApplication;
import reso.ip.IPHost;

/**
 * This class represents the application that will receive a message from the sender application.
 * It extends the abstract class AbstractApplication and implements the method start().
 */
public class AppReceiver extends AbstractApplication {
	
	private final TransportLayer transportLayer;
    	
	
	// Creating a new instance of the class AppReceiver.
	public AppReceiver(IPHost host, Double rate) {
		super(host, "receiver");
		transportLayer = new TransportLayer(host, rate);
    }
	
	/**
	 * Start listening for incoming connections.
	 */
	public void start() {
    	transportLayer.listen();
    }
	
	/**
	 * Stop the app.
	 */
	public void stop() {}
}
