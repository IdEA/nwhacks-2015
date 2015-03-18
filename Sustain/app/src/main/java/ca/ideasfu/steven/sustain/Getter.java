package ca.ideasfu.steven.sustain;

import android.os.Handler;
import android.support.v4.util.CircularArray;
import android.util.Log;
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

/**
 * Created by steven on 15/03/15.
 */
public class Getter implements Runnable {
	//String host = "http://206.12.53.185:12000/get/2";
	String host = "http://128.189.236.224:12000/get/2";
	StringBuilder builder;
	CircularArray<String> dataSet;
	TextView big;
	int towels = 0;
	int getHerz = 6;
	boolean inAnInterval = false;
	Handler updateHandler;

	public interface towelListener {
		void receiveTowel(int towel);
	}

	Getter() {
		builder = new StringBuilder();
		dataSet = new CircularArray<>(getHerz*10);
		updateHandler = new Handler();
	}

	public Runnable updateRunnable = new Runnable() {
		@Override
		public void run() {
			new Thread(new Runnable() {
				@Override
				public void run() {
					connect(host);
					for(String s : builder.toString().split(",")) {
						debug("s: " + s);
						if(s.length() > 0 && dataSet != null) {
							dataSet.addLast(s);
						}
					}
					builder.setLength(0);

					if(dataSet.size() > 0) {
						frag.run();
						updateHandler.removeCallbacks(updateRunnable);
					}
				}
			}).start();
			updateHandler.postDelayed(updateRunnable, 1000/getHerz);
		}
	};
	public void stop() {
		updateHandler.removeCallbacks(updateRunnable);
	}
	@Override
	public void run() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				connect(host);
				for(String s : builder.toString().split(",")) {
					debug("s: " + s);
					if(s.length() > 0 && dataSet != null) {
						dataSet.addLast(s);
					}
				}
				builder.setLength(0);

				if(dataSet.size() > 0) {
					frag.run();
					updateHandler.removeCallbacks(this);
				}
			}
		}).start();
		updateHandler.postDelayed(updateRunnable, 1000/getHerz);
	}

	void uiUpdateBigText() {
		Utils.uiRun(new Runnable() {
			@Override
			public void run() {
				big.setText(String.valueOf(towels));
			}
		});
	}
	Runnable frag;


	public void setNextFrag(Runnable frag) {
		this.frag = frag;
	}

	Runnable towelIntervalRunnable = new Runnable() {
		@Override
		public void run() {
			towels = 0;
			inAnInterval = false;
			updateHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					frag.run();
				}
			}, Utils.FRAGMENT_DELAY);
		}
	};

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
				}
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
