/*******************************************************************************
 * Copyright (c) 2011 Aalto University
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package eu.mobileguild.graphs;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Rect;

import eu.mobileguild.patterns.NamingStrategyForDoubleParameter;

public class XYMultipleSeriesRenderer {
	
	private List<AxisRenderingInfo> xAxis = new ArrayList<AxisRenderingInfo> (0);
	private List<AxisRenderingInfo> yAxis = new ArrayList<AxisRenderingInfo> (0);

	public static final int DEFAULT_TOP_PADDING = 5;
	public static final int DEFAULT_LEFT_PADDING = 5;
	public static final int DEFAULT_BOTTOM_PADDING = 5;
	public static final int DEFAULT_RIGHT_PADDING = 5;
	public static final int DEFAULT_GRID_STROKE_WIDTH = 1;
	
	private int legendTextSize;
	
	private String chartTitle;
	private int chartTitleTextSize;
	private Rect paddingRect = new Rect(
			DEFAULT_LEFT_PADDING, 
			DEFAULT_TOP_PADDING,
			DEFAULT_RIGHT_PADDING,
			DEFAULT_BOTTOM_PADDING
			);
	
	public void setLegendTextSize(int legendTextSize) {
		this.legendTextSize = legendTextSize;
	}

	public void addYAxis(AxisRenderingInfo axisInfo) {
		yAxis.add(axisInfo);
	}

	public void setYAxisMinMax(double min, double max) {
		setYAxisMinMax(0, min, max);
	}
	
	public void setYAxisMinMax(int index, double min, double max) {
		yAxis.get(index).setMinMax(min, max);
	}
	
	public void removeYAxis(int index) {
		yAxis.remove(index);
	}
	
	public void addXAxis(AxisRenderingInfo axisInfo) {
		xAxis.add(axisInfo);
	}

	public void setXAxisMinMax(double min, double max) {
		setXAxisMinMax(0, min, max);
	}
	
	public void setXAxisMinMax(int index, double min, double max) {
		xAxis.get(index).setMinMax(min, max);
	}
	
	public void removeXAxis(int index) {
		xAxis.remove(index);
	}

	
	public int getXAxisNum() {
		return xAxis.size();
	}
	
	public int getYAxisNum() {
		return yAxis.size();
	}
	
	public void setYTitle(String title) {
		this.yAxis.get(0).setTitle(title);
	}

	public void setYTitle(int index, String title) {
		this.yAxis.get(index).setTitle(title);
	}
	
	public String getYTitle() {
		return yAxis.get(0).getTitle();
	}

	public String getYTitle(int index) {
		return yAxis.get(index).getTitle();
	}
	
	public int getLegendTextSize() {
		return legendTextSize;
	}

	public int getYLabels() {
		return yAxis.get(0).getLabelsNum();
	}

	public int getYAxisTitleTextSize() {
		return yAxis.get(0).getTitleTextSize();
	}

	public int getXAxisTitleTextSize() {
		return xAxis.get(0).getTitleTextSize();
	}

	public double getYAxisMax() {
		return yAxis.get(0).getMax();
	}

	public double getYAxisMax(int index) {
		return yAxis.get(index).getMax();
	}
	
	public double getYAxisMin() {
		return yAxis.get(0).getMin();
	}

	public double getYAxisMin(int index) {
		return yAxis.get(index).getMin();
	}

	public double getXAxisMax() {
		return xAxis.get(0).getMax();
	}

	public double getXAxisMax(int index) {
		return xAxis.get(index).getMax();
	}
	
	public double getXAxisMin() {
		return xAxis.get(0).getMin();
	}

	public double getXAxisMin(int index) {
		return xAxis.get(index).getMin();
	}	
	
	public String getXTitle() {
		return xAxis.get(0).getTitle();
	}

	public String getChartTitle() {
		return chartTitle;
	}

	public void setChartTitle(String chartTitle) {
		this.chartTitle = chartTitle;
	}

	public String getXLabel(double xLabelValue) {
		NamingStrategyForDoubleParameter namingStrategy = xAxis.get(0).getNamingStrategy();
		
		if (namingStrategy != null) {
			return namingStrategy.getName(xLabelValue);
		}		
		
		xLabelValue = Math.round(xLabelValue*10)/10d;
		
		if (Math.round(xLabelValue) != xLabelValue) {
			return Double.toString(xLabelValue);
		} else {
			return Integer.toString((int)xLabelValue);
		}			
	}

	public String getYLabel(double yLabelValue) {
		NamingStrategyForDoubleParameter namingStrategy = xAxis.get(0).getNamingStrategy();
		
		if (namingStrategy != null) {
			return namingStrategy.getName(yLabelValue);
		}		
		
		yLabelValue = Math.round(yLabelValue*10)/10d;
		if (Math.round(yLabelValue) != yLabelValue) {
			return Double.toString(yLabelValue);
		} else {
			return Integer.toString((int)yLabelValue);
		}
	}

	public String getXLabel(int index, double xLabelValue) {
		NamingStrategyForDoubleParameter namingStrategy = xAxis.get(index).getNamingStrategy();
		
		if (namingStrategy != null) {
			return namingStrategy.getName(xLabelValue);
		}		
		
		xLabelValue = Math.round(xLabelValue*10)/10d;
		
		if (Math.round(xLabelValue) != xLabelValue) {
			return Double.toString(xLabelValue);
		} else {
			return Integer.toString((int)xLabelValue);
		}			
	}

	public String getYLabel(int index, double yLabelValue) {
		NamingStrategyForDoubleParameter namingStrategy = yAxis.get(index).getNamingStrategy();
		
		if (namingStrategy != null) {
			return namingStrategy.getName(yLabelValue);
		}		
		
		yLabelValue = Math.round(yLabelValue*10)/10d;
		if (Math.round(yLabelValue) != yLabelValue) {
			return Double.toString(yLabelValue);
		} else {
			return Integer.toString((int)yLabelValue);
		}
	}

	public AxisRenderingInfo getYAxis(int index) {
		return yAxis.get(index);
	}

	public AxisRenderingInfo getXAxis(int index) {
		return xAxis.get(index);
	}

	public int getChartTitleTextSize() {
		return chartTitleTextSize;
	}	
	
	public void setChartTitleTextSize(int chartTitle) {
		this.chartTitleTextSize  = chartTitle;
	}

	public int getGridStrokeWidth() {
		return DEFAULT_GRID_STROKE_WIDTH;
	}

	public Rect getChartPadding() {
		return paddingRect;
	}
	
	public void setPadding(int top, int right, int bottom, int left) {
		paddingRect.top = top;
		paddingRect.right = right;
		paddingRect.bottom = bottom;
		paddingRect.left = left;
	}
	
	public void setPaddingRight(int right) {
		paddingRect.right = right;		
	}
}
