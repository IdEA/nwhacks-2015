package ca.ideasfu.steven.sustain;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by steven on 14/03/15.
 */
public class IdleFragment extends Fragment {
	Handler updateHandler;
	Getter getter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		debug("onCreate");
		updateHandler = new Handler();
		getter = new Getter();
		getter.setNextFrag(new Runnable() {
			@Override
			public void run() {
				changeToPullingFragment();
			}
		});
		getter.run();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		debug("onCreateView");
		View view = inflater.inflate(R.layout.idle_layout, container, false);
		return view;
	}

	private void debug(String s, Object ... args) {
		Log.d(this.getClass().getSimpleName(), String.format(s, args));
	}

	void changeToPullingFragment() {
		if(getter != null) {
			getter.stop();
		}
		FragmentManager fm = getFragmentManager();
		if(fm != null) {
			fm.beginTransaction()
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
					.replace(R.id.fragment_container, new PullingFragment())
					.commit();
		}
	}
}
