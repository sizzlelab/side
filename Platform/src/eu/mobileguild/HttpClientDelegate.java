package eu.mobileguild;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.util.Log;

public class HttpClientDelegate {

	private static final String TAG = HttpClientDelegate.class.getSimpleName();
	private HttpClient httpClient;

	public void createHttpClient()
	{
		Log.d(TAG,"createHttpClient()...");
		HttpParams params = new BasicHttpParams();
		
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);
		HttpProtocolParams.setUseExpectContinue(params, true);
		
		
		
		SchemeRegistry schReg = new SchemeRegistry();
		schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		
		ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params,schReg);
		
		httpClient = new DefaultHttpClient(conMgr, params);
	}

	public void shutdownHttpClient()
	{
		if (httpClient!=null && httpClient.getConnectionManager()!=null)
		{
			httpClient.getConnectionManager().shutdown();
		}
	}

	public HttpClient getHttpClient() {
		return httpClient;
	}

	public void setHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}
}
