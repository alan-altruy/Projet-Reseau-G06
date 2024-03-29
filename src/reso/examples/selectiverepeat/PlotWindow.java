package reso.examples.selectiverepeat;

import javax.swing.*;

import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.graphics.Insets2D;
import de.erichseifert.gral.graphics.Label;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.lines.DefaultLineRenderer2D;
import de.erichseifert.gral.plots.lines.LineRenderer;
import de.erichseifert.gral.ui.InteractivePanel;

import java.awt.*;

/**
 * It's a Java class that extends JFrame and has a method to add a point to a graph.
 * This represents the evolution of the congestion window's size.
 */
public class PlotWindow extends JFrame{
    private final Color RED_COLOR = Color.RED, GREEN_COLOR = new Color(30, 100, 40);
    private double before_x = 0, before_y = 0;
    private DataTable points;
    private LineRenderer lines;
    private XYPlot plot;

    // It's the constructor of the class. It creates a new window with a graph and sets the title, the
    // size, the labels, the ticks, the insets, the bounds, etc.
    public PlotWindow(String name) {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        this.points = new DataTable(Double.class, Double.class);
        this.plot = new XYPlot(points);
        lines = new DefaultLineRenderer2D();
        plot.setLineRenderers(points, lines);
        setVisible(true);
        setTitle("Congestion Window");

        plot.getAxisRenderer(XYPlot.AXIS_X).setLabel(new Label("Time (in RTT)"));
        plot.getAxisRenderer(XYPlot.AXIS_Y).setTickSpacing(1);
        plot.setInsets(new Insets2D.Double(20, 60, 60, 40));
        plot.getTitle().setText("Congestion Window");
        plot.getAxisRenderer(XYPlot.AXIS_Y).setLabel(new Label("Size of window"));
        plot.getAxisRenderer(XYPlot.AXIS_Y).setMinorTicksVisible(false);
        plot.getAxisRenderer(XYPlot.AXIS_Y).getLabel().setRotation(90);
        getRootPane().setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        plot.setBounds(50, 50, 50, 50);
        this.setBounds(50, 50, 800, 600);
        getContentPane().add(new InteractivePanel(plot));
    }

    /**
     * It adds a point to the graph, and if the point is higher than the previous point, it makes the
     * line green, otherwise it makes it red.
     * 
     * @param y The value of the point to be added.
     */
    public void addPoint(double y){
        before_x++;
        points.add(before_x, y);
        Color color;
        if (before_y<y){
            color = GREEN_COLOR;
        } else {
            color = RED_COLOR;
        }
        plot.getPointRenderers(points).get(0).setColor(color);
        plot.getPointRenderers(points).get(0).setShape(null);
        plot.getLineRenderers(points).get(0).setColor(color);
        before_y = y;
        setVisible(true);
    }
}
