package ca.ideasfu.steven.sustain;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by steven on 14/03/15.
 */
public class Utils {
	public static void uiRun(Runnable run) {
		final Handler th = new Handler(Looper.getMainLooper());
		th.post(run);
	}
}
