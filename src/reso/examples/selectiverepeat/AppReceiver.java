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
import reso.ip.IPHost;

public class AppReceiver extends AbstractApplication {
	
	private final TransportLayer transportLayer;
    	
	public AppReceiver(IPHost host, Double rate) {
		super(host, "receiver");
		transportLayer = new TransportLayer(host, rate);
    }
	
	public void start() {
    	transportLayer.listen();
    }
	
	public void stop() {}
}
