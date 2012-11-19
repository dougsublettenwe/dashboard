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
		((TextView) findViewById(R.id.metroLine1Text)).setText("");
		((TextView) findViewById(R.id.metroLine2Text)).setText("");
		for (WMATATime t : arrivalTimes) {
			if (t.getLine().equals("1")) {
				((TextView) findViewById(R.id.metroLine2Text)).append(Html.fromHtml("<b>" + t.getTime() + "</b> " + t.getDestination() + "<br>"));
			} else if (t.getLine().equals("2")) {
				((TextView) findViewById(R.id.metroLine1Text)).append(Html.fromHtml("<b>" + t.getTime() + "</b> " + t.getDestination() + "<br>"));
			}
		}
		// change background color if metro is within 10-8 minutes or 8-5 minutes
		((TextView) findViewById(R.id.metroLine1Text)).setBackgroundColor(Color.TRANSPARENT);
		((TextView) findViewById(R.id.metroLine2Text)).setBackgroundColor(Color.TRANSPARENT);
		for (WMATATime t : arrivalTimes) {
			if (t.getTimeInt() > 5 && t.getTimeInt() <= 8) {
				if (t.getLine().equals("1")) {
					((TextView) findViewById(R.id.metroLine2Text)).setBackgroundColor(Color.RED);
				} else {
					((TextView) findViewById(R.id.metroLine1Text)).setBackgroundColor(Color.RED);
				}
				break;
			} else if (t.getTimeInt() > 8 && t.getTimeInt() <= 10) {
				if (t.getLine().equals("1")) {
					((TextView) findViewById(R.id.metroLine2Text)).setBackgroundColor(Color.YELLOW);
				} else {
					((TextView) findViewById(R.id.metroLine1Text)).setBackgroundColor(Color.YELLOW);
				}
				break;
			}
		}
		((TextView) findViewById(R.id.bikeshareText)).setText("");
		for (BikeStation s : stations) {
			((TextView) findViewById(R.id.bikeshareText)).append(s.getStationName() + " - ");
			((TextView) findViewById(R.id.bikeshareText)).append(s.getNumBikes() + " Bike");
			try {
				if (Integer.parseInt(s.getNumBikes()) != 1) {
					((TextView) findViewById(R.id.bikeshareText)).append("s");
				}
			} catch (NumberFormatException e) {
				 Log.e(TAG, e.getMessage());
			}
			((TextView) findViewById(R.id.bikeshareText)).append(", ");
			((TextView) findViewById(R.id.bikeshareText)).append(s.getNumDocks() + " Dock");
			try {
				if (Integer.parseInt(s.getNumDocks()) != 1) {
					((TextView) findViewById(R.id.bikeshareText)).append("s");
				}
			} catch (NumberFormatException e) {
				Log.e(TAG, e.getMessage());
			}
			((TextView) findViewById(R.id.bikeshareText)).append("\n");
		}
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
