package reso.examples.selectiverepeat;
import java.util.ArrayList;
import java.util.List;

/**
 * It's a class that represents the congestion window
 */
public class CongestionWindow {

    private int size, firstSeqNum, nextSeqNum, ssThresh;
    private boolean inSlowStart;
    private List<Boolean> packetAcks;
    private List<SelectiveRepeatPacket> window;
    private TransportLayer transportLayer;
    private PlotWindow plot;

    // This is the constructor of the class CongestionWindow. It initializes the variables of the
    // class.
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


    /**
     * If the window size is greater than the slow start threshold, then we are in congestion avoidance
     * mode. If we are in slow start mode, then we double the window size. If we are in congestion
     * avoidance mode, then we increment the window size by one
     */
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

    /**
     * When a timeout occurs, the congestion window is halved and the slow start threshold is set to
     * half the congestion window
     */
    public void timeout(){
        inSlowStart=true;
        ssThresh = size/2 + size%2;
        size=1;
        plot.addPoint(size+0.0);
    }

   /**
    * The multiplicative decrease function halves the congestion window size and sets the slow start
    * threshold to the congestion window size
    */
    public void multiplicativeDecrease(){
        ssThresh = size/2 + size%2;
        size = ssThresh;
        plot.addPoint(size+0.0);
    }

    /**
     * If the sequence number is valid, set the corresponding bit in the bit array to true, check the
     * window, update the window size, and add the new window size to the plot
     * 
     * @param sequenceNumber The sequence number of the packet that was received.
     */
    public void setAck(int sequenceNumber) throws Exception {
        if (sequenceNumber >= firstSeqNum){
            packetAcks.set(sequenceNumber - firstSeqNum, true);
            checkWindow();
            updateWindowSize();
            plot.addPoint(size);
        }
    }

    /**
     * If the window is not full, add the packet to the window and send it
     * 
     * @param packet The packet to be added to the window
     */
    public void addPacket(SelectiveRepeatPacket packet) throws Exception {
        if (window.size() < size && transportLayer.getSizeMessage()-1 >= nextSeqNum){
            window.add(packet);
            printWindow();
            packetAcks.add(false);
            transportLayer.sendPacket(packet.getSequenceNumber());
        }
    }

    /**
     * This function removes the first packet in the window and updates the window accordingly
     */
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

    /**
     * If the first packet in the window has been acked, remove it from the window
     */
    private void checkWindow() throws Exception {
        if (packetAcks.get(0).equals(true)){
            removePacket();
        }
    }

    /**
     * This function returns the size of the wuindow.
     * 
     * @return The size of the window.
     */
    public int getSize(){
        return size;
    }

    /**
     * It prints the sequence numbers of the packets in the window
     */
    private void printWindow(){
        String txt="     Congestion Window (size: "+size+") : ";

        for (int i=0; i< Math.min(size, window.size()); i++){
            txt += window.get(i).getSequenceNumber()+" - ";
        }
        System.out.println(txt);
    }
}
