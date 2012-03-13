package fi.hut.soberit.sensors.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteException;
import eu.mobileguild.db.MGDatabaseHelper;
import fi.hut.soberit.sensors.DatabaseHelper;
import fi.hut.soberit.sensors.SessionDao;

public class SessionHelper {
	
	final static String DEFAULT_SESSION_PREF_NAME = "session_id";

	private static final String DEFAULT_PREFS_FILENAME = "default.preferences";

	// defines whenever new sessions has to be started with new activity
	protected static boolean startNewSession = false;

	protected static boolean registerInDatabase = true;

	protected String sessionName;

	private MGDatabaseHelper dbHelper;

	private String prefsFilename = DEFAULT_PREFS_FILENAME;

	private Context context;

	private SessionDao sessionDao;

	private String sessionIdPreferenceName  = DEFAULT_SESSION_PREF_NAME;;

	private long lastSessionUpdate;

	public SessionHelper(Context context, MGDatabaseHelper dbHelper) {
		this.context = context;
		this.dbHelper = dbHelper;
	}
	
	public void setStartNewSession(boolean startNewSession) {
		this.startNewSession = startNewSession;
	}
	
	public boolean isStartNewSession() {
		return startNewSession;
	}
	
	public void setRegisterInDatabase(boolean doRegister) {
		this.registerInDatabase = doRegister;
		
		if (!registerInDatabase) {
			return;
		}
		
		sessionDao = new SessionDao(dbHelper);
		
	}
	
	public boolean isRegisterInDatabase() {
		return registerInDatabase;
	}
	
	public void setSessionName(String name) {
		this.sessionName = name;
	}
	
	public void setPreferencesFilename(String prefsFilename) {
		this.prefsFilename = prefsFilename;
	}
	
	public void setSessionIdPreference(String sessionIdPreferenceName) {
		this.sessionIdPreferenceName = sessionIdPreferenceName;
	}

	public void startSession(long sessionId) {
		final SharedPreferences prefs = context.getSharedPreferences(
				prefsFilename, 
				Context.MODE_PRIVATE);

		if (registerInDatabase) {
			sessionId = sessionDao.insertSession(sessionName, System.currentTimeMillis());
		}
		
		final Editor editor = prefs.edit();
		editor.putLong(sessionIdPreferenceName, sessionId);
		editor.commit();
	}
	
	public void startSession() {
		startSession(1);
	}

	public Long getSessionId() {
		final SharedPreferences prefs = context.getSharedPreferences(
				prefsFilename, 
				Context.MODE_PRIVATE);
		long sessionId = prefs.getLong(sessionIdPreferenceName, -1);
		return sessionId == -1 ? null : sessionId;
	}
	
	public boolean hasStarted() {
		return getSessionId() != null;
	}

	public void stopSession() {
		if (registerInDatabase) {
			sessionDao.updateSession(getSessionId(), System.currentTimeMillis());
		}
		
		final SharedPreferences prefs = context.getSharedPreferences(
				this.prefsFilename, 
				Context.MODE_PRIVATE);
		
		final Editor editor = prefs.edit();
		editor.remove(sessionIdPreferenceName);
		editor.commit();		
	}

	public void updateSession() {		
		final long now = System.currentTimeMillis();
		
		if (now - lastSessionUpdate   < 60*1000) {
			return;
		}
		
		lastSessionUpdate = now;
		
		if (registerInDatabase) {
			sessionDao.updateSession(getSessionId(), now);
		}
	}
	
	public void destroySession() {
		if (registerInDatabase) {
			return;
		}
		
		final long sessionId = getSessionId();
		
		final SharedPreferences prefs = context.getSharedPreferences(
				this.prefsFilename, 
				Context.MODE_PRIVATE);
		
		final Editor editor = prefs.edit();
		editor.remove(sessionIdPreferenceName);
		editor.commit();
		
		new Thread(new Runnable() {
			public void run() {
				while(true) {
					try {
						if (sessionDao.delete(sessionId) > 0) {
							dbHelper.closeDatabases();
							return;
						}
					} catch(SQLiteException e) {
						
					}
						
					try {
						Thread.currentThread().sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
						return;
					}
				}
			}
		}).start();
	}
}
