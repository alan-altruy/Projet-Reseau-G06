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

import reso.common.Message;
import reso.ip.*;

public class SelectiveRepeatProtocol implements IPInterfaceListener {

	public static final int IP_PROTO_SELECTIVE_REPEAT= Datagram.allocateProtocolNumber("SELECTIVE_REPEAT");
	
	private final IPHost host;
	private final TransportLayer transportLayer;
	private int sequenceNumber = 0, repeat = 0;
	
	public SelectiveRepeatProtocol(IPHost host, TransportLayer transportLayer) {
		this.host= host;
		this.transportLayer = transportLayer;
	}
	
	@Override
	public void receive(IPInterfaceAdapter src, Datagram datagram) throws Exception {
		Message message = datagram.getPayload();
		if (message.getByteLength() == 4){
			SelectiveRepeatAck ack = (SelectiveRepeatAck) message;
			if (ack.getPayload()==sequenceNumber){
				repeat++;
			} else {
				repeat=1;
				sequenceNumber = ack.getPayload();
			}
			if (repeat == 3){
				transportLayer.send(ack.getPayload());
			}
		}else{
			SelectiveRepeatPacket packet = (SelectiveRepeatPacket) message;
			transportLayer.receive(packet, datagram.dst, datagram.src);
		}
		System.out.println("SelectiveRepeat (" + (int) (host.getNetwork().getScheduler().getCurrentTime()*1000) + "ms)" +
				" host=" + host.name + ", dgram.src=" + datagram.src + ", dgram.dst=" +
				datagram.dst + ", iif=" + src + ", " + message.toString());
	}
}
