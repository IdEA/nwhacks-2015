package ca.ideasfu.steven.sustain;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.util.CircularArray;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by steven on 14/03/15.
 */
public class DispenserFragment extends Fragment {
	static int FIELD_THRESHOLD = 400;
	TextView big;
	StringBuilder builder;
	CircularArray<String> dataSet;
	int hertz = 5;
	Handler updateHandler;
	String host = "http://206.12.53.185:12000/get/2";
	Runnable updateRunnable = new Runnable() {
		@Override
		public void run() {
			new Thread(new Runnable() {
				@Override
				public void run() {
					connect(host);
					grunt();
					doAnalysis();
				}


			}).start();
			updateHandler.postDelayed(updateRunnable, 1000/hertz);
		}
	};

	void grunt() {
		for(String s : builder.toString().split(",")) {
			debug("s: " + s);
			if(s.length() > 0) {
				dataSet.addLast(s);
			}
			builder.setLength(0);
		}
	}
	boolean prevAnalWasCandidate = false;
	int towels = 0;
	void doAnalysis() {
		int count = 0;
		int thrown = 0;
		int candidates = 0;
		while(!dataSet.isEmpty()) {
			count++;
			String popped = dataSet.popFirst();
			if(popped == null)
				continue;
			int pval = Integer.valueOf(popped);
			debug("k: " + popped);
			if(pval >= FIELD_THRESHOLD) {
				// throw it out
				thrown++;
				if(prevAnalWasCandidate) {
					prevAnalWasCandidate = false;
					towels++;
					Utils.uiRun(new Runnable() {
						@Override
						public void run() {
							big.setText(String.valueOf(towels));
						}
					});
					debug("towels: " + towels);
				}
			} else {
				// candidate
				candidates++;
				prevAnalWasCandidate = true;
			}
		}
	}

	void startRepeatingTask() {
		updateRunnable.run();
	}

	void stopRepeatingTask() {
		updateHandler.removeCallbacks(updateRunnable);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		debug("onCreate");
		builder = new StringBuilder();
		updateHandler = new Handler();
		dataSet = new CircularArray<>(hertz*10);
		startRepeatingTask();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		debug("onCreateView");

		View view = inflater.inflate(R.layout.dispenser_layout, container, false);
		big = (TextView)view.findViewById(R.id.disp_big);
		return view;
	}

	public void connect(String url) {
		debug("connecting to " + url);
		HttpClient httpclient = new DefaultHttpClient();

		// Prepare a request object
		HttpGet httpget = new HttpGet(url);

		// Execute the request
		HttpResponse response;
		try {
			response = httpclient.execute(httpget);
			// Examine the response status
			debug(response.getStatusLine().toString());

			// Get hold of the response entity
			HttpEntity entity = response.getEntity();
			// If the response does not enclose an entity, there is no need
			// to worry about connection release

			if (entity != null) {

				// A Simple JSON Response Read
				InputStream instream = entity.getContent();
				String result = convertStreamToString(instream).replace("\n", "");
				if(result.length() > 0) {
					builder.append(result);
					debug("builder: " + builder.toString());
				}
				debug("res: " + result);
				// now you have the string representation of the HTML request
				instream.close();
			}


		} catch (Exception e) {
			debug("something bad happened: " + e.toString());
		}
	}

	private static String convertStreamToString(InputStream is) {
    /*
     * To convert the InputStream to String we use the BufferedReader.readLine()
     * method. We iterate until the BufferedReader return null which means
     * there's no more data to read. Each line will appended to a StringBuilder
     * and returned as String.
     */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	private void debug(String s, Object ... args) {
		Log.d(this.getClass().getSimpleName(), String.format(s, args));
	}
}

