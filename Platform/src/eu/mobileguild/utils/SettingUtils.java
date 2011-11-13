package eu.mobileguild.utils;

import java.util.Arrays;



public class SettingUtils {

	
	/**
	 * Method to set ListPreference summary title
	 * @param entries android:entries
	 * @param entries
	 * @param defaultValueId
	 * @return
	 */
	
	public static String getEntryName(String[] entries, String[] entryValues, String defaultValue) {
		int entry = Arrays.binarySearch(entryValues, defaultValue);
		
		return entries[entry];		
	}
}
