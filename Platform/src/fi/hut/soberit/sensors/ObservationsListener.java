package fi.hut.soberit.sensors;

import java.util.List;

import android.os.Parcelable;

public interface ObservationsListener {
	

	void onReceiveObservations(DriverConnection connection, List<Parcelable> observations);
}
