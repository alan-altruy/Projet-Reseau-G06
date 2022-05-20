/*******************************************************************************
 * Copyright (c) 2011 Bruno Quoitin.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Bruno Quoitin - initial API and implementation
 ******************************************************************************/
package reso.examples.selectiverepeat;

import reso.common.AbstractApplication;
import reso.ip.IPAddress;
import reso.ip.IPHost;

/**
 * It sends a message to the receiver
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
     * Send a message to the other side, and then wait for the other side to send a ack back.
     */
    public void start() throws Exception {
        transportLayer.sendMessage(new SelectiveRepeatMessage(numberOfPackets));
    }
    
    // A method that is called when the application is stopped.
    public void stop() {}
}
