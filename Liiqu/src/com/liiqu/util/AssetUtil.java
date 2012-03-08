package com.liiqu.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

public class AssetUtil {
	
	public static String readAssetsFile(Context context, String filename) {
		final StringBuilder builder = new StringBuilder();
		
		final AssetManager assets = context.getAssets();
		
        try {
			final LineNumberReader reader = new LineNumberReader(new InputStreamReader(assets.open(filename)));

			String tmp = null;
			while((tmp = reader.readLine()) != null) {
				builder.append(tmp);
			}
			
			reader.close();
        } catch (IOException e) {
			e.printStackTrace();
		}
        
		return builder.toString();
	}
}
