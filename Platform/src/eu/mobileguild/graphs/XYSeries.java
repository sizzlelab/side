package eu.mobileguild.graphs;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class XYSeries implements Parcelable {

	private static final int DEFAULT_SYMBOL_SIZE = 3;

	private String name;

	List<Double[]> series = new ArrayList<Double[]>();

	private int color;

	protected double yMax;
	
	protected double yMin;

	private int strokeWidth;
	
	public XYSeries(String name, int color) {
		this.name = name;
		this.setColor(color);
	}
	
	public XYSeries(String name, int color, int strokeWidth, double defaultYMin, double defaultYMax) {
		this.name = name;
		this.setColor(color);
		
		this.strokeWidth = strokeWidth;
		
		this.yMin = defaultYMin;
		this.yMax = defaultYMax;
	}

	public XYSeries(Parcel source) {
		this.name = source.readString();
		this.setColor(source.readInt());

		yMin = source.readDouble();
		yMax = source.readDouble();
		
		final int size = source.readInt();

		
		for (int i = 0; i<size; i++) {
			final Double [] cords = new Double[2];
			cords[0] = source.readDouble();
			cords[1] = source.readDouble();

			series.add(cords);
		}
	}

	public void add(double x, double y) {
		series.add(new Double[] {x, y});
	}

	public double getX(int i) {
		return series.get(i)[0];
	}

	public double getY(int i) {
		return series.get(i)[1];
	}

	public int getItemCount() {
		return series.size();
	}

	public String getTitle() {
		return name;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public int getColor() {
		return color;
	}
	
    public static final Parcelable.Creator<XYSeries> CREATOR
    	= new Parcelable.Creator<XYSeries>() {

    	@Override
        public XYSeries[] newArray(int size) {
            return new XYSeries[size];
        }

		@Override
		public XYSeries createFromParcel(Parcel source) {
			return new XYSeries(source);
		}
    };

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeInt(color);
		
		dest.writeDouble(yMin);
		dest.writeDouble(yMax);
		
		dest.writeInt(series.size());
		
		for(int i = 0; i<series.size(); i++) {
			dest.writeDouble(getX(i));
			dest.writeDouble(getY(i));

		}		
	}

	public double getYMin() {
		return yMin;
	}

	public double getYMax() {
		return yMax;
	}

	public void setYMin(double seriesYMin) {
		this.yMin = seriesYMin;		
	}
	
	public void setYMax(double seriesYMax) {
		this.yMax = seriesYMax;		
	}

	public int getStrokeWidth() {
		return strokeWidth;
	}
	
	public Object clone() {
		final XYSeries obj = new XYSeries(name, color, strokeWidth, yMin, yMax);
		
		obj.setSeries(series);
		
		return obj;
	}

	public List<Double[]> getSeries() {
		return series;
	}

	public void setSeries(List<Double[]> series) {
		this.series = series;
	}

	public float getSymbolSize() {
		return DEFAULT_SYMBOL_SIZE;
	}
}
