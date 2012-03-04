package com.liiqu;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.webkit.WebView;

import com.github.droidfu.adapters.WebGalleryAdapter;
import com.github.droidfu.cachefu.CacheHelper;
import com.github.droidfu.cachefu.ImageCache;
import com.github.droidfu.widgets.WebImageView;


public class ImageHtmlLoader implements Runnable {

   public static final int HANDLER_MESSAGE_ID = 0;
   public static final String IMAGE_FILE_EXTRA = "droidfu:extra_image_file";
   public static final String IMAGE_URL_EXTRA = "droidfu:extra_image_url";
   public static final String IMAGE_ID_EXTRA = "droidfu:extra_image_id";
   
   private static final String LOG_TAG = "Droid-Fu/ImageHtmlLoader";
   // the default thread pool size
   private static final int DEFAULT_POOL_SIZE = 3;
   // expire images after a day
   // TODO: this currently only affects the in-memory cache, so it's quite pointless
   private static final int DEFAULT_TTL_MINUTES = 24 * 60;
   private static final int DEFAULT_RETRY_HANDLER_SLEEP_TIME = 1000;
   private static final int DEFAULT_NUM_RETRIES = 3;
   

   private static ThreadPoolExecutor executor;
   private static ImageCache imageCache;
   private static int numRetries = DEFAULT_NUM_RETRIES;

   private static long expirationInMinutes = DEFAULT_TTL_MINUTES;
   
   private String imageId;
   private String imageUrl;

   private ImageHtmlLoaderHandler handler;

   private String dbId;
   private ImageDbUpdater updater;

   
   /**
    * @param numThreads
    *            the maximum number of threads that will be started to download images in parallel
    */
   public static void setThreadPoolSize(int numThreads) {
       executor.setMaximumPoolSize(numThreads);
   }

   /**
    * @param numAttempts
    *            how often the image loader should retry the image download if network connection
    *            fails
    */
   public static void setMaxDownloadAttempts(int numAttempts) {
       ImageHtmlLoader.numRetries = numAttempts;
   }

   /**
    * This method must be called before any other method is invoked on this class. Please note that
    * when using ImageHtmlLoader as part of {@link WebImageView} or {@link WebGalleryAdapter}, then
    * there is no need to call this method, since those classes will already do that for you. This
    * method is idempotent. You may call it multiple times without any side effects.
    * 
    * @param context
    *            the current context
    */
   public static synchronized void initialize(Context context) {
       if (executor == null) {
           executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(DEFAULT_POOL_SIZE);
       }
       if (imageCache == null) {
           imageCache = new ImageCache(25, expirationInMinutes, DEFAULT_POOL_SIZE);
           imageCache.enableDiskCache(context, ImageCache.DISK_CACHE_SDCARD);
       }
   }

   public static synchronized void initialize(Context context, long expirationInMinutes) {
   	ImageHtmlLoader.expirationInMinutes = expirationInMinutes;
   	initialize(context);
   }   

   private ImageHtmlLoader(String dbId, String imageId, String imageUrl, ImageDbUpdater updater, ImageHtmlLoaderHandler handler) {
	   
	   this.dbId = dbId;
	   this.imageId = imageId;
	   this.imageUrl = imageUrl;
       this.handler = handler;
       this.updater = updater;
   }

   public static void start(String dbId, String id, String imageUrl, WebView webView, ImageDbUpdater updater) {
       start(dbId, id, imageUrl, updater, new ImageHtmlLoaderHandler(webView));
   }

   public static void start(String dbId, String imageId, String imageUrl, ImageDbUpdater updater, ImageHtmlLoaderHandler handler) {
       if (imageUrl == null) {
    	   throw new RuntimeException("Shouldn't happen");	
       }

       executor.execute(new ImageHtmlLoader(dbId, imageId, imageUrl, updater, handler));       
   }

   /**
    * Clears the 1st-level cache (in-memory cache). A good candidate for calling in
    * {@link android.app.Application#onLowMemory()}.
    */
   public static void clearCache() {
       imageCache.clear();
   }

   /**
    * Returns the image cache backing this image loader.
    * 
    * @return the {@link ImageCache}
    */
   public static ImageCache getImageCache() {
       return imageCache;
   }

   public static boolean isCached(String imageUrl) {
	   return imageCache.containsKey(imageUrl);
   }
   
   /**
    * The job method run on a worker thread. It will first query the image cache, and on a miss,
    * download the image from the Web.
    */
   public void run() {
	   Log.d(LOG_TAG, "run()");
	   
       if (imageCache.containsKey(imageUrl)) {
    	   notifyImageLoaded(imageUrl);
    	   return;
       }
       
       if (!downloadImage()) {
    	   Log.d(LOG_TAG, "Failed to download: " + imageUrl);
    	   return;
       }
       
       updater.update(dbId, getPath(imageUrl));
       
       notifyImageLoaded(imageUrl);
   }
   
   public static String getPath(String url) {
		return "file://"
			+ imageCache.getDiskCacheDirectory()
			+ "/"
			+ CacheHelper.getFileNameFromUrl(url);
   }
   
   protected boolean downloadImage() {
       int timesTried = 1;

       while (timesTried <= numRetries) {
           try {
               byte[] imageData = retrieveImageData();

               if (imageData != null) {
                   imageCache.put(imageUrl, imageData);
                   return true;
               } else {
                   break;
               }
           } catch (Throwable e) {
               Log.w(LOG_TAG, "download for " + imageUrl + " failed (attempt " + timesTried + ")");
               e.printStackTrace();
               SystemClock.sleep(DEFAULT_RETRY_HANDLER_SLEEP_TIME);
               timesTried++;
           }
       }

       return false;
   }

   protected byte[] retrieveImageData() throws IOException {
       URL url = new URL(imageUrl);
       HttpURLConnection connection = (HttpURLConnection) url.openConnection();

       // determine the image size and allocate a buffer
       int fileSize = connection.getContentLength();
       if (fileSize < 0) {
           return null;
       }
       byte[] imageData = new byte[fileSize];

       // download the file
       Log.d(LOG_TAG, "fetching image " + imageUrl + " (" + fileSize + ")");
       BufferedInputStream istream = new BufferedInputStream(connection.getInputStream());
       int bytesRead = 0;
       int offset = 0;
       while (bytesRead != -1 && offset < fileSize) {
           bytesRead = istream.read(imageData, offset, fileSize - offset);
           offset += bytesRead;
       }

       // clean up
       istream.close();
       connection.disconnect();

       return imageData;
   }

   public void notifyImageLoaded(String url) {
	   Log.d(LOG_TAG, String.format("notifyImageLoaded(%s)", url));

       Message message = new Message();
       message.what = HANDLER_MESSAGE_ID;
       
       Bundle data = new Bundle();
       data.putString(IMAGE_URL_EXTRA, url);
       data.putString(IMAGE_FILE_EXTRA, getPath(url));
       data.putString(IMAGE_ID_EXTRA, imageId);
       
       message.setData(data);

       handler.sendMessage(message);
   }
}