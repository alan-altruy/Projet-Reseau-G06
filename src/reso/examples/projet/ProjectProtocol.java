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
package reso.examples.projet;

import reso.ip.*;

public class ProjectProtocol implements IPInterfaceListener {

	public static final int IP_PROTO_PROJECT= Datagram.allocateProtocolNumber("PROJECT");
	
	private final IPHost host; 
	
	public ProjectProtocol(IPHost host) {
		this.host= host;
	}
	
	@Override
	public void receive(IPInterfaceAdapter src, Datagram datagram) throws Exception {
    	ProjectMessage msg= (ProjectMessage) datagram.getPayload();
		System.out.println("Project (" + (int) (host.getNetwork().getScheduler().getCurrentTime()*1000) + "ms)" +
				" host=" + host.name + ", dgram.src=" + datagram.src + ", dgram.dst=" +
				datagram.dst + ", iif=" + src + ", counter=" + msg.num);
    	if (msg.num > 0)
    		host.getIPLayer().send(IPAddress.ANY, datagram.src, IP_PROTO_PROJECT, new ProjectMessage(msg.num-1));
	}
}
