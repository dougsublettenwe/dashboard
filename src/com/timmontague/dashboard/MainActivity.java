package com.timmontague.dashboard;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.timmontague.dashboard.Bikeshare.BikeStation;
import com.timmontague.dashboard.WMATA.WMATATime;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.text.Html;
import android.text.SpannableString;
import android.util.Log;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity {
	private final static String TAG = "DASHBOARD";
	private WeatherForecast weather;
	private WMATA metro;
	private Bikeshare bikeshare;
	private final Handler myHandler = new Handler();
	private Timer timer;
	private ArrayList<WMATATime> arrivalTimes;
	private ArrayList<BikeStation> stations;
	private Resources res;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        weather = new WeatherForecast();
        metro = new WMATA(getString(R.string.wmata_api_key));
        bikeshare = new Bikeshare();
        res = getResources();
    }
    
    public void onResume() {
    	super.onResume();
    	
    	timer = new Timer();
    	TimerTask updateProfile = new UpdateTask();
        timer.scheduleAtFixedRate(updateProfile, 0, 30000);
    }
    public void onPause() {
    	super.onResume();
    	timer.cancel();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    final Runnable updateRunnable = new Runnable() {
        public void run() {
            updateUI();
        }
	};
	private void updateUI() {
		StringBuilder sbMetro1 = new StringBuilder("");
		StringBuilder sbMetro2 = new StringBuilder("");
		StringBuilder sbBikeshare = new StringBuilder("");
		
		// update temperature
		((TextView) findViewById(R.id.currentTempText)).setText(weather.currentTemp + "¼ F");
		((TextView) findViewById(R.id.currentDescText)).setText(weather.currentDesc);
		((TextView) findViewById(R.id.todayHighText)).setText(weather.todayHigh + "¼ F");
		((TextView) findViewById(R.id.todayLowText)).setText(weather.todayLow + "¼ F");
		((TextView) findViewById(R.id.todayDescText)).setText(weather.todayDesc + "\n");
		((TextView) findViewById(R.id.tomorrowHighText)).setText(weather.tomorrowHigh + "¼ F");
		((TextView) findViewById(R.id.tomorrowLowText)).setText(weather.tomorrowLow + "¼ F");
		((TextView) findViewById(R.id.tomorrowDescText)).setText(weather.tomorrowDesc + "\n");
		((ImageView) findViewById(R.id.currentImage)).setImageResource(res.getIdentifier("weather" + weather.currentCode, "drawable", getPackageName()));
		((ImageView) findViewById(R.id.todayImage)).setImageResource(res.getIdentifier("weather" + weather.todayCode, "drawable", getPackageName()));
		((ImageView) findViewById(R.id.tomorrowImage)).setImageResource(res.getIdentifier("weather" + weather.tomorrowCode, "drawable", getPackageName()));
		
		// update WMATA
		for (WMATATime t : arrivalTimes) {
			StringBuilder sb;
			if (t.getLine().equals("1")) {
				sb = sbMetro1;
			} else {
				sb = sbMetro2;
			}
			
			// change text color if metro is within 10-8 minutes or 8-3 minutes
			if (t.getTimeInt() > 3 && t.getTimeInt() <= 8) {
				sb.append("<font color='red'>");
			} else if (t.getTimeInt() > 8 && t.getTimeInt() <= 10) {
				sb.append("<font color='#CD7300'>"); // orange
			}
			sb.append("<b>" + t.getTime() + "</b> " + t.getDestination());
			if (t.getTimeInt() > 3 && t.getTimeInt() <= 10) {
				sb.append("</font>");
			}
			sb.append("<br>");
		}
		((TextView) findViewById(R.id.metroLine1Text)).setText(
				Html.fromHtml(sbMetro1.toString()), TextView.BufferType.SPANNABLE);
		((TextView) findViewById(R.id.metroLine2Text)).setText(
				Html.fromHtml(sbMetro2.toString()), TextView.BufferType.SPANNABLE);
	
		// update bikeshare
		for (BikeStation s : stations) {
			// if home station and less than 4 bikes, change text color
			if (s.getId().equals(Bikeshare.homeId)) {
				if (s.getIntNumBikes() < 1) {
					sbBikeshare.append("<font color='red'>");
				} else if (s.getIntNumBikes() < 4) {
					sbBikeshare.append("<font color='#CD7300'>");
				}
			} else { // if not home station and less than 4 docks, change text color
				if (s.getIntNumDocks() < 1) {
					sbBikeshare.append("<font color='red'>");
				} else if (s.getIntNumDocks() < 4) {
					sbBikeshare.append("<font color='#CD7300'>");
				}
			}
			sbBikeshare.append(s.getStationName() + " - ");
			sbBikeshare.append(s.getNumBikes() + " Bike");
			try {
				if (Integer.parseInt(s.getNumBikes()) != 1) {
					sbBikeshare.append("s");
				}
			} catch (NumberFormatException e) {
				 Log.e(TAG, e.getMessage());
			}
			sbBikeshare.append(", ");
			sbBikeshare.append(s.getNumDocks() + " Dock");
			try {
				if (Integer.parseInt(s.getNumDocks()) != 1) {
					sbBikeshare.append("s");
				}
			} catch (NumberFormatException e) {
				Log.e(TAG, e.getMessage());
			}
			if (s.getId().equals(Bikeshare.homeId)) {
				if(s.getIntNumBikes() < 4) {
					sbBikeshare.append("</font>");
				}
			} else {
				if(s.getIntNumDocks() < 4) {
					sbBikeshare.append("</font>");
				}
			}
			sbBikeshare.append("<br>");
		}
		((TextView) findViewById(R.id.bikeshareText)).setText(
				Html.fromHtml(sbBikeshare.toString()), TextView.BufferType.SPANNABLE);
     }
    
    private class UpdateTask extends TimerTask {

		@Override
		public void run() {
			weather.update();
			stations = bikeshare.update();
			arrivalTimes = metro.update();
			myHandler.post(updateRunnable);
		}
    	
    }
}
