package eu.mobileguild.utils;

import java.util.Arrays;



public class SettingUtils {

	final static StringBuffer buffer = new StringBuffer();
 
	
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
	
	
	
	public static String getStarsForPassword(String password) {
		
		int oldLength = buffer.length();
		
		for(; oldLength < password.length();oldLength++) {
			buffer.append('*');
		}
		
		buffer.setLength(password.length());
		
		return buffer.toString();
	}

}
