package reso.examples.selectiverepeat;

import reso.common.AbstractTimer;
import reso.ip.IPAddress;
import reso.ip.IPHost;
import reso.ip.IPLayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TransportLayer{

    private final IPLayer ip;
    private IPAddress dst;
    private final IPHost host;
    private List<SelectiveRepeatPacket> packets, buffer;
    private HashMap<Integer, PacketTimer> packetTimers;
    private int expected=1;
    private int expectedAck=0, repeat = 0;

    private class PacketTimer extends AbstractTimer {

        int sequenceNumber;

        public PacketTimer(int sequenceNumber, double interval) {
            super(host.getNetwork().getScheduler(), interval, false);
            this.sequenceNumber = sequenceNumber;
            this.start();
        }
        protected void run() throws Exception {
            System.out.println("time=" + scheduler.getCurrentTime() + " seqNum=" + sequenceNumber);
            sendPacket(sequenceNumber);
        }
    }

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
        packetTimers = new HashMap<>();
    }

    public void sendMessage(SelectiveRepeatMessage message) throws Exception {
        listen();
        hashMessage(message);
        for (SelectiveRepeatPacket packet : packets) {
            sendPacket(packets.indexOf(packet));
        }
    }

    public void sendAck(SelectiveRepeatAck ack, IPAddress source, IPAddress destination) throws Exception {
        ip.send(source, destination, SelectiveRepeatProtocol.IP_PROTO_SELECTIVE_REPEAT, ack);
    }

    public void sendPacket(int sequenceNumber) throws Exception{
        if (Math.random() < 0.9){
            ip.send(IPAddress.ANY, dst, SelectiveRepeatProtocol.IP_PROTO_SELECTIVE_REPEAT, packets.get(sequenceNumber));
        } else {
            System.out.println("Packet lost [sequence number:  " + packets.get(sequenceNumber).getSequenceNumber() + "]");
        }
        if (packetTimers.containsKey(sequenceNumber)) {
            packetTimers.get(sequenceNumber).stop();
        }
        packetTimers.put(sequenceNumber, new PacketTimer(sequenceNumber, 2.0));
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

    public void receiveAck(SelectiveRepeatAck ack, IPAddress dst, IPAddress src) throws Exception {
        if (ack.getPayload()==expectedAck){
            repeat++;
        } else {
            packetTimers.get(expectedAck).stop();
            repeat=1;
            expectedAck = ack.getPayload();
        }
        //System.out.println("Repeat: " + repeat);
        if (repeat == 3){
            sendPacket(ack.getPayload());
        }
    }

    public void receivePacket(SelectiveRepeatPacket packet, IPAddress dst, IPAddress src) throws Exception {
        int sequenceNumber = packet.getSequenceNumber();
        //System.out.println("sq: " + packet.getSequenceNumber());
        if (expected == sequenceNumber){
            packets.add(packet);
            sendAck(new SelectiveRepeatAck(sequenceNumber), dst, src);
            expected++;
            checkBuffer(dst, src);
        } else {
            buffer.add(packet);
            sendAck(new SelectiveRepeatAck(expected-1), dst, src);
        }
    }

    private void checkBuffer(IPAddress dst, IPAddress src) throws Exception {
        List<SelectiveRepeatPacket> toMove = new ArrayList<>();
        for (SelectiveRepeatPacket packet : buffer){
            if (packet.getSequenceNumber() == expected){
                sendAck(new SelectiveRepeatAck(expected), dst, src);
                toMove.add(packet);
                expected++;
            } else {
                break;
            }
        }
        buffer.removeAll(toMove);
        packets.addAll(toMove);
    }
}
