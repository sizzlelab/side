/*******************************************************************************
 * Copyright (c) 2011 Maksim Golivkin
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

import eu.mobileguild.patterns.NamingStrategyForDoubleParameter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;

public class XYChartView extends View {

	public static final int GRAPH_TITLE_PADDING_TOP = 5;
	
	public static final int SPACE_BETWEEN_AXIS_AND_LABEL = 5;

    public static final int LEGEND_VERTICAL_SPACING = 2;
    public static final int LEGEND_CELL_SPACING = 5;
	public static final int LEGEND_SERIES_TITLE_AND_LINE_SPACE = 5;
	
	
	private static final String TAG = XYChartView.class.getSimpleName();

	
	protected XYMultipleSeriesRenderer renderer;
	private List<XYSeries> dataset;
	
	protected int width;
	protected int height;
	
	class AxisProperties {
		
		final static int VERTICAL = 0;
		final static int HORIZONTAL = 1;
		
		int type;
		
		public AxisProperties(int type) {
			this.type = type;
			
			// TODO: move color to renderer
			notchLabelPaint.setColor(Color.rgb(0xce, 0xcf, 0xce));
			notchLabelPaint.setTextAlign(Paint.Align.CENTER);
			notchLabelPaint.setAntiAlias(true);
			
			axisLabelPaint.setColor(Color.rgb(0xce, 0xcf, 0xce));
			axisLabelPaint.setTextAlign(Paint.Align.CENTER);
			axisLabelPaint.setAntiAlias(true);
			
			notchPaint.setColor(Color.rgb(0x63, 0x65, 0x63));
			notchPaint.setStyle(Paint.Style.STROKE);
			
			axisPaint.setColor(Color.rgb(0x63, 0x65, 0x63));
			axisPaint.setStyle(Paint.Style.STROKE);
		}
		
		Paint axisPaint = new Paint();
		Paint notchPaint = new Paint();

		Paint notchLabelPaint = new TextPaint();
		Paint axisLabelPaint = new TextPaint();
		
		int axisLabelHeight;
		int notchLabelHeight;
		int notchLabelWidth;
		
		String axisLabelTextExample;
		
		public int width() {
			if (type == HORIZONTAL) {
				return axisLabelHeight + notchLabelHeight + SPACE_BETWEEN_AXIS_AND_LABEL;
			} else {
				return axisLabelHeight + notchLabelWidth + SPACE_BETWEEN_AXIS_AND_LABEL;
			}
		}
		
		public void recalculate() {
			axisLabelHeight = findLabelHeight(axisLabelPaint, axisLabelTextExample);
			notchLabelHeight = findLabelHeight(notchLabelPaint, axisLabelTextExample);
			notchLabelWidth = findLabelWidth(notchLabelPaint, axisLabelTextExample);
		}
		
	}
	
	protected final List<AxisProperties> xAxisPaints = new ArrayList<AxisProperties>();
	protected final List<AxisProperties> yAxisPaints = new ArrayList<AxisProperties>(); 

	protected Paint gridPaint = new Paint();
	protected Paint chartTitlePaint = new TextPaint();

	
	protected TextPaint legendPaint = new TextPaint();

	{	
		chartTitlePaint.setColor(Color.rgb(0xce, 0xcf, 0xce));
		chartTitlePaint.setTextAlign(Paint.Align.CENTER);
		chartTitlePaint.setAntiAlias(true);
		
		gridPaint.setColor(Color.rgb(0x63, 0x65, 0x63));
		gridPaint.setStyle(Paint.Style.STROKE);
				
		legendPaint.setColor(Color.rgb(0xce, 0xcf, 0xce));
		legendPaint.setAntiAlias(true);
		legendPaint.setTextAlign(Paint.Align.LEFT);
	}
	
	protected int oneLegendTitleHeight;
	
	// inner chart boundary (drawing area, demarked by axis)
	// TODO: use Rect clas for boundary
	protected int left;
	protected int top; // graph grid padding from edge of the screen; also, x cord of top X axis 
	protected int right;
	protected int bottom;
	
	
	// outer chart boundary - includes legend, axis titles, axis lables and chart title)
	Rect chartRect;
	
	protected int xAxisLength;
//	protected int labelHeight;
//	protected int labelWidth;
	protected int legendLineLength;

	public XYChartView(Context context, XYMultipleSeriesRenderer renderer, List<XYSeries> dataset) {
		super(context);
		
		this.renderer = renderer;
		this.dataset = dataset;
	}

	public void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
		super.onSizeChanged(width, height, oldWidth, oldHeight);
		this.width = width;
		this.height = height;
		
		legendLineLength = (int)(width * 0.15d);

		final Rect rect = new Rect();
		
		legendPaint.setTextSize(renderer.getLegendTextSize());
		legendPaint.getTextBounds("A text", 0, 6, rect);
		oneLegendTitleHeight = rect.height();
		
		for(int i = 0; i<renderer.getXAxisNum(); i++) {
			xAxisPaints.add(new AxisProperties(AxisProperties.HORIZONTAL));
		}
		for(int i = 0; i<renderer.getYAxisNum(); i++) {
			yAxisPaints.add(new AxisProperties(AxisProperties.VERTICAL));
		}
		
		calculateRendererDependentValues();
	}
	

	protected void calculateRendererDependentValues() {
		chartRect = renderer.getChartPadding();

		
		bottom = height -chartRect.bottom -SPACE_BETWEEN_AXIS_AND_LABEL;
		
		for(int i = 0; i<renderer.getXAxisNum(); i++) {
			final AxisProperties paints = xAxisPaints.get(i);
			final AxisRenderingInfo axisInfo = renderer.getXAxis(i);
			
			paints.axisLabelPaint.setTextSize(axisInfo.getTitleTextSize());
			paints.notchLabelPaint.setTextSize(axisInfo.getNotchTextSize());
			paints.axisPaint.setStrokeWidth(axisInfo.getAxisStrokeWidth());
			paints.axisLabelTextExample = axisInfo.getAxisLabelTextExample();
			
			paints.recalculate();
		}

		for(int i = 0; i<renderer.getYAxisNum(); i++) {
			final AxisProperties paints = yAxisPaints.get(i);
			final AxisRenderingInfo axisInfo = renderer.getYAxis(i);
			
			paints.axisLabelPaint.setTextSize(axisInfo.getAxisLabelTextSize());
			paints.notchLabelPaint.setTextSize(axisInfo.getNotchTextSize());
			paints.axisPaint.setStrokeWidth(axisInfo.getAxisStrokeWidth());
			paints.axisLabelTextExample = axisInfo.getAxisLabelTextExample();

			paints.recalculate();
		}

		chartTitlePaint.setTextSize(renderer.getChartTitleTextSize());

		gridPaint.setStrokeWidth(renderer.getGridStrokeWidth());
		
		top = chartRect.top + findLabelHeight(chartTitlePaint, renderer.getChartTitle()) + SPACE_BETWEEN_AXIS_AND_LABEL;
		left = chartRect.left ;
		
		right = width - chartRect.right;
		
		// Lets check axis clockwise -- first X top axis.
		if (renderer.getXAxisNum() > 1) {
			top = top + xAxisPaints.get(1).width(); 
		}
					
		// then, lets check right most Y axis
		if (renderer.getYAxisNum() > 1) {
			right = right - yAxisPaints.get(1).width();
		}
		
		// then, bottom X axis
		bottom = bottom - xAxisPaints.get(0).width();
		
		// then, left Y axis
		left = left + yAxisPaints.get(0).width();
		
		xAxisLength = right - left;

		bottom = bottom - calculateLegendHeight();

	}

	
	public synchronized void draw(Canvas canvas) {
		Log.d(TAG, "draw");
		
		super.draw(canvas);
		
		calculateRendererDependentValues();

		canvas.save();
        canvas.translate(left, top);
		canvas.clipRect(0, 0, right-left, bottom-top);
		drawData(canvas);
		canvas.restore();

		drawAxis(canvas);

		drawGridAndYAxisNotches(canvas);
		drawGridAndXAxisNotches(canvas);
		
		drawLegend(canvas);
	}

	private void drawAxis(Canvas canvas) {
		
		Path path = new Path();

		AxisProperties axisPaints;
		AxisRenderingInfo axisInfo;

		// top edge
		if (renderer.getXAxisNum() > 1) {
			axisPaints = xAxisPaints.get(1);
			axisInfo = renderer.getXAxis(1);
			
			path.reset();
			path.moveTo(left, top);
			path.lineTo(right, top);			
			canvas.drawTextOnPath(axisInfo.getTitle(), path, 
					0, - axisPaints.width(), 
					axisPaints.axisLabelPaint);

			canvas.drawPath(path, axisPaints.axisPaint);
		}
		
		// right edge
		if (renderer.getYAxisNum() > 1) {
			axisPaints = yAxisPaints.get(1);
			axisInfo = renderer.getYAxis(1);

			path.reset();
			path.moveTo(right, top);
			path.lineTo(right, bottom);			
			canvas.drawTextOnPath(
					axisInfo.getTitle(), path, 
					0, - axisPaints.axisLabelHeight - axisPaints.notchLabelWidth/2 - SPACE_BETWEEN_AXIS_AND_LABEL, 
					axisPaints.axisLabelPaint);
			
			canvas.drawPath(path, axisPaints.axisPaint);
		}
		
		// bottom edge
		axisPaints = xAxisPaints.get(0);
		axisInfo = renderer.getXAxis(0);

		path.reset();
		path.moveTo(left, bottom);
		path.lineTo(right, bottom);			
		canvas.drawTextOnPath(
				axisInfo.getTitle(), path, 
				0, axisPaints.width(), 
				axisPaints.axisLabelPaint);
		
		canvas.drawPath(path, axisPaints.axisPaint);

		// left edge
		axisPaints = yAxisPaints.get(0);
		axisInfo = renderer.getYAxis(0);

		path.reset();
		path.moveTo(left, bottom);
		path.lineTo(left, top);			
		canvas.drawTextOnPath(
				axisInfo.getTitle(), path, 
				0, - axisPaints.axisLabelHeight - axisPaints.notchLabelWidth/2 - SPACE_BETWEEN_AXIS_AND_LABEL, 
				axisPaints.axisLabelPaint);
		
		canvas.drawPath(path, axisPaints.axisPaint);

		final String chartTitle = renderer.getChartTitle();
		if (chartTitle == null) {
			return;
		}
		
		final Rect rect = new Rect();
		chartTitlePaint.getTextBounds(chartTitle, 0, chartTitle.length(), rect);
		
		int graphTitleOffset = left + xAxisLength /2;
		if (width < left + xAxisLength) {
			graphTitleOffset = left + rect.width() /2; // might be 0, but "at least some offset" is better
		}
		
		canvas.drawText(chartTitle, 
				graphTitleOffset, 
				chartRect.top + rect.height(), 
				chartTitlePaint);
	}

	protected void drawGridAndXAxisNotches(Canvas canvas) {

		AxisRenderingInfo axisInfo = renderer.getXAxis(0);
		AxisProperties axisPaints = xAxisPaints.get(0);
		
		int xLabelStep = (int) (axisInfo.range() / axisInfo.getLabelsNum());

		int valueOffset = (int) (Math.round(axisInfo.getMin()) % xLabelStep);
		if (valueOffset != 0) {
			valueOffset = xLabelStep - valueOffset; 
		}
		
		for(int xLabelIndex = 0; xLabelIndex<axisInfo.getLabelsNum(); xLabelIndex++) {	
			final double relativeXLabelValue =  valueOffset + xLabelStep *xLabelIndex;
			
			/* In order to pass absolute time value for label rendering, we need to add it to the minimum,
			 * while when willing to calculate position of the label relative to center of coordinates
			 * we need to use only relative value */
			// TODO: transfer label name extraction logic to AxisRenderingInfo
			final String label = renderer.getXLabel(0, axisInfo.getMin() + relativeXLabelValue);
			
			final int labelX = (int) (left + relativeXLabelValue * xAxisLength / axisInfo.range());
			
			// grid line is perpendicular to X axis.
			canvas.drawLine(labelX, bottom, labelX, top, gridPaint); // grid line

			canvas.drawLine(labelX, bottom, labelX, bottom + axisInfo.getNotchLength(), axisPaints.notchPaint); // bottom notch
			canvas.drawText(label, 
					labelX, bottom + axisPaints.notchLabelHeight, 
					axisPaints.notchLabelPaint); // bottom notch label
		} 
		
		if (renderer.getXAxisNum() == 1) {
			return;
		}
		
		axisInfo = renderer.getXAxis(1);
		axisPaints = xAxisPaints.get(1);
		
		xLabelStep = (int) (axisInfo.range() / axisInfo.getLabelsNum());

		valueOffset = (int) (Math.round(axisInfo.getMin()) % xLabelStep);
		if (valueOffset != 0) {
			valueOffset = xLabelStep - valueOffset; 
		}
		
		for(int xLabelIndex = 0; xLabelIndex<axisInfo.getLabelsNum(); xLabelIndex++) {	
			final double relativeXLabelValue =  valueOffset + xLabelStep *xLabelIndex;
			
			/* In order to pass absolute time value for label rendering, we need to add it to the minimum,
			 * while when willing to calculate position of the label relative to center of coordinates
			 * we need to use only relative value */
			
			// TODO: transfer label name extraction logic to AxisRenderingInfo
			final String label = renderer.getXLabel(1, axisInfo.getMin() + relativeXLabelValue);
			
			final int labelX = (int) (left + relativeXLabelValue * xAxisLength / axisInfo.range());
			
			// grid line is perpendicular to X axis.
			canvas.drawLine(labelX, bottom, labelX, top, gridPaint); // grid line
			
			canvas.drawLine(labelX, top, labelX, top - axisInfo.getNotchLength(), axisPaints.notchPaint); // top notch

			canvas.drawText(label, 
					labelX, top, 
					axisPaints.notchLabelPaint); // top notch label
		} 
//		canvas.drawLine(labelX, top, labelX, top - 3, gridPaint); // top notch

//		canvas.drawText(label, labelX, top -2, labelPaint); // top label
	}	
	
	protected void drawGridAndYAxisNotches(Canvas canvas) {
		final int yAxisLength = bottom - top;

		AxisRenderingInfo axisInfo = renderer.getYAxis(0);
		AxisProperties axisPaints = yAxisPaints.get(0);
		
		for(int yLabel = 0; yLabel<renderer.getYLabels(); yLabel++) {
			final double yLabelValue = axisInfo.getMin() 
				+ axisInfo.range() *yLabel /axisInfo.getLabelsNum();

			// TODO: move this logic to axisInfo
			final String label = renderer.getYLabel(0, yLabelValue);

			final int labelY = top + yAxisLength * (axisInfo.getLabelsNum() - yLabel) /axisInfo.getLabelsNum();
			
			// grid line is perpendicular to Y axis.
			canvas.drawLine(left, labelY, right, labelY, gridPaint); // grid line
			canvas.drawText(label, left - axisPaints.notchLabelWidth/2, labelY, axisPaints.notchLabelPaint); // notch label
			
			canvas.drawLine(left - axisInfo.getNotchLength(), labelY, left, labelY, axisPaints.notchPaint); // left notch
		}

		if (renderer.getYAxisNum() == 1) {
			return;
		}
		
		axisInfo = renderer.getYAxis(1);
		axisPaints = yAxisPaints.get(1);
		
		for(int yLabel = 0; yLabel<renderer.getYLabels(); yLabel++) {
			final double yLabelValue = axisInfo.getMin() 
				+ axisInfo.range() *yLabel /axisInfo.getLabelsNum();

			// TODO: move this logic to axisInfo
			final String label = renderer.getYLabel(0, yLabelValue);

			final int labelY = top + yAxisLength * (axisInfo.getLabelsNum() - yLabel) /axisInfo.getLabelsNum();
			
			// grid line is perpendicular to Y axis.
			canvas.drawLine(left, labelY, right, labelY, gridPaint); // grid line
			canvas.drawText(label, right, labelY, axisPaints.notchLabelPaint); // notch label
			
			canvas.drawLine(right, labelY, right + axisInfo.getNotchLength(), labelY, axisPaints.notchPaint); // right notch
		}		
	}

	private void drawData(Canvas canvas) {
		Paint seriesPaint = new Paint();
		
		for(XYSeries series : dataset) {
			if (series.getItemCount() == 0) {
				continue;
			}
			seriesPaint.setColor(series.getColor());
			seriesPaint.setAntiAlias(true);
			
			canvas.drawCircle(
					(float)translateX(series.getX(0)), 
					(float)translateY(series, 0), 
					series.getSymbolSize(), seriesPaint);								
						
			seriesPaint.setStrokeWidth(series.getStrokeWidth());
			for(int i = 1; i<series.getItemCount(); i++) {
				seriesPaint.setStyle(Paint.Style.STROKE);
				
				canvas.drawLine(
						translateX(series.getX(i -1)), translateY(series, i-1),
						translateX(series.getX(i)), translateY(series, i),
						seriesPaint);
				seriesPaint.setStyle(Paint.Style.FILL);
				
				canvas.drawCircle(
						(float)translateX(series.getX(i)), 
						(float)translateY(series, i), 
						series.getSymbolSize(), seriesPaint);								
			}
		}
	}

	private float translateX(double x) {
		return (float) ((x - renderer.getXAxisMin())
			/(renderer.getXAxisMax() - renderer.getXAxisMin())
			* xAxisLength);
	}

	private float translateY(XYSeries series, int i) {
		final double range = (series.getYMax() - series.getYMin()) ;
		
		return (float)((series.getYMax() - series.getY(i))
				/range)
				* (bottom - top);
	}
	
	private int drawLegend(Canvas canvas) { 				
		int legendCellX = left;
		int legendCellY = bottom 
			+ xAxisPaints.get(0).width() 
			+ oneLegendTitleHeight + LEGEND_VERTICAL_SPACING;
		
		for (int legendIndex = 0; legendIndex<dataset.size(); legendIndex++) {		
			final XYSeries series = dataset.get(legendIndex);
			 
			final int legendCellWidth = (int) (legendLineLength 
				+ LEGEND_SERIES_TITLE_AND_LINE_SPACE 
				+ legendPaint.measureText(series.getTitle()));
			
			if (legendCellX + legendCellWidth > width) {
				legendCellX = left;
				legendCellY += oneLegendTitleHeight + LEGEND_VERTICAL_SPACING;
			}			
			
			if (canvas != null) {
				drawLegendCell(canvas, legendCellX, legendCellY, series);
			}
			
			legendCellX += legendCellWidth + LEGEND_CELL_SPACING;
		}
		return legendCellY;
	}
	
	public int calculateLegendHeight() {
		return drawLegend(null) 
		- bottom 
		- xAxisPaints.get(0).width();
	}

	private void drawLegendCell(Canvas canvas, int legendCellX,
			int legendCellY, final XYSeries series) {
		legendPaint.setStyle(Paint.Style.STROKE);
		legendPaint.setColor(series.getColor());

		final int legendLineY = legendCellY - oneLegendTitleHeight/2 +  LEGEND_VERTICAL_SPACING/2;
		canvas.drawLine(
				legendCellX, legendLineY, 
				legendCellX + legendLineLength, legendLineY, legendPaint);

		legendPaint.setStyle(Paint.Style.FILL);
		canvas.drawText(series.getTitle(), 
				legendCellX + LEGEND_SERIES_TITLE_AND_LINE_SPACE + legendLineLength, legendCellY, 
				legendPaint);
	}
	
	public int getChartWidth() {
		return xAxisLength;
	}

	public void setDataset(List<XYSeries> dataset) {
		this.dataset = dataset;
	}

	public List<XYSeries> getDataset() {
		return dataset;
	}
	
	
	final Rect tmpRect = new Rect();

	private int findLabelHeight(Paint paint, String text) {
		paint.getTextBounds(text, 0, text.length(), tmpRect);
		return  tmpRect.height();
	}
	
	private int findLabelWidth(Paint paint, String text) {
		paint.getTextBounds(text, 0, text.length(), tmpRect);
		return  tmpRect.width();
	}


} 
