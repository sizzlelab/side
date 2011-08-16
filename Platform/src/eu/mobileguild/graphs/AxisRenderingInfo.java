package eu.mobileguild.graphs;

import eu.mobileguild.patterns.NamingStrategyForDoubleParameter;

public class AxisRenderingInfo {

	private static final int DEFAULT_LABELS_NUM = 5;
	private static final int DEFAULT_NOTCH_LENGTH = 3;
	private static final float DEFAULT_AXIS_STROKE_WIDTH = 2;
	private static final float DEFAULT_NOTCH_TEXT_SIZE = 12;
	private static final String DEFAULT_AXIS_LABEL_TEXT_EXAMPLE = "-180.0";

	private String title;
	private int titleTextSize;
	private int labelsNum = DEFAULT_LABELS_NUM;
	private int labelsTextSize;	
	
	private NamingStrategyForDoubleParameter namingStrategy;
	
	private double[] minMax = new double[2];
	private String axisLabelTextExample = DEFAULT_AXIS_LABEL_TEXT_EXAMPLE;
	private float axisLabelTextSize;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getTitleTextSize() {
		return titleTextSize;
	}

	public void setTitleTextSize(int titleTextSize) {
		this.titleTextSize = titleTextSize;
	}

	public int getLabelsNum() {
		return labelsNum;
	}

	public void setLabelsNum(int numLabels) {
		this.labelsNum = numLabels;
	}

	public NamingStrategyForDoubleParameter getNamingStrategy() {
		return namingStrategy;
	}

	public void setNamingStrategy(NamingStrategyForDoubleParameter namingStrategy) {
		this.namingStrategy = namingStrategy;
	}

	public double getMin() {
		return minMax[0];
	}

	public double getMax() {
		return minMax[1];
	}
	
	public double range() {
		return minMax[1] - minMax[0];
	}
	
	public void setMinMax(double min, double max) {
		this.minMax[0] = min;
		this.minMax[1] = max;
	}
	
	
	public int getLabelsTextSize() {
		return labelsTextSize;
	}

	public void setLabelsTextSize(int labelsTextSize) {
		this.labelsTextSize = labelsTextSize;
	}

	public int getNotchLength() {
		return DEFAULT_NOTCH_LENGTH;
	}

	public float getAxisStrokeWidth() {
		return DEFAULT_AXIS_STROKE_WIDTH;
	}

	public float getNotchTextSize() {
		return DEFAULT_NOTCH_TEXT_SIZE;
	}

	public String getAxisLabelTextExample() {
		return axisLabelTextExample;
	}
	
	// TODO refactor to "AxisLabelMaxLength	
	public void setAxisLabelTextExample(String example) {
		axisLabelTextExample = example;
	}

	public float getAxisLabelTextSize() {
		return axisLabelTextSize;
	}
	
	public void setAxisLabelTextSize(float size) {
		axisLabelTextSize = size;
	}
}