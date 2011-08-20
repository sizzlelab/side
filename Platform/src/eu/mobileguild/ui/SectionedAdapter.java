/*******************************************************************************
 * Copyright (c) 2011 Aalto University
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package eu.mobileguild.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;


abstract public class SectionedAdapter extends BaseAdapter {
	abstract protected View getHeaderView(String caption, int index,
			View convertView, ViewGroup parent);

	protected List<Section> sections = new ArrayList<Section>();
	public boolean dataValid;
	private boolean autoRequery;
	private Cursor cursor;

	private Context context;
	private int rowIDColumn;
	private ChangeObserver changeObserver;
	private DataSetObserver dataSetObserver;
	
	private static int TYPE_SECTION_HEADER = 0;
	private static int TYPE_SECTION_ROW = 1;

	protected String idColumnName = "_id";
	
	public SectionedAdapter(Context context, Cursor c, boolean autoRequiry, String idColumnName) {
		this.idColumnName = idColumnName;
		init(context, c, autoRequiry);
		calculateSections(context, c);
		c.registerDataSetObserver(new MyDataSetObserver());
	}
	
    protected void init(Context context, Cursor c, boolean autoRequery) {
        boolean cursorPresent = c != null;
        this.autoRequery = autoRequery;
        cursor = c;
        dataValid = cursorPresent;
        this.context = context;
        rowIDColumn = cursorPresent ? c.getColumnIndexOrThrow(idColumnName) : -1;
        changeObserver = new ChangeObserver();
        dataSetObserver = new MyDataSetObserver();
        if (cursorPresent) {
            c.registerContentObserver(changeObserver);
            c.registerDataSetObserver(dataSetObserver);
        }
    }

	public Object getItem(int position) {
        if (!dataValid || getCursor() == null ) {
        	return null;        	
        }
        
		int positionGauge = position;
		for (int i = 0; i<sections.size(); i++) {
			final Section section = sections.get(i);
		
			if (positionGauge == 0) {
				return (section);
			}

			int size = section.size + 1;

			if (positionGauge < size) {
				if (dataValid && cursor != null) {
		            cursor.moveToPosition(position - (i+1));
		            return cursor;
		        } else {
		            return null;
		        }
			}

			positionGauge -= size;
		}

		return (null);
	}

	private Object getCursor() {
		return cursor;
	}

	public int getCount() {
		if (dataValid && cursor != null) {
			return cursor.getCount() + sections.size();
		} else {
			return 0;
		}
	}

	public int getViewTypeCount() {
		return super.getViewTypeCount() + 1; // 1 for header
	}

	public int getItemViewType(int position) {
		for (Section section : this.sections) {
			if (position == 0) {
				return (TYPE_SECTION_HEADER);
			}

			int size = section.size + 1;

			if (position < size) {
				return TYPE_SECTION_ROW;
			}

			position -= size;
		}

		return (-1);
	}

	public boolean areAllItemsSelectable() {
		return (false);
	}

	public boolean isEnabled(int position) {
		return (getItemViewType(position) != TYPE_SECTION_HEADER);
	}

	protected abstract View getRowView( 
			View convertView,
			ViewGroup parent);
	
	@Override
	public long getItemId(int position) {
        if (!dataValid || getCursor() == null ) {
        	return 0;        	
        }
        
		int positionGauge = position;
		for (int i = 0; i<sections.size(); i++) {
			final Section section = sections.get(i);

			if (positionGauge == 0) {
				return -2;
			}

			int size = section.size + 1;

			if (positionGauge < size) {
	            if (cursor.moveToPosition(position - (i+1))) {
	                return cursor.getLong(rowIDColumn);
	            } else {
	                return 0;
	            }
			}

			positionGauge -= size;
		}
		
		throw new RuntimeException("shouldn't happen");
	}

	@Override
    public boolean hasStableIds() {
        return true;
    }

	protected abstract void calculateSections(Context context, Cursor cursor);
	
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return getRowView(null, parent);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		
		int positionGauge = position;
		for (int i = 0; i<sections.size(); i++) {
			final Section section = sections.get(i);
			
			if (positionGauge == 0) {
				return getHeaderView(section.caption, i, null, parent);
			}

			int size = section.size + 1;

			if (positionGauge < size) {
				return getViewCopied(position - (i +1), convertView, parent);
			}

			positionGauge -= size;
		}
		return null;
	}
	
	public View getViewCopied(int position, View convertView, ViewGroup parent) {
        if (!dataValid) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }
        if (!cursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }
        View v;
        if (convertView == null) {
            v = newView(context, cursor, parent);
        } else {
            v = convertView;
        }
        bindView(v, context, cursor);
        return v;
    }
	
	public void changeCursor(Cursor cursor) {
        if (this.cursor == cursor) {
            return;
        }
        if (cursor != null) {
            cursor.unregisterContentObserver(changeObserver);
            cursor.unregisterDataSetObserver(dataSetObserver);
            cursor.close();
        }
        this.cursor = cursor;
        if (cursor != null) {
            cursor.registerContentObserver(changeObserver);
            cursor.registerDataSetObserver(dataSetObserver);
            rowIDColumn = cursor.getColumnIndexOrThrow(idColumnName);
            dataValid = true;
            // notify the observers about the new cursor
            notifyDataSetChanged();
        } else {
            rowIDColumn = -1;
            dataValid = false;
            // notify the observers about the lack of a data set
            notifyDataSetInvalidated();
        }
    }
	
	public abstract void bindView(View view, Context context, Cursor cursor);
	
	public class Section {
		String caption;
		int size;

		public Section(String caption, int size) {
			this.caption = caption;
			this.size = size;
		}
	}

	
	protected void onContentChanged() {
        if (autoRequery && cursor != null && !cursor.isClosed()) {
            dataValid = cursor.requery();
        }
    }

    private class ChangeObserver extends ContentObserver {
        public ChangeObserver() {
            super(new Handler());
        }

        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
            onContentChanged();
        }
    }

    private class MyDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            dataValid = true;
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            dataValid = false;
            notifyDataSetInvalidated();
        }
    }
    
	public void setCursor(Cursor cursor) {
		this.cursor = cursor;
	}
}
