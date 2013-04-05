package com.example.weatherapp;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Weather application. Gets your location and shows the weather information.
 * 
 * @author Otto Kivikarki
 * 
 */
public class MainActivity extends Activity {

	/** TextViews **/
	private TextView textView, textView2;
	/** ImageView for weather icon **/
	private ImageView imageView;
	/** Location Manager **/
	private LocationManager locationManager;
	/** Location Listener **/
	private LocationListener locationListener;
	/** ProgressDialog **/
	private ProgressDialog pDialog1;
	/**  Latitude **/
	private double latitude = 0;
	/** Longitude **/
	private double longitude = 0;

	/**
	 * Called when activity is created.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		textView = (TextView) findViewById(R.id.tView1);
		textView2 = (TextView) findViewById(R.id.tView2);
		imageView = (ImageView) findViewById(R.id.imageView1);

		setupLocationService();
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, locationListener);
	}

	/**
	 * Called when options is created.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/**
	 * Initializes the location listener and shows a progress dialog.
	 */
	public void setupLocationService() {
				
		pDialog1 = new ProgressDialog(MainActivity.this);
		pDialog1.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pDialog1.setMessage("Getting location");
		pDialog1.show();
		
		locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
						
		locationListener = new LocationListener() {

			@Override
			public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onProviderEnabled(String arg0) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onProviderDisabled(String arg0) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onLocationChanged(Location loc) {
												
				latitude = loc.getLatitude();
				longitude = loc.getLongitude();
								
				Log.d("BOOT", latitude + " + " + longitude);

				try {
					URL url = new URL(
							"http://api.wunderground.com/api/2c763d8615191628/conditions/q/"
									+ latitude + "," + longitude + ".json");
					new getURLContent().execute(url);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
				
				locationManager.removeUpdates(locationListener);
								
			}
		};
	}

	/**
	 * Gets the weather icon from a url.
	 */
	private class getWeatherIcon extends AsyncTask<URL, Void, Drawable> {

		@Override
		protected Drawable doInBackground(URL... urls) {

			Drawable drawable = null;

			try {
				InputStream is = (InputStream) urls[0].getContent();
				drawable = Drawable.createFromStream(is, "src");

			} catch (Exception e) {
				e.printStackTrace();
			}

			return drawable;
		}

		@Override
		protected void onPostExecute(Drawable result) {
			imageView.setImageDrawable(result);
		}

	}
	 
	/**
	 * Gets the weather information from json.
	 */
	private class getURLContent extends AsyncTask<URL, Integer, String> {
								
		@Override
		protected String doInBackground(URL... urls) {
			
			HttpURLConnection urlConnection = null;
			String line = "";
			int character;

			try {
				urlConnection = (HttpURLConnection) urls[0].openConnection();
				InputStream in = new BufferedInputStream(
						urlConnection.getInputStream());

				while ((character = in.read()) != -1) {
					line += (char) character;
					
				}

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				urlConnection.disconnect();
			}
				         
	        return line;
		}
		
		protected void onProgressUpdate(Integer... progress) {
			
		}

		@Override
		protected void onPostExecute(String result) {
			if (pDialog1.isShowing()) {
				pDialog1.dismiss();
			}
			
			try {
				JSONObject jObject = new JSONObject(result);
				JSONObject cObject = jObject
						.getJSONObject("current_observation");
				JSONObject dObject = cObject.getJSONObject("display_location");

				String location = dObject.getString("full");
				String imageUrl = cObject.getString("icon_url");
				String weather = cObject.getString("weather");
				String temp_c = cObject.getString("temp_c");

				textView.setText(location);
				textView2.setText(weather + ", " + temp_c + "c");

				new getWeatherIcon().execute(new URL(imageUrl));
								
				pDialog1.dismiss();
				

			} catch (JSONException e) {
				e.printStackTrace();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}

	}

}
