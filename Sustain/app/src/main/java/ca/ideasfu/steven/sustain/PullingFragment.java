package ca.ideasfu.steven.sustain;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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

/**
 * Created by steven on 14/03/15.
 */
public class PullingFragment extends Fragment {
	StringBuilder builder;
	int towels = 0;
	int factnm = 0;
	boolean pulling = false;
	String facts[] = {"1+1=2", "3+3=2", "f+f=3"};
	TextView big, response, response2;
	CircularArray<String> dataSet;
	int getHerz = 6;
	int infoIntervalSec = 10;
	Handler updateHandler;
	Handler fragHandler;
	//String host = "http://206.12.53.185:12000/get/2";
	String host = "http://128.189.236.224:12000/get/2";
	boolean inAnInterval = false;
	Runnable towelIntervalRunnable = new Runnable() {
		@Override
		public void run() {
			debug("wtf?: towelinterv" );
			towels = 0;
			inAnInterval = false;
			uiUpdateBigText();
			fragHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					changeToIdleFragment();
				}
			}, Utils.FRAGMENT_DELAY);
		}
	};

	void changeToIdleFragment() {
		updateHandler.removeCallbacks(updateRunnable);
		updateHandler.removeCallbacks(towelIntervalRunnable);
		fragHandler.removeCallbacks(towelIntervalRunnable);
		debug("wtf?: " + "removed updatehandle from pullingfrag");
		getFragmentManager().beginTransaction()
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
				.replace(R.id.fragment_container, new IdleFragment(), MainActivity.DRAWER_DISPENSER_TAG)
				.commit();
	}

	public Runnable updateRunnable = new Runnable() {
		@Override
		public void run() {
			new Thread(new Runnable() {
				@Override
				public void run() {
					if(!inAnInterval) {
						inAnInterval = true;
						fragHandler.postDelayed(towelIntervalRunnable, 3000);
						debug("wtf? initial");
					} else {
						fragHandler.removeCallbacks(towelIntervalRunnable);
						fragHandler.postDelayed(towelIntervalRunnable, 3000);
					}
					connect(host);
					for(String s : builder.toString().split(",")) {
						debug("s: " + s);
						if(s.length() > 0 && dataSet != null) {
							dataSet.addLast(s);
						}
					}
					builder.setLength(0);

					while(!dataSet.isEmpty()) {
						String popped = dataSet.popFirst();
						debug("wtf?: not empty" );
						if(popped == null)
							continue;
						debug("wtf: cont" + popped + " towels" + inAnInterval);
						towels++;
						if(!inAnInterval) {
							inAnInterval = true;
							fragHandler.postDelayed(towelIntervalRunnable, 3000);
						} else {
							debug("in the else");
							fragHandler.removeCallbacks(towelIntervalRunnable);
							fragHandler.postDelayed(towelIntervalRunnable, 3000);
						}
						uiUpdateBigText();
					}
				}
			}).start();
			updateHandler.postDelayed(updateRunnable, 1000/getHerz);
		}
	};

	Runnable infoRunnable = new Runnable() {
		@Override
		public void run() {
			Utils.uiRun(new Runnable() {
				@Override
				public void run() {
					factnm = ++factnm >= facts.length ? 0 : factnm;
					//Crouton.makeText(getActivity(), facts[factnm], Style.CONFIRM, croutonView).show();
				}
			});
			updateHandler.postDelayed(infoRunnable, 1000 * infoIntervalSec);
		}
	};

	void uiUpdateBigText() {
		Utils.uiRun(new Runnable() {
			@Override
			public void run() {
				big.setText(String.valueOf(towels));
				int calc = (40000 * towels * 3)/800*25;
				response.setText("Cost UBC $" + String.valueOf(calc) + " per day");
				int carbon = (int)(towels*40000*3*3.75*7.75/2/1000);
				response2.setText("Contribute " + String.valueOf(carbon) + " kilograms of carbon");
			}
		});
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		debug("onCreate");
		builder = new StringBuilder();
		updateHandler = new Handler();
		fragHandler = new Handler();
		dataSet = new CircularArray<>(getHerz*2);
		dataSet.addFirst("0");
		updateRunnable.run();
		infoRunnable.run();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		debug("onCreateView");

		View view = inflater.inflate(R.layout.dispenser_pulling_layout, container, false);
		big = (TextView)view.findViewById(R.id.pulling_bigtxt);
		response = (TextView)view.findViewById(R.id.pulling_response);
		response2= (TextView)view.findViewById(R.id.pulling_response2);
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

