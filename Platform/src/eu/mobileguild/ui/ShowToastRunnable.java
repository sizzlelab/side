package eu.mobileguild.ui;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

public class ShowToastRunnable implements Runnable {

	private Context context;
	private int msgId;

	public ShowToastRunnable(Context context, int msgId) {
		this.context = context;
		this.msgId = msgId;
	}
	
	@Override
	public void run() {
		Toast.makeText(context, msgId, Toast.LENGTH_LONG).show();
	}

	public static void runOnUIThread(Activity activity, int msgId) {
		
		activity.runOnUiThread(new ShowToastRunnable(activity, msgId));
	}
}
