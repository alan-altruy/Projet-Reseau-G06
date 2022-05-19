package reso.examples.selectiverepeat;
import javax.swing.*;
import java.util.List;
import org.math.plot.*;


public class PlotWindow{
    private double before_x =-1.0, before_y = -1.0;
    private Plot2DPanel plot;
    private String name;
    private JFrame frame;
    // create your PlotPanel (you can use it as a JPanel)



    public PlotWindow(String name) {
        this.plot = new Plot2DPanel();
        plot.setAxisLabel(0, "Time(seconds)");
        plot.setAxisLabel(1, "Size of the window");
        this.name =name;
        this.frame = new JFrame(name);
        frame.setContentPane(plot);
        frame.setSize(500,500);
        frame.setVisible(true);
    }

    public void addPoint(double x, double y){
        if (before_x ==-1.0  && before_y == -1.0){
            before_x = x;
            before_y = y;
        } else {
            plot.addLinePlot("plot line", new double[]{before_x, before_y}, new double[]{x, y});
            before_x = x;
            before_y = y;
            frame.setContentPane(plot);
        }
    }

    public static void main(String args[]){
        PlotWindow plot = new PlotWindow("test");
        plot.addPoint(0.500, 4);
        plot.addPoint(0.650, 2);
        plot.addPoint(0.800, 5);
    }
}

