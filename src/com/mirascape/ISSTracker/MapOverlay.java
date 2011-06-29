package com.mirascape.ISSTracker;

import java.text.DecimalFormat;

import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.content.Context;

import android.location.Location;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class MapOverlay extends Overlay{

Bitmap iss;
Bitmap logo;
Bitmap logotext;
Bitmap tracker;
Location location;
int Lock = 0;

int Height = 0;
int Width = 0;
boolean Clicked = false;
long lastTime = -1;

	

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow)
	{
		Projection projection = mapView.getProjection();
		//int circleradius = 10;
		
		if (shadow == false)
		{
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			paint.setTextSize(22);
			paint.setTextAlign(Paint.Align.CENTER);
			
			paint.setARGB(150, 60, 60, 60);
			canvas.drawBitmap(logotext, canvas.getWidth() - logo.getWidth() - logotext.getWidth(), canvas.getHeight() - logotext.getHeight() - 75, paint);
			
			paint.setARGB(255, 255, 255, 255);
			canvas.drawBitmap(logo, canvas.getWidth() - logo.getWidth() - 10, canvas.getHeight() - logo.getHeight() - 75, paint);
			
			//draw logo
			//canvas.drawBitmap(tracker, (canvas.getWidth() / 2) - (tracker.getWidth() / 2), 0, paint);
			
			if (Height == 0 || Width == 0)
			{
				Width = canvas.getWidth();
				Height = canvas.getHeight();
			}
			
			if (Lock == 0)
			{
				paint.setARGB(150, 100, 100, 100);
				RectF backRect = new RectF ((canvas.getWidth() / 2) - 140, (canvas.getHeight() * 0.2f) - 20 , (canvas.getWidth() / 2) + 150 , canvas.getHeight() * 0.2f + 5 );
				canvas.drawRoundRect(backRect, 5, 5, paint);
				
				paint.setARGB(255, 255, 255, 255);
				//canvas.drawText("Getting ISS Position Data...", (canvas.getWidth() / 2) - 120, 50, paint);
				canvas.drawText("Getting ISS Position Data...", (canvas.getWidth() / 2), canvas.getHeight() * 0.2f, paint);
			}
			else
			{
			//get loc
				if (Lock == 1)
				{
					paint.setARGB(150, 100, 100, 100);
					RectF backRect = new RectF ((canvas.getWidth() / 2) - 150, (canvas.getHeight() * 0.2f) - 20, (canvas.getWidth() / 2) + 150 ,  canvas.getHeight() * 0.2f + 5  );
					canvas.drawRoundRect(backRect, 5, 5, paint);
					
					paint.setARGB(255, 255, 255, 255);
					//canvas.drawText("Getting ISS Movement Data...", (canvas.getWidth() / 2) - 140, 50, paint);
					canvas.drawText("Getting ISS Movement Data...", (canvas.getWidth() / 2),  canvas.getHeight() * 0.2f, paint);
				}
				if (location != null)
				{
					Double lat = location.getLatitude()*1E6;
					Double lng = location.getLongitude()*1E6;
					GeoPoint geopoint = new GeoPoint(lat.intValue(),lng.intValue());
					//conv to pixels
					Point point = new Point();
					projection.toPixels(geopoint, point);
					
					//RectF oval = new RectF(point.x - circleradius, point.y - circleradius, point.x + circleradius, point.y + circleradius);
					
					paint.setARGB(150, 100, 100, 100);
					RectF backRect = new RectF (point.x - (iss.getScaledHeight(canvas) / 2), point.y + (iss.getScaledWidth(canvas) / 2) - 25, point.x + (iss.getScaledHeight(canvas) / 2) + 45 , point.y + (iss.getScaledWidth(canvas) / 2) + 20 );
					canvas.drawRoundRect(backRect, 5, 5, paint);
					
					DecimalFormat df = new DecimalFormat("###.0###");
					String Latitude = df.format(location.getLatitude());
					String Longitude = df.format(location.getLongitude());
					
					paint.setARGB(255, 255, 255, 255);
					paint.setTextSize(16);
					paint.setTextAlign(Paint.Align.LEFT);
					canvas.drawBitmap(iss, point.x - (iss.getScaledHeight(canvas) / 2), point.y - (iss.getScaledWidth(canvas) / 2), paint);
					canvas.drawText("Latitude: " + Latitude, point.x - (iss.getScaledHeight(canvas) / 2) + 4, point.y + (iss.getScaledWidth(canvas) / 2) - 6, paint);
					canvas.drawText("Longitude: " + Longitude, point.x - (iss.getScaledHeight(canvas) / 2) + 4, point.y + (iss.getScaledWidth(canvas) / 2) + 10, paint);
					
					
				}
			}
			
		}
		super.draw(canvas, mapView, shadow);
			
	}
	
	@Override
	public boolean onTouchEvent(android.view.MotionEvent event, MapView v)
	{
		boolean caught = false;
			switch (event.getAction())
			{
				// Action started
				default: Log.i("touch", "got called");
				case MotionEvent.ACTION_DOWN:
					lastTime = System.currentTimeMillis();
					//Log.i("click" , "Last ="+lastTime);
					break;
				case MotionEvent.ACTION_UP:
					//Log.i("motion", "UP CALL");
					long thisTime = System.currentTimeMillis();
					//Log.i("click", "This ="+thisTime);
					long time = thisTime - lastTime;
					//Log.i("click", "X= "+event.getX()+" Y="+event.getY()+" time= " + time);
					if (thisTime - lastTime < 250 && event.getY() < tracker.getHeight())
					{
						Clicked = true;
						Log.i("click" , "Clicked");
						caught = true;
					}
						
					break;
			}
		
		return caught;
	}

	
	
	public void SetImages(Bitmap issimg, Bitmap logoimg, Bitmap logotextimg, Bitmap logotracker)
	{
		iss = issimg;
		logo = logoimg;
		logotext = logotextimg;
		tracker = logotracker;
	}
	
	@Override
	public boolean onTap(GeoPoint point, MapView mapView)
	{
		return false;
	}
	
	public boolean getClick()
	{
		return Clicked;
	}
	
	public void setClick(boolean click)
	{
		Clicked = click;
	}
	
	public Location getLocation()
	{
		return location;
	}
	
	public void setLocation(Location newlocation)
	{
		this.location = newlocation;
	}

	public void setLock(int i)
	{
		Lock = i;
	}
	
}
