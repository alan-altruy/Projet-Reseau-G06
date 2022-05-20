package reso.examples.selectiverepeat;
import java.util.ArrayList;
import java.util.List;

public class CongestionWindow {

    private int size, firstSeqNum, nextSeqNum, ssThresh;
    private boolean inSlowStart;
    private List<Boolean> packetAcks;
    private List<SelectiveRepeatPacket> window;
    private TransportLayer transportLayer;
    private PlotWindow plot;

    public CongestionWindow(TransportLayer transportLayer) {
        this.transportLayer = transportLayer;
        this.size = 1;
        this.ssThresh = 8;
        this.firstSeqNum = 0;
        this.inSlowStart = true;
        this.packetAcks= new ArrayList<>();
        this.window = new ArrayList<>();
        this.plot = new PlotWindow("Window size over time");
    }


    private void updateWindowSize() throws Exception {
        if (size >= ssThresh) {
            inSlowStart = false;
        }
        if (inSlowStart) {
            size *= 2;//Slow Start
        } else {
            size++;//CA
        }
        while (window.size() < size && nextSeqNum < transportLayer.getSizeMessage()) {
            addPacket(transportLayer.getPacket(nextSeqNum));
            nextSeqNum = window.get(window.size() - 1).getSequenceNumber() + 1;
        }
    }

    public void timeout(){
        inSlowStart=true;
        ssThresh = size/2 + size%2;
        size=1;
        plot.addPoint(size+0.0);
    }

    public void multiplicativeDecrease(){
        ssThresh = size/2 + size%2;
        size = ssThresh;
        plot.addPoint(size+0.0);
    }

    public void setAck(int sequenceNumber) throws Exception {
        if (sequenceNumber >= firstSeqNum){
            packetAcks.set(sequenceNumber - firstSeqNum, true);
            checkWindow();
            updateWindowSize();
            plot.addPoint(size);
        }
    }

    public void addPacket(SelectiveRepeatPacket packet) throws Exception {
        if (window.size() < size && transportLayer.getSizeMessage()-1 >= nextSeqNum){
            window.add(packet);
            printWindow();
            packetAcks.add(false);
            transportLayer.sendPacket(packet.getSequenceNumber());
        }
    }

    public void removePacket() throws Exception {
        //plot.addPoint(size+0.0);
        nextSeqNum = window.get(window.size()-1).getSequenceNumber()+1;
        packetAcks.remove(0);
        window.remove(0);
        printWindow();
        firstSeqNum++;
        if (!window.isEmpty()){
            checkWindow();
        }
    }

    private void checkWindow() throws Exception {
        if (packetAcks.get(0).equals(true)){
            removePacket();
        }
    }

    public int getSize(){
        return size;
    }

    private void printWindow(){
        String txt="     Congestion Window (size: "+size+") : ";

        for (int i=0; i< Math.min(size, window.size()); i++){
            txt += window.get(i).getSequenceNumber()+" - ";
        }
        System.out.println(txt);
    }

    public void addPlot() {

    }
}
