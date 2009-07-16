package phenom.utils.graph;

import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Day;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.date.MonthConstants;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;


public class TimeSeriesGraph extends ApplicationFrame {
	private static final long serialVersionUID = -8786646376046670382L;
	private TimeSeriesCollection dataset = new TimeSeriesCollection();
	private String title, XAxisName, YAxisName;
		
	public TimeSeriesGraph(final String title, final String XAxisName, final String YAxisName) {
		super(title);
		this.title = title;
		this.XAxisName = XAxisName;
		this.YAxisName = YAxisName;
		
	}

	public void addDataSource(final String name, final List<Double> ds) {
		if (ds == null)
			throw new IllegalArgumentException("data source can't be null");
		
		RegularTimePeriod day = new Day(1, MonthConstants.JANUARY, 2008);
		TimeSeries t = new TimeSeries(name);
		for (Double d : ds) {
			t.add(day, d);
			day = day.next();
		}
		dataset.addSeries(t);
	}
	
	public void display() {
		JFreeChart chart = createChart(dataset);
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		chartPanel.setMouseZoomable(true, false);
		setContentPane(chartPanel);
		this.pack();
		RefineryUtilities.centerFrameOnScreen(this);
		setVisible(true);
	}
	
	private JFreeChart createChart(XYDataset dataset) {
		JFreeChart chart = ChartFactory.createTimeSeriesChart(
				title,
				XAxisName,
				YAxisName,
				dataset,
				true,
				true, 
				false);
		
		chart.getXYPlot().getDomainAxis().setTickLabelsVisible(false);
		return chart;
	}
	
	public static void main(String [] args) {
		TimeSeriesGraph graph = new TimeSeriesGraph("Stock", "Date", "Price");		
		List<Double> ds = new ArrayList<Double>();
		for (int i = 0; i < 1000; ++i)
			ds.add(1.0 + i);
		graph.addDataSource("a", ds);		
		graph.display();
	}
}
