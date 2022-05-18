package reso.examples.selectiverepeat;

import java.util.ArrayList;
import java.util.List;

public class CongestionWindow {

    private int size, cursor, firstSeqNum;
    private List<Boolean> packetAcks;
    private List<SelectiveRepeatPacket> window;
    private TransportLayer transportLayer;

    public CongestionWindow(TransportLayer transportLayer){
        this.transportLayer = transportLayer;
        this.size = 4;
        this.cursor = 0;
        this.firstSeqNum = 0;
        this.packetAcks= new ArrayList<>();
        this.window = new ArrayList<>();
    }


    public void updateWindowSize(int size){
        this.size = size;
    }

    public void incrementCursor(){
        this.cursor++;
    }

    public void setAck(int sequenceNumber) throws Exception {
        packetAcks.set(sequenceNumber - firstSeqNum, true);
        checkWindow();
    }

    public void removePacket() throws Exception {
        packetAcks.remove(0);
        window.remove(0);
        firstSeqNum++;
        if (!window.isEmpty()){
            int nextSequenceNum = window.get(window.size()-1).getSequenceNumber();
            if (window.size()<size && transportLayer.getSizeMessage()-1 > nextSequenceNum){
                addPacket(transportLayer.getPacket(nextSequenceNum+1));
            }
            checkWindow();
        }
    }

    public void addPacket(SelectiveRepeatPacket packet) throws Exception{
        if (window.size() < getSize()){
            window.add(packet);
            printPacket();
            packetAcks.add(false);
            transportLayer.sendPacket(packet.getSequenceNumber());
        }
    }

    private void printPacket() {
        String txt="";
        for (SelectiveRepeatPacket packet : window){
            txt += packet.getSequenceNumber()+" - ";
        }
        System.out.println(txt);
    }

    private void checkWindow() throws Exception {
        if (packetAcks.get(0).equals(true)){
            removePacket();
        }
    }

    /*public void slowStart(){
        minimum++;
        maximum++;
        updateWindowSize(getSize()+1);
    }*/


    public int getSize(){
        return size;
    }

    /*public boolean isInCongestionWindows(int sequenceNumber){
        return (minimum >= sequenceNumber && maximum <= sequenceNumber) ;
    }*/
}
