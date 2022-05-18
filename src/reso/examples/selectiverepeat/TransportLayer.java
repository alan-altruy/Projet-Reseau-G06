package reso.examples.selectiverepeat;

import reso.common.AbstractTimer;
import reso.ip.IPAddress;
import reso.ip.IPHost;
import reso.ip.IPLayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TransportLayer{

    private final double INITIAL_RTO = 3.0;
    private final double ALPHA = 0.125;
    private final double BETA = 0.25;
    private final IPLayer ip;
    private IPAddress dst;
    private final IPHost host;
    private List<SelectiveRepeatPacket> packets, buffer;
    private List<Double> sRTTList, devRTTList;
    private HashMap<Integer, PacketTimer> packetTimers;
    private int expected=0;
    private double rto;
    private int repeat = 0;
    private CongestionWindow congestionWindow;

    public int getSizeMessage() {
        return packets.size();
    }

    private class PacketTimer extends AbstractTimer {

        int sequenceNumber;
        private final boolean isRetransmitted;
        double startTime, endTime;

        public PacketTimer(int sequenceNumber, double interval, boolean isRetransmitted) {
            super(host.getNetwork().getScheduler(), interval, false);
            this.sequenceNumber = sequenceNumber;
            this.isRetransmitted = isRetransmitted;
            startTime = host.getNetwork().getScheduler().getCurrentTime();
            this.start();
        }

        private void stopTimer() {
            endTime = scheduler.getCurrentTime();
            stop();
        }

        protected void run() throws Exception {
            rto *=2;
            System.out.println("$ RTO updated x2: " + rto);
            System.out.println("- time=" + (scheduler.getCurrentTime()-startTime) + " seqNum=" + (sequenceNumber));
            sendPacket(sequenceNumber);
        }

        private boolean isRetransmitted(){
            return isRetransmitted;
        }

        private double getRTT(){
            return endTime - startTime;
        }
    }

    //Receiver
    public TransportLayer(IPHost host){
        buffer = new ArrayList<>();
        packets = new ArrayList<>();
        ip = host.getIPLayer();
        this.host = host;
    }

    // Sender
    public TransportLayer(IPHost host, IPAddress dst) {
        this.packets = new ArrayList<>();
        this.ip = host.getIPLayer();
        this.dst = dst;
        this.host = host;
        this.packetTimers = new HashMap<>();
        this.rto = INITIAL_RTO;
        this.sRTTList = new ArrayList<>();
        this.devRTTList = new ArrayList<>();
        this.congestionWindow = new CongestionWindow(this);
    }

    public void sendMessage(SelectiveRepeatMessage message) throws Exception {
        listen();
        hashMessage(message);
        for (int i=0; i < congestionWindow.getSize(); i++){
            congestionWindow.addPacket(packets.get(i));
        }
    }

    private void sendAck(SelectiveRepeatAck ack, IPAddress source, IPAddress destination) throws Exception {
        if (Math.random() < 0.9){
            ip.send(source, destination, SelectiveRepeatProtocol.IP_PROTO_SELECTIVE_REPEAT, ack);
        } else {
            System.out.println("[] Ack lost [sequence number:  " + ack.getPayload() + "]");
        }
    }

    public void sendPacket(int sequenceNumber) throws Exception {
        if (Math.random() < 0.9){
            ip.send(IPAddress.ANY, dst, SelectiveRepeatProtocol.IP_PROTO_SELECTIVE_REPEAT, packets.get(sequenceNumber));
        } else {
            System.out.println("[] Packet lost [sequence number:  " + packets.get(sequenceNumber).getSequenceNumber() + "]");
        }
        boolean isRetransmitted = false;
        if (packetTimers.containsKey(sequenceNumber)) {
            packetTimers.get(sequenceNumber).stop();
            isRetransmitted = true;
        }
        packetTimers.put(sequenceNumber, new PacketTimer(sequenceNumber, rto, isRetransmitted));
    }

    private void hashMessage(SelectiveRepeatMessage message){
        List<Integer> payload = message.getPayload();
        for (int i = 0; i < payload.size(); i++) {
            packets.add(new SelectiveRepeatPacket(payload.get(i), i));
            System.out.println("Packet " + (i) + ", payload: " + payload.get(i));
        }
    }

    public void listen(){
        ip.addListener(SelectiveRepeatProtocol.IP_PROTO_SELECTIVE_REPEAT, new SelectiveRepeatProtocol(host, this));
    }

    public void receiveAck(SelectiveRepeatAck ack) throws Exception {
        if (ack.getPayload()==expected){
            repeat++;
        } else if (ack.getPayload()==expected+1){
            repeat = 1;
            expected++;
        }
        if (packetTimers.containsKey(ack.getPayload())) {
            PacketTimer timer = packetTimers.get(ack.getPayload());
            timer.stopTimer();
            congestionWindow.setAck(ack.getPayload());
            if (!timer.isRetransmitted()){
                updateRTO(ack.getPayload());
            }
            System.out.println("Timer of " + ack.getPayload() +
                    " stopped: " + packetTimers.get(ack.getPayload()).getRTT());
        }
        //System.out.println("Repeat: " + repeat);
        if (repeat == 3){
            //TODO diviser fenêtre de congestion
        }
    }

    private void updateRTO(int sequenceNumber) {
        updateSRTTList(sequenceNumber);
        updateDevRTTList(sequenceNumber);
        this.rto = sRTTList.get(sRTTList.size()-1) + 4 * devRTTList.get(devRTTList.size()-1);
        System.out.println("$ RTO updated: " + rto);
    }

    private void updateSRTTList(int sequenceNumber) {
        double rtt = packetTimers.get(sequenceNumber).getRTT();
        if (sRTTList.isEmpty()){
            sRTTList.add(rtt);
        } else {
            sRTTList.add((1-ALPHA)*sRTTList.get(sRTTList.size()-1) + ALPHA * rtt);
        }
    }

    private void updateDevRTTList(int sequenceNumber){
        Double rtt = packetTimers.get(sequenceNumber).getRTT();
        if (devRTTList.isEmpty()){
            devRTTList.add(rtt/2);
        } else {
            devRTTList.add((1-BETA) * devRTTList.get(devRTTList.size()-1)
                    + BETA * Math.abs(sRTTList.get(sRTTList.size()-1) - rtt));
        }
    }

    public void receivePacket(SelectiveRepeatPacket packet, IPAddress dst, IPAddress src) throws Exception {
        int sequenceNumber = packet.getSequenceNumber();
        //System.out.println("sq: " + packet.getSequenceNumber());
        if (expected == sequenceNumber){
            packets.add(packet);
            expected++;
            checkBuffer();
        } else if (!packets.contains(packet) && !buffer.contains(packet)){
            buffer.add(packet);
        }
        sendAck(new SelectiveRepeatAck(sequenceNumber), dst, src);
        //printMessage();
    }

    private void printMessage(){
        StringBuilder pack = new StringBuilder("List packets: ");
        StringBuilder buffs = new StringBuilder("List buffer: ");
        for (SelectiveRepeatPacket packet : packets){
            pack.append(packet.getSequenceNumber()).append(" - ");
        }
        for (SelectiveRepeatPacket buff : buffer){
            buffs.append(buff.getSequenceNumber()).append(" - ");
        }
        System.out.println(pack);
        System.out.println(buffs);
    }

    private void checkBuffer() {
        List<SelectiveRepeatPacket> toMove = new ArrayList<>();
        for (SelectiveRepeatPacket packet : buffer){
            if (packet.getSequenceNumber() == expected){
                toMove.add(packet);
                expected++;
            } else {
                break;
            }
        }
        buffer.removeAll(toMove);
        packets.addAll(toMove);
    }

    public SelectiveRepeatPacket getPacket(int sequenceNumber) {
        return packets.get(sequenceNumber);
    }
}
