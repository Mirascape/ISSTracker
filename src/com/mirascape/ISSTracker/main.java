package com.mirascape.ISSTracker;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.Rob.googlemapsC.R;
import com.google.android.maps.GeoPoint;
import android.location.Location;
import android.location.LocationManager;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.gson.Gson;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class main extends MapActivity
{

	MapController mControl;
	GeoPoint GeoP;
	MapView mapV;
	LocationManager locationManager;
	MapOverlay posOverlay;
	
	boolean FirstLock = false;
	boolean SecondLock = false;
	boolean dataSent = true;
	
	Location initlocation = new Location("initlp");
	Location jsonlocation = new Location("jsonlp");
	Location currentlocation = new Location("currentlp");
	Location pastlocation = new Location("pastlp");
	
	Timer updateTimer;
	int TimeSinceLastUpdate = 0;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mapV = (MapView) findViewById(R.id.mapView);

		mapV.displayZoomControls(true);
		mapV.setBuiltInZoomControls(true);
		mapV.setSaveEnabled(true);


		initlocation.setLatitude(0.1);
		initlocation.setLongitude(0.1);
	
		GeoP = new GeoPoint((int) (initlocation.getLatitude() * 1E6), (int) (initlocation.getLongitude() * 1E6));

		posOverlay = new MapOverlay();
		List<Overlay> overlays = mapV.getOverlays();
		overlays.add(posOverlay);

		mControl = mapV.getController();
		mControl.animateTo(GeoP);
		mControl.setZoom(5);

		Bitmap iss = BitmapFactory.decodeResource(getResources(), R.drawable.iss);
		Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
		Bitmap logotext = BitmapFactory.decodeResource(getResources(), R.drawable.logotext);
		Bitmap tracker = BitmapFactory.decodeResource(getResources(), R.drawable.isstracker);
		posOverlay.SetImages(iss, logo, logotext, tracker);
		posOverlay.setLocation(initlocation);


		if (CheckNetwork(getApplicationContext()))
		{
			startUpdateTimer();
		}
		else
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("No data connection found, please check you have a mobile data connection or wifi enabled.");
			builder.setNegativeButton("Quit", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int id)
				{
					dialog.cancel();
					finish();
				}
			});
			AlertDialog alert = builder.create();
			
			alert.show();
		}
	}
	
	public void onSaveInstanceState(Bundle savedInstanceState)
	{
		super.onSaveInstanceState(savedInstanceState);
		
		savedInstanceState.putBoolean("First", FirstLock);
		savedInstanceState.putBoolean("Second", SecondLock);
		if (currentlocation.getLatitude() != 35.317)
		{
			savedInstanceState.putDouble("curlat", currentlocation.getLatitude());
			savedInstanceState.putDouble("curlng", currentlocation.getLongitude());
			Log.i("Save Instance" , "Saving cur location");
			
			if (pastlocation.getLatitude() != 0.0)
			{
				savedInstanceState.putDouble("pastlat", pastlocation.getLatitude());
				savedInstanceState.putDouble("pastlng", pastlocation.getLongitude());
				Log.i("Save Instance" , "Saving past location");
			}
		}		
	}
	
	public void onRestoreInstanceState(Bundle savedInstanceState)
	{
		super.onRestoreInstanceState(savedInstanceState);
		
		FirstLock = savedInstanceState.getBoolean("First");
		Log.i("Restore Instance" , "First = " + FirstLock);
		
		SecondLock = savedInstanceState.getBoolean("Second");
		Log.i("Restore Instance" , "Second = " + SecondLock);
		
		pastlocation.setLatitude(savedInstanceState.getDouble("pastlat"));
		pastlocation.setLongitude(savedInstanceState.getDouble("pastlng"));
		currentlocation.setLatitude(savedInstanceState.getDouble("curlat"));
		currentlocation.setLongitude(savedInstanceState.getDouble("curlng"));
		
		if (FirstLock && !SecondLock)
		{
			posOverlay.setLock(1);
		}
		
		if (SecondLock)
		{
			posOverlay.setLock(2);
		}
	}

	@Override
	public void onPause()
	{
		stopUpdateTimer();
		super.onPause();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
	    return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.item1:
	    	mControl.animateTo(GetISSPos());
	        return true;
	    case R.id.item2:
	    	startActivity(new Intent(main.this, ISSWebView.class));
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	public boolean CheckNetwork(Context context)
	{
		 ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	     if (connectivity != null) {
	        NetworkInfo[] info = connectivity.getAllNetworkInfo();
	        if (info != null) {
	           for (int i = 0; i < info.length; i++) {
	              if (info[i].getState() == NetworkInfo.State.CONNECTED) {
	                 return true;
	              }
	           }
	        }
	     }
	     return false;
	 }
	
	protected void startUpdateTimer()
	{
		updateTimer = new Timer();
		updateTimer.schedule(new UpdateTask(), 0, 5000);
	}

	protected void stopUpdateTimer()
	{
		if (updateTimer != null)
			updateTimer.cancel();
	}

	public class UpdateTask extends TimerTask
	{

		@Override
		public void run()
		{
			updateISSLocation();
			Log.i("update", "Time since update:" + TimeSinceLastUpdate);
		}
	}

	private void updateISSLocation()
	{
		if (CheckForUpdate())
		{
			posOverlay.setLocation(currentlocation);
			mControl.animateTo(GetISSPos());
			
			if (!dataSent)
			{
				if (FirstLock && !SecondLock)
				{
					posOverlay.setLock(1);
					dataSent = true;
				}
				
				if (SecondLock)
				{
					posOverlay.setLock(2);
					dataSent = true;
				}
			}
		}

		else if (pastlocation.getLatitude() != 0.0)
		{
			double latdist = jsonlocation.getLatitude() - pastlocation.getLatitude();
			double longdist = jsonlocation.getLongitude() - pastlocation.getLongitude();
			if (longdist < -300)
			{
				longdist = 180 + longdist;
			}
			double changelat = (latdist / 60) * TimeSinceLastUpdate;
			double changelong = (longdist / 60) * TimeSinceLastUpdate;
			currentlocation.setLatitude(jsonlocation.getLatitude() + changelat);
			currentlocation.setLongitude(jsonlocation.getLongitude() + changelong);
			posOverlay.setLocation(currentlocation);
			mControl.animateTo(GetISSPos());
		}

	}

	public GeoPoint GetISSPos()
	{
		GeoPoint GeoISS = new GeoPoint((int) (currentlocation.getLatitude() * 1E6), (int) (currentlocation.getLongitude() * 1E6));
		return GeoISS;
	}
	public boolean CheckForUpdate()
	{

		Result result = GetJSONData();
		if (result != null)
		{
			if (result.latitude != null)
			{
				if (currentlocation != null)
				{
					if (result.latitude != jsonlocation.getLatitude())
					{
						// New data!
						if (jsonlocation.getLatitude() != 0.0)
						{
							pastlocation.setLatitude(jsonlocation.getLatitude());
							pastlocation.setLongitude(jsonlocation.getLongitude());
							pastlocation.setAltitude(jsonlocation.getAltitude());
						}
						
						jsonlocation.setLatitude(result.latitude);
						jsonlocation.setLongitude(result.longitude);
						jsonlocation.setAltitude(result.altitude);
	
						currentlocation.setLatitude(jsonlocation.getLatitude());
						currentlocation.setLongitude(jsonlocation.getLongitude());
						currentlocation.setAltitude(jsonlocation.getAltitude());
						
						if (!FirstLock && !SecondLock)
						{
							FirstLock = true;
							dataSent = false;
						}
						else if (FirstLock && !SecondLock)
						{
							SecondLock = true;
							dataSent = false;
						}
							
						
						TimeSinceLastUpdate = 0;
						return true;
					} 
					else
					{
						TimeSinceLastUpdate += 5;
						return false;
					}
				}
			} 
			else
			{
				TimeSinceLastUpdate += 5;
				return false;
			}
			TimeSinceLastUpdate += 5;
			return false;
		}
		return false;

	}

	public Result GetJSONData()
	{
		String url = "https://www.mirascape.com/earthmarks/155449.json";
		InputStream source = retrieveStream(url);

		if (source != null)
		{
		Gson gson = new Gson();

		Reader reader = new InputStreamReader(source);

		Result result = gson.fromJson(reader, Result.class);

		return result;
		}
		else
			return null;

	}

	private InputStream retrieveStream(String url)
	{

		DefaultHttpClient client = new DefaultHttpClient();

		HttpGet getRequest = new HttpGet(url);

		try
		{

			HttpResponse getResponse = client.execute(getRequest);
			final int statusCode = getResponse.getStatusLine().getStatusCode();

			if (statusCode != HttpStatus.SC_OK)
			{
				Log.w(getClass().getSimpleName(), "Error " + statusCode + " for URL " + url);
				return null;
			}

			HttpEntity getResponseEntity = getResponse.getEntity();
			return getResponseEntity.getContent();

		} catch (IOException e)
		{
			getRequest.abort();
			Log.w(getClass().getSimpleName(), "Error for URL " + url, e);
		}

		return null;

	}

	@Override
	protected boolean isRouteDisplayed()
	{
		// TODO Auto-generated method stub
		return false;
	}
}