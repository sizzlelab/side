package fi.hut.soberit.sensors.sessions;

import java.util.Date;

import org.apache.commons.lang.time.DateUtils;


import eu.mobileguild.ApplicationWithGlobalPreferences;
import eu.mobileguild.ui.SectionedAdapter;
import fi.hut.soberit.sensors.DatabaseHelper;
import fi.hut.soberit.sensors.R;
import fi.hut.soberit.sensors.SessionDao;
import fi.hut.soberit.sensors.SessionsTable;

import android.app.ListActivity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

public class SessionsList extends ListActivity {

	SessionDao sessionsDao;

	private CustomSectionedAdapter customSectionedAdapter;

	private Cursor cursor;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		

		sessionsDao = new SessionDao(new DatabaseHelper(this));
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
		protected View getRowView(View convertView, ViewGroup parent) {

			TextView result = (TextView) convertView;

			if (convertView == null) {
				result = (TextView) getLayoutInflater().inflate(
						android.R.layout.simple_list_item_1, null);
			}
			
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
			
			String dayLabel;
			
			if (end == null) {
				dayLabel = String.format("%1$tH:%1$tM", start);
			} else if(!DateUtils.isSameDay(start, end)) {
				dayLabel = String.format("%1$tH:%1$tM - %2$tb %2$te, %2$tH:%2$tM",
						start, end);
			} else {
				dayLabel = String.format("%1$tH:%1$tM - %2$tH:%2$tM",
						start, end);				
			}
				
			((TextView)view).setText(dayLabel);
		}
	};	
}
