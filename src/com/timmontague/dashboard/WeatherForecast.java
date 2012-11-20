package com.timmontague.dashboard;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class WeatherForecast extends DefaultHandler {
	private final static String TAG = "DASHBOARD";
	private final static String urlstring = "http://weather.yahooapis.com/forecastrss?w=12765839";
	private URL url;
	
	public String currentTemp;
	public String currentDesc;
	public String todayLow;
	public String todayHigh;
	public String todayDesc;
	public String tomorrowLow;
	public String tomorrowHigh;
	public String tomorrowDesc;
	public String currentCode;
	public String todayCode;
	public String tomorrowCode;

	private boolean firstDate;

	public WeatherForecast() {
		try {
			url = new URL(urlstring);
		} catch (MalformedURLException e) {
			Log.e(TAG, "Error opening URL: " + e.getLocalizedMessage());
		}
	}

	public void startElement(String uri, String name, String qName,
			Attributes atts) {
		//Log.i(TAG, "Name: " + qName + "\t" + name);
		if (qName.trim().equals("yweather:condition")) {
			currentTemp = atts.getValue("temp");
			currentDesc = atts.getValue("text");
			currentCode = atts.getValue("code");
		} else if (qName.trim().equals("yweather:forecast")) {
			if (firstDate) {
				todayLow = atts.getValue("low");
				todayHigh = atts.getValue("high");
				todayDesc = atts.getValue("text");
				todayCode = atts.getValue("code");
				firstDate = false;
			} else {
				tomorrowLow = atts.getValue("low");
				tomorrowHigh = atts.getValue("high");
				tomorrowDesc = atts.getValue("text");
				tomorrowCode = atts.getValue("code");
			}
		}
	}
	
	public void update() {
		try {
			firstDate = true;
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
			xr.setContentHandler(this);
			xr.parse(new InputSource(url.openStream()));
		} catch (IOException e) {
			Log.e(TAG, "Error parsing: " + e.getLocalizedMessage());
		} catch (SAXException e) {
			Log.e(TAG, "Error parsing: " + e.getLocalizedMessage());
		} catch (ParserConfigurationException e) {
			Log.e(TAG, "Error parsing: " + e.getLocalizedMessage());
		}
	}
}
