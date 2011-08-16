package fi.hut.soberit.manager.uploaders;

import java.util.List;
import java.util.WeakHashMap;

import fi.hut.soberit.sensors.generic.ObservationType;
import fi.hut.soberit.sensors.generic.UploadedType;
import fi.hut.soberit.sensors.generic.Uploader;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class AvailableUploaderListAdapter extends BaseAdapter {
    
	List<Uploader> list;
	private Context context;
	
    private int layout;
    
    private LayoutInflater inflater;
    
    protected final WeakHashMap<View, View[]> holders = new WeakHashMap<View, View[]>();
	private int [] to = new int [] {
		android.R.id.title,
		android.R.id.summary,
		android.R.id.checkbox
	};
    
	public AvailableUploaderListAdapter(Context context, int layout, List<Uploader> list) {
		this.layout = layout;
		this.context = context;
		
		this.list = list;

		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        super.notifyDataSetChanged();

	}

	public List<Uploader> getList() {
		return list;
	}

	public void setList(List<Uploader> list) {
		this.list = list;
	}


	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Uploader getItem(int position) {

		return list.get(position);
	}

	@Override
	public long getItemId(int position) {

		return list.get(position).getId();
	}

	static final StringBuilder builder = new StringBuilder();
	
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v;
        if (convertView == null) {
            v = newView(context, parent);

        } else {
            v = convertView;
        }

        final View[] holder = holders.get(v);
        final Uploader from = getItem(position);
        
        ((TextView)holder[0]).setText(from.getName());
        
        builder.setLength(0);
        for(UploadedType type: from.getUploadedTypes()) {
        	if (builder.length() != 0) {
            	builder.append("\n");
        	}
        	builder.append(type.getMimeType());
        }
        
        ((TextView)holder[1]).setText(builder.toString());
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

	public void addItem(Uploader type) {
		list.add(type);
        super.notifyDataSetChanged();
	}

	public Uploader toggeEnabled(int position) {
		final Uploader type = list.get(position);
		type.setEnabled(!type.isEnabled());
		super.notifyDataSetInvalidated();
		
		return type;
	}
}
