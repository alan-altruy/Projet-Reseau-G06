package reso.examples.selectiverepeat;

import reso.common.Message;
import reso.ip.*;

/**
 * It receives a datagram, checks if it's an ack or a packet, and then calls the appropriate method in
 * the transport layer
 */
public class SelectiveRepeatProtocol implements IPInterfaceListener {

	public static final int IP_PROTO_SELECTIVE_REPEAT= Datagram.allocateProtocolNumber("SELECTIVE_REPEAT");
	
	private final IPHost host;
	private final TransportLayer transportLayer;
	
	// The constructor of the class.
	public SelectiveRepeatProtocol(IPHost host, TransportLayer transportLayer) {
		this.host= host;
		this.transportLayer = transportLayer;
	}
	
	/**
	 * The function receives a datagram from the network layer, and if the datagram contains a packet, it
	 * passes the packet to the transport layer, else it passs the ack to the rransport layer
	 * 
	 * @param src The IPInterfaceAdapter that received the datagram.
	 * @param datagram The datagram that was received.
	 */
	@Override
	public void receive(IPInterfaceAdapter src, Datagram datagram) throws Exception {
		Message message = datagram.getPayload();
		System.out.println("[+] Received (" + (int) (host.getNetwork().getScheduler().getCurrentTime()*1000) + "ms)" +
				" host=" + host.name + ", dgram.src=" + datagram.src + ", dgram.dst=" +
				datagram.dst + ", iif=" + src + ", " + message.toString());
		if (message.getByteLength() == 4){
			SelectiveRepeatAck ack = (SelectiveRepeatAck) message;
			transportLayer.receiveAck(ack);
		}else{
			SelectiveRepeatPacket packet = (SelectiveRepeatPacket) message;
			transportLayer.receivePacket(packet, datagram.dst, datagram.src);
		}
	}
}
