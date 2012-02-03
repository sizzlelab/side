package fi.hut.soberit.sensors;

import android.os.Message;

public interface MessagesListener {

	void onReceivedMessage(DriverConnection connection, Message msg);
}
