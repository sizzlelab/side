package eu.mobileguild.graphs;

import java.text.SimpleDateFormat;
import java.util.Date;

import eu.mobileguild.patterns.NamingStrategyForDoubleParameter;

public class DateNamingStrategyForDoubleParameter implements
		NamingStrategyForDoubleParameter {

	final SimpleDateFormat format;
	
	final Date container = new Date();
	
	public DateNamingStrategyForDoubleParameter(String format) {
		this.format = new SimpleDateFormat(format);
		
	}
	
	@Override
	public String getName(double param) {
		container.setTime((long)param);
		
		return format.format(container);
	}
}
