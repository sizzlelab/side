package eu.mobileguild.ui;

import java.util.List;
import java.util.WeakHashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class ListItemWithTextFieldAndCheckboxAdapter extends BaseAdapter {
    
	List<Object[]> list;
	private Context context;
	
    private int layout;
    
    private LayoutInflater inflater;

    /**
     * A list of View ids representing the views to which the data must be bound.
     * This field should be made private, so it is hidden from the SDK.
     * {@hide}
     */
    protected int[] to;

    protected final WeakHashMap<View, View[]> holders = new WeakHashMap<View, View[]>();
	
	public ListItemWithTextFieldAndCheckboxAdapter(Context context, int layout, int[] to, List<Object[]> list) {
		this.to = to;
		this.layout = layout;
		this.context = context;
		
		assert list.size() == 0 || list.get(0).length == to.length;
		this.list = list;
		
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        super.notifyDataSetChanged();

	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v;
        if (convertView == null) {
            v = newView(context, parent);
        } else {
            v = convertView;
        }
        bindView(v, context, list.get(position));
        return v;
    }

	public void bindView(View view, Context context, Object[] from) {
        final View[] holder = holders.get(view);

        for (int i = 0; i < to.length -1; i++) {
            final View v = holder[i];
            if (v != null) {

                String text = (String)from[i];
                if (text == null) {
                    text = "";
                }
                ((TextView) v).setText(text);
            }
            
            final CheckBox checkbox = (CheckBox) holder[to.length];
            checkbox.setEnabled((Boolean)from[i]);
        }
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
    
    public void addItem(String[] item) {
    	list.add(item);
        super.notifyDataSetChanged();
    }
}
