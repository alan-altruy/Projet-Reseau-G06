package reso.examples.selectiverepeat;

import reso.ip.IPAddress;
import reso.ip.IPHost;
import reso.ip.IPLayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TransportLayer{

    private final IPLayer ip;
    private IPAddress dst;
    private final IPHost host;
    private List<SelectiveRepeatPacket> packets, buffer;
    private int expected=1;


    public TransportLayer(IPHost host){
        buffer = new ArrayList<>();
        packets = new ArrayList<>();
        ip = host.getIPLayer();
        this.host = host;
    }

    public TransportLayer(IPHost host, IPAddress dst) {
        this.packets = new ArrayList<>();
        ip = host.getIPLayer();
        this.dst = dst;
        this.host = host;
    }

    public void send(SelectiveRepeatMessage message) throws Exception {
        listen();
        hashMessage(message);
        for (SelectiveRepeatPacket packet : packets) {
            send(packets.indexOf(packet));
        }
    }

    public void send(SelectiveRepeatAck ack, IPAddress source, IPAddress destination) throws Exception {
        ip.send(source, destination, SelectiveRepeatProtocol.IP_PROTO_SELECTIVE_REPEAT, ack);
    }

    public void send(int sequenceNumber) throws Exception{
        if (Math.random() < 0.9){
            ip.send(IPAddress.ANY, dst, SelectiveRepeatProtocol.IP_PROTO_SELECTIVE_REPEAT, packets.get(sequenceNumber));
        } else {
            System.out.println("Packet lost [sequence number:  " + packets.get(sequenceNumber).getSequenceNumber() + "]");
        }
    }

    private void hashMessage(SelectiveRepeatMessage message){
        List<Integer> payload = message.getPayload();
        for (int i = 0; i < payload.size(); i++) {
            packets.add(new SelectiveRepeatPacket(payload.get(i), i+1));
            System.out.println("Packet " + (i+1) + ", payload: " + payload.get(i));
        }
    }

    public void listen(){
        ip.addListener(SelectiveRepeatProtocol.IP_PROTO_SELECTIVE_REPEAT, new SelectiveRepeatProtocol(host, this));
    }

    public void receive(SelectiveRepeatPacket packet, IPAddress dst, IPAddress src) throws Exception {
        int sequenceNumber = packet.getSequenceNumber();
        SelectiveRepeatAck ack;
        if (expected == sequenceNumber){
            packets.add(packet);
            send(new SelectiveRepeatAck(sequenceNumber), dst, src);
            expected++;
            checkBuffer(dst, src);
        }
        else {
            buffer.add(packet);
            send(new SelectiveRepeatAck(expected-1), dst, src);
        }
    }

    private void checkBuffer(IPAddress dst, IPAddress src) throws Exception {
        List<SelectiveRepeatPacket> toMove = new ArrayList<>();
        for (SelectiveRepeatPacket packet : buffer){
            if (packet.getSequenceNumber() == expected){
                send(new SelectiveRepeatAck(expected), dst, src);
                toMove.add(packet);
                expected++;
            } else {
                send(new SelectiveRepeatAck(expected-1), dst, src);
                send(new SelectiveRepeatAck(expected-1), dst, src);
                break;
            }
        }
        buffer.removeAll(toMove);
        packets.addAll(toMove);
    }
}
