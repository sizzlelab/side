package eu.mobileguild;

import java.util.Locale;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Configuration;

public abstract class ApplicationWithChangingLocale extends Application {
    private Locale locale = null;

	private final String localePreferenceName;

	private String appPreferenceFile;

	protected ApplicationWithChangingLocale(String appPreferenceFile, String localPreferenceName) {
    	this.appPreferenceFile = appPreferenceFile;
    	this.localePreferenceName  = localPreferenceName;
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        if (locale != null)
        {
            newConfig.locale = locale;
            Locale.setDefault(locale);
            getBaseContext().getResources().updateConfiguration(newConfig, getBaseContext().getResources().getDisplayMetrics());
        }
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        
        SharedPreferences settings = getSharedPreferences(appPreferenceFile, MODE_PRIVATE);

        Configuration config = getBaseContext().getResources().getConfiguration();

        String lang = settings.getString(localePreferenceName, "");
        if (! "".equals(lang) && ! config.locale.getLanguage().equals(lang))
        {
            locale = new Locale(lang);
            Locale.setDefault(locale);
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
    }
    

    public void setLocale(Locale locale) {
		this.locale = locale;
	}
}