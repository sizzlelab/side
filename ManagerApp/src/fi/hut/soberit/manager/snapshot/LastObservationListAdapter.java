/*******************************************************************************
 * Copyright (c) 2011 Aalto University
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 * Authors:
 * Maksim Golivkin <maksim@golivkin.eu>
 ******************************************************************************/
package fi.hut.soberit.manager.snapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import fi.hut.soberit.sensors.DriverInterface;
import fi.hut.soberit.sensors.generic.GenericObservation;
import fi.hut.soberit.sensors.generic.ObservationKeyname;
import fi.hut.soberit.sensors.generic.ObservationType;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class LastObservationListAdapter extends BaseAdapter {
    
	private Context context;
	
    private int layout;
    
    private LayoutInflater inflater;
    
	private Map<Long, GenericObservation> values;
	private List<ObservationType> types;
	private boolean[] selected;
        
    protected final WeakHashMap<View, View[]> holders = new WeakHashMap<View, View[]>();
	    
    private int [] to = new int [] {
		android.R.id.title,
		android.R.id.summary,
		android.R.id.checkbox		
	};

	public LastObservationListAdapter(Context context, int layout, List<ObservationType> types, Map<Long, GenericObservation> values, boolean [] selected) {
		this.layout = layout;
		this.context = context;
		
		this.types = types;
		this.values = values;
		this.selected = selected;

		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        super.notifyDataSetChanged();

	}

	@Override
	public int getCount() {
		return types.size();
	}

	public void toggleSelected(int position) {

		selected[position] = !selected[position];
	}

	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v;
        if (convertView == null) {
            v = newView(context, parent);
        } else {
            v = convertView;
        }

        final View[] holder = holders.get(v);
        final ObservationType type = types.get(position);
        
        final StringBuilder builder = new StringBuilder();
        
        if (values.get(type.getId()) != null) {
            final GenericObservation value = values.get(type.getId());
                                	
            int bytePos = 0;
			for(ObservationKeyname keyname: type.getKeynames()) {
				
	        	if (DriverInterface.KEYNAME_DATATYPE_FLOAT.equals(keyname.getDatatype())) {
					builder.append(value.getFloat(bytePos));
	        		bytePos += 4;

	        	} else if(DriverInterface.KEYNAME_DATATYPE_INTEGER.equals(keyname.getDatatype())) {
	        		builder.append(value.getInteger(bytePos));
	        		bytePos += 4;
	        	}
				builder.append(" ");
				builder.append(keyname.getUnit());
				builder.append("; ");
	        }
        }
        
        ((TextView)holder[0]).setText(type.getName());
        ((TextView)holder[1]).setText(builder.substring(0, Math.max(0, builder.length() - 2)));
        ((CheckBox)holder[2]).setChecked(selected[position]);
        
        return v;
    }
	
    public View newView(Context context, ViewGroup parent) {
    	final View view = inflater.inflate(layout, parent, false);
        return generateViewHolder(view);
    }
    
    private View generateViewHolder(View v) {
        final int count = to.length;
        final View[] holder = new View[count];

        for (int i = 0; i < count; i++) {
            holder[i] = v.findViewById(to[i]);
        }
        holders.put(v, holder);

        return v;
    }
    
	public void clear() {
		types.clear();
		getValues().clear();
        super.notifyDataSetChanged();
	}

	public void addItem(ObservationType type, GenericObservation value) {
		types.add(type);
		values.put(type.getId(), value);
        super.notifyDataSetChanged();
	}

	@Override
	public ObservationType getItem(int position) {
		return types.get(position);
	}

	@Override
	public long getItemId(int position) {
		return types.get(position).getId();
	}

	public void setValues(Map<Long, GenericObservation> values) {
		this.values = values;
	}

	public Map<Long, GenericObservation> getValues() {
		return values;
	}
	
	public List<ObservationType> getSelectedTypes() {
		
		List<ObservationType> types = new ArrayList<ObservationType>();
		
		for(int i = 0; i<selected.length; i++) {
			if (selected[i]) {
				types.add(this.types.get(i));
			}
		}
		
		return types;		
	}
}
