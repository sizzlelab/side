package eu.mobileguild;

import org.apache.http.client.HttpClient;

import android.app.Application;

public class ApplicationProvidingHttpClient extends Application implements WithHttpClient {

	HttpClientDelegate httpClientDelegate = new HttpClientDelegate();
	
	public ApplicationProvidingHttpClient() {
		
		httpClientDelegate.createHttpClient();
	}
		
	@Override
	public void onLowMemory() {
		super.onLowMemory();
		httpClientDelegate.shutdownHttpClient();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		httpClientDelegate.shutdownHttpClient();
	}

	@Override
	public HttpClient getHttpClient() {
		// TODO Auto-generated method stub
		return httpClientDelegate.getHttpClient();
	}

}
