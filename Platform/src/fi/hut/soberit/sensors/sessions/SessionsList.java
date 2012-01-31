/*******************************************************************************
 * Copyright (c) 2011 Aalto University
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package fi.hut.soberit.sensors.sessions;

import java.util.Date;

import org.apache.commons.lang.time.DateUtils;

import android.app.ListActivity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import eu.mobileguild.ui.SectionedAdapter;
import fi.hut.soberit.sensors.DatabaseHelper;
import fi.hut.soberit.sensors.R;
import fi.hut.soberit.sensors.SessionDao;
import fi.hut.soberit.sensors.SessionsTable;

public class SessionsList extends ListActivity  {

	SessionDao sessionsDao;

	protected CustomSectionedAdapter customSectionedAdapter;

	protected Cursor cursor;

	public boolean uploadVisible = false;
	
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		sessionsDao = new SessionDao(new DatabaseHelper(this));
		
		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
	}

	@Override
	public void onResume() {
		super.onResume();
		

		cursor = sessionsDao.getSessions();
		customSectionedAdapter = new CustomSectionedAdapter(this, cursor, true);
		setListAdapter(customSectionedAdapter);

		startManagingCursor(cursor);	
	}

	
	@Override
	public void onPause() {
		super.onPause();		
	}
	
	class CustomSectionedAdapter extends SectionedAdapter {		
		
		public CustomSectionedAdapter(Context context, Cursor c,
				boolean autoRequiry) {
			super(context, c, autoRequiry, SessionsTable.SESSION_ID);
			
			
		}

		@Override
		protected View getHeaderView(String caption, int index,
				View convertView, ViewGroup parent) {
			TextView result = (TextView) convertView;

			if (convertView == null) {
				result = (TextView) getLayoutInflater().inflate(
						R.layout.sessions_header, null);
			}

			result.setText(caption);

			return (result);
		}

		@Override
		protected View getRowView(View convertView, int position, ViewGroup parent) {

			ViewGroup result = (ViewGroup) convertView;

			if (convertView == null) {
				result = (ViewGroup) getLayoutInflater().inflate(
						R.layout.sessions_item, null);
			}
			

			result.findViewById(android.R.id.checkbox).setVisibility(uploadVisible ? View.VISIBLE : View.GONE);			
			
			return result;
		}

		@Override
		protected void calculateSections(Context context, Cursor cursor) {
			Date lastDay = null;
			
			int lastSectionSize = 0;
					
			String lastSectionTitle = null;
			
			int initialPosition = cursor.getPosition();
			for(int i = 0; i<cursor.getCount(); i++) {
				cursor.moveToPosition(i);
				final Date start = DatabaseHelper.getDateFromUtcDateString(
						cursor.getString(cursor.getColumnIndex(SessionsTable.START)));
				
				if (lastDay == null || !DateUtils.isSameDay(start, lastDay)) {
					lastDay = start;
					if (lastSectionTitle != null) {
						sections.add(new Section(lastSectionTitle, lastSectionSize));
					}
					
					lastSectionSize = 0;
					lastSectionTitle = android.text.format.DateUtils.formatDateTime(
							context, 
							start.getTime(), 
							android.text.format.DateUtils.FORMAT_SHOW_DATE | 
							android.text.format.DateUtils.FORMAT_SHOW_YEAR
						);
				}
				lastSectionSize++;
			}
			if (lastDay != null) {
				sections.add(new Section(lastSectionTitle, lastSectionSize));
			}
			
			cursor.moveToPosition(initialPosition);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {		
			
			final Date start = DatabaseHelper.getDateFromUtcDateString(
					cursor.getString(cursor.getColumnIndex(SessionsTable.START)));
			final String endString = cursor.getString(cursor.getColumnIndex(SessionsTable.END));
			final Date end = endString != null 
				? DatabaseHelper.getDateFromUtcDateString(endString)
				: null;	
			
			final String name = cursor.getString(cursor.getColumnIndex(SessionsTable.NAME)); 
				
			String dayLabel;
			
			if (end == null) {
				dayLabel = String.format("%1$tH:%1$tM - ...", start);
			} else if(!DateUtils.isSameDay(start, end)) {
				dayLabel = String.format("%1$tH:%1$tM - %2$tb %2$te, %2$tH:%2$tM",
						start, end);
			} else if (Math.round(start.getTime() / DateUtils.MILLIS_PER_MINUTE) == Math.round(end.getTime() / DateUtils.MILLIS_PER_MINUTE)) {
				dayLabel = String.format("%1$tH:%1$tM", start);
			} else {
				dayLabel = String.format("%1$tH:%1$tM - %2$tH:%2$tM",
						start, end);				
			}
				
			final CheckBox uploaded = (CheckBox) view.findViewById(android.R.id.checkbox);
			
			uploaded.setChecked(cursor.getInt(cursor.getColumnIndex(SessionsTable.UPLOADED)) == 1);
			
			final TextView label = (TextView)view.findViewById(android.R.id.text1);
			
			label.setText((name != null ? name + " " : "") + dayLabel);
		}
	}
}
