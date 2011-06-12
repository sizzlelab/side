package fi.hut.soberit.manager.drivers;

import java.util.List;
import java.util.WeakHashMap;

import fi.hut.soberit.sensors.generic.ObservationType;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class AvailableObservationTypeListAdapter extends BaseAdapter {
    
	List<ObservationType> list;
	private Context context;
	
    private int layout;
    
    private LayoutInflater inflater;
    
    protected final WeakHashMap<View, View[]> holders = new WeakHashMap<View, View[]>();
	private int [] to = new int [] {
		android.R.id.title,
		android.R.id.summary,
		android.R.id.checkbox		
	};
    
	public AvailableObservationTypeListAdapter(Context context, int layout, List<ObservationType> list) {
		this.layout = layout;
		this.context = context;
		
		this.list = list;

		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        super.notifyDataSetChanged();

	}

	public List<ObservationType> getList() {
		return list;
	}

	public void setList(List<ObservationType> list) {
		this.list = list;
	}


	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public ObservationType getItem(int position) {

		return list.get(position);
	}

	@Override
	public long getItemId(int position) {

		return list.get(position).getId();
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
        final ObservationType from = getItem(position);
        
        ((TextView)holder[0]).setText(from.getName());
        ((TextView)holder[1]).setText(from.getMimeType());
        ((CheckBox)holder[2]).setChecked(from.isEnabled());
        
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
		list.clear();	
        super.notifyDataSetChanged();

	}

	public void addItem(ObservationType type) {
		list.add(type);
        super.notifyDataSetChanged();
	}

	public ObservationType toggeEnabled(int position) {
		final ObservationType type = list.get(position);
		type.setEnabled(!type.isEnabled());
		super.notifyDataSetInvalidated();
		
		return type;
	}
}
