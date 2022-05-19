package reso.examples.selectiverepeat;
import javax.swing.*;
import org.math.plot.*;

import java.awt.*;


public class PlotWindow{
    private double before_x =-1.0, before_y = -1.0;
    private Plot2DPanel plot;
    private String name;
    private JFrame frame;
    // create your PlotPanel (you can use it as a JPanel)



    public PlotWindow(String name) {
        this.plot = new Plot2DPanel();
        plot.setAxisLabel(0, "Time (in RTT)");
        plot.setAxisLabel(1, "Size cwnd");
        plot.addLinePlot("plot", new double[]{0.0, 0.0}, new double[]{0.0, 0.0});
        this.name =name;
        this.frame = new JFrame(name);
        frame.setContentPane(plot);
        frame.setSize(500,500);
        frame.setVisible(true);
    }

    public void addPoint(double y){
        Color color = Color.RED;
        if (y>before_y){
            color = new Color(30, 100, 40);
        }

        if (before_x !=-1.0  && before_y != -1.0){
            plot.addLinePlot("plot line", color, new double[]{before_x, before_y}, new double[]{x, y});
            frame.setContentPane(plot);
        }
        before_x = before_x+1;
        before_y = y;
    }

    public static void main(String args[]){
        PlotWindow plot = new PlotWindow("test");
        plot.addPoint(0.500, 4);
        plot.addPoint(0.650, 2);
        plot.addPoint(0.800, 5);
        plot.addPoint(1.00, 3);
        plot.addPoint(1.50, 1);
        plot.addPoint(1.70, 3);
        plot.addPoint(1.90, 5);
    }
}

