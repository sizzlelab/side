package eu.mobileguild.ui;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;

public class ListItemOnClickListener implements OnClickListener {
	// TODO: could be rewritten to hold a view -> <parent, position, id> map, 
	// so that too many objects wouldn't be created;
	
	
	private android.widget.AdapterView.OnItemClickListener listener;
	private AdapterView<?> parent;
	private int position;
	private long id;

	public ListItemOnClickListener(AdapterView.OnItemClickListener listener, AdapterView<?> parent, int position, long id) {
		this.listener = listener;
		this.parent = parent;
		this.position = position;
		this.id = id;
	}
	
	@Override
	public void onClick(View view) {
		listener.onItemClick(parent, view, position, id);
	}
}
