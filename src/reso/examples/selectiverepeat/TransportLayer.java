package reso.examples.selectiverepeat;

import reso.common.AbstractTimer;
import reso.ip.IPAddress;
import reso.ip.IPHost;
import reso.ip.IPLayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * It's a class that implements the selective repeat protocol
 */
public class TransportLayer{

    private final double INITIAL_RTO = 3.0;
    private final double ALPHA = 0.125;
    private final double BETA = 0.25;
    private final IPLayer ip;
    private final Double rate;
    private IPAddress dst;
    private final IPHost host;
    private List<SelectiveRepeatPacket> packets, buffer;
    private List<Double> sRTTList, devRTTList;
    private HashMap<Integer, PacketTimer> packetTimers;
    private int expected=0;
    private double rto;
    private int repeat = 0;
    private CongestionWindow congestionWindow;

    private class PacketTimer extends AbstractTimer {

        int sequenceNumber;
        private final boolean isRetransmitted;
        double startTime, endTime;

        public PacketTimer(int sequenceNumber, double interval, boolean isRetransmitted) {
            super(host.getNetwork().getScheduler(), interval, false);
            startTime = host.getNetwork().getScheduler().getCurrentTime();
            this.sequenceNumber = sequenceNumber;
            this.isRetransmitted = isRetransmitted;
            this.start();
        }

        private void stopTimer() {
            endTime = scheduler.getCurrentTime();
            stop();
            System.out.println("$  Timer (seqNum: " + sequenceNumber +
                    ") stopped= " + (int)(getRTT()*1000) + " ms");
        }

        protected void run() throws Exception {
            rto *=2;
            congestionWindow.timeout();
            System.out.println("!  timeout (seqNum: " + sequenceNumber + ") = " +
                    (int)((scheduler.getCurrentTime()-startTime)*1000) +" ms");
            System.out.println("$  RTO updated x2: " + (int)(rto*1000)+" ms");
            sendPacket(sequenceNumber);
        }

        private boolean isRetransmitted(){
            return isRetransmitted;
        }

        private double getRTT(){
            return endTime - startTime;
        }
    }

   
    // It's the constructor of the class TransportLayer for the AppReceiver.
    public TransportLayer(IPHost host, Double rate){
        this.rate = rate;
        buffer = new ArrayList<>();
        packets = new ArrayList<>();
        ip = host.getIPLayer();
        this.host = host;
    }

    // It's the constructor of the class TransportLayer for the AppSender.
    public TransportLayer(IPHost host, IPAddress dst, Double rate) {
        this.rate = rate;
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

    /**
     * The function sends a message for any incoming packets, then it hashes the
     * message into packets, and then it adds the first n packets to the congestion window
     * 
     * @param message The message to be sent
     */
    public void sendMessage(SelectiveRepeatMessage message) throws Exception {
        listen();
        packets = SelectiveRepeatPacket.hashMessage(message);
        for (int i=0; i < congestionWindow.getSize(); i++){
            congestionWindow.addPacket(packets.get(i));
        }
    }

    
    /**
     * It sends an ack to the source with a probability of the rate (to simulate packet loss)
     * 
     * @param ack The ack to send
     * @param source The source IP address of the packet.
     * @param destination The destination IP address.
     */
    private void sendAck(SelectiveRepeatAck ack, IPAddress source, IPAddress destination) throws Exception {
        System.out.println("");
        if (Math.random() < rate){
            ip.send(source, destination, SelectiveRepeatProtocol.IP_PROTO_SELECTIVE_REPEAT, ack);
            System.out.println("[+] Ack sended [sequence number:  " + ack.getPayload() + "]");
        } else {
            System.out.println("[-] Ack lost [sequence number:  " + ack.getPayload() + "]");
        }
    }

    /**
     * It sends a packet to the destination, and if it's not lost, it starts a timer for that packet
     * 
     * @param sequenceNumber The sequence number of the packet to be sent.
     */
    public void sendPacket(int sequenceNumber) throws Exception {
        System.out.println("");
        if (Math.random() < 0.95){
            ip.send(IPAddress.ANY, dst, SelectiveRepeatProtocol.IP_PROTO_SELECTIVE_REPEAT, packets.get(sequenceNumber));
            System.out.println("[+] Packet sended [sequence number:  " + sequenceNumber + "]");
        } else {
            System.out.println("[-] Packet lost [sequence number:  " + sequenceNumber + "]");
        }
        boolean isRetransmitted = false;
        if (packetTimers.containsKey(sequenceNumber)) {
            packetTimers.get(sequenceNumber).stop();
            isRetransmitted = true;
        }
        packetTimers.put(sequenceNumber, new PacketTimer(sequenceNumber, rto, isRetransmitted));
    }

    /**
     * This function adds a listener to the IP layer that listens for packets with the IP protocol
     * of the Selective Repeat protocol.
     */
    public void listen(){
        ip.addListener(SelectiveRepeatProtocol.IP_PROTO_SELECTIVE_REPEAT, new SelectiveRepeatProtocol(host, this));
    }

    /**
     * If the ack is for the expected packet, increment the repeat counter. If the ack is for the next
     * packet, reset the repeat counter and increment the expected packet. If the ack is for a packet
     * that has a timer, stop the timer and update the RTO
     * 
     * @param ack the ack received from the receiver
     */
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
            if (!timer.isRetransmitted()){
                updateRTO(ack.getPayload());
            }
            congestionWindow.setAck(ack.getPayload());
        }
        if (repeat == 3){
            congestionWindow.multiplicativeDecrease();
        }
    }

    /**
     * The RTO is updated by taking the last SRTT and adding 4 times the last DevRTT
     * 
     * @param sequenceNumber the sequence number of the packet that was just received
     */
    private void updateRTO(int sequenceNumber) {
        updateSRTTList(sequenceNumber);
        updateDevRTTList(sequenceNumber);
        this.rto = sRTTList.get(sRTTList.size()-1) + 4 * devRTTList.get(devRTTList.size()-1);
        System.out.println("$  RTO updated: " + rto);
    }

    /**
     * This function updates the sRTTList with the new RTT value
     * 
     * @param sequenceNumber The sequence number of the packet that was just received.
     */
    private void updateSRTTList(int sequenceNumber) {
        double rtt = packetTimers.get(sequenceNumber).getRTT();
        if (sRTTList.isEmpty()){
            sRTTList.add(rtt);
        }
            sRTTList.add((1-ALPHA)*sRTTList.get(sRTTList.size()-1) + ALPHA * rtt);
    }

    /**
     * This function updates the list of deviations of the round trip times
     * 
     * @param sequenceNumber The sequence number of the packet that was just received.
     */
    private void updateDevRTTList(int sequenceNumber){
        Double rtt = packetTimers.get(sequenceNumber).getRTT();
        if (devRTTList.isEmpty()){
            devRTTList.add(rtt/2);
        } else {
            devRTTList.add((1-BETA) * devRTTList.get(devRTTList.size()-1)
                    + BETA * Math.abs(sRTTList.get(sRTTList.size()-1) - rtt));
        }
    }

    /**
     * If the packet is the next expected packet, add it to the list of packets. If it's not the next
     * expected packet, but it's not already in the buffer, add it to the buffer. Then, check the
     * buffer to see if any packets in the buffer can be added to the list of packets. Finally, send an
     * ack for the packet and print the message
     * 
     * @param packet The packet that was received
     * @param dst destination address
     * @param src The source IP address of the packet
     */
    public void receivePacket(SelectiveRepeatPacket packet, IPAddress dst, IPAddress src) throws Exception {
        int sequenceNumber = packet.getSequenceNumber();
        if (expected == sequenceNumber){
            packets.add(packet);
            expected++;
        } else if (!packets.contains(packet) && !buffer.contains(packet)){
            addToBuffer(packet);
        }
        checkBuffer();
        sendAck(new SelectiveRepeatAck(sequenceNumber), dst, src);
        printMessage();
    }

    /**
     * It prints out the list of packets, the expected sequence number, and the list of packets in the
     * buffer
     */
    private void printMessage(){
        StringBuilder pack = new StringBuilder("     List packets: ");
        StringBuilder buffs = new StringBuilder("     List buffer: ");
        for (SelectiveRepeatPacket packet : packets){
            pack.append(packet.getSequenceNumber()).append(" - ");
        }
        for (SelectiveRepeatPacket buff : buffer){
            buffs.append(buff.getSequenceNumber()).append(" - ");
        }
        System.out.println(pack);
        System.out.println("     Expected = " + expected);
        System.out.println(buffs);
        System.out.println("");
    }

    /**
     * It adds the packet to the buffer in the correct order
     * 
     * @param packet the packet to be added to the buffer
     */
    private void addToBuffer(SelectiveRepeatPacket packet) {
        int seqNum = packet.getSequenceNumber();
        boolean added = false;
        for (int i=0; i< buffer.size(); i++){
            if (buffer.get(i).getSequenceNumber() > seqNum){
                buffer.add(i, packet);
                added=true;
                break;
            }
        }
        if (!added){
            buffer.add(packet);
        }
    }

    /**
     * If the packet in the buffer has the expected sequence number, move it to the packets list and
     * increment the expected sequence number
     */
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

    /**
     * This function returns the packet with the given sequence number.
     * 
     * @param sequenceNumber The sequence number of the packet you want to get.
     * @return The packet with the sequence number that is passed in.
     */
    public SelectiveRepeatPacket getPacket(int sequenceNumber) {
        return packets.get(sequenceNumber);
    }

    /**
     * This function returns the number of packets in the queue.
     * 
     * @return The size of the packets array list.
     */
    public int getSizeMessage() {
        return packets.size();
    }
}
