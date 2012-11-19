package com.timmontague.dashboard;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class WMATA extends DefaultHandler {
	private final static String TAG = "DASHBOARD";
	private static final String station = "B35"; // new york ave
	private final static String urlstring = "http://api.wmata.com/StationPrediction.svc/GetPrediction/";
	private URL url;
	private ArrayList<WMATATime> arrivalTimes;
	private WMATATime arrivalTime;
	
	private boolean inDestination = false;
	private boolean inGroup = false;
	private boolean inMin = false;
	private boolean isNull = false;
	
	public WMATA (String apikey) {
		arrivalTimes = new ArrayList<WMATA.WMATATime>();
		try {
			url = new URL(urlstring + station + "?api_key=" + apikey);
		} catch (MalformedURLException e) {
			Log.e(TAG, "Error opening URL: " + e.getLocalizedMessage());
		}
	}
	
	public ArrayList<WMATATime> update () {
		arrivalTimes.clear();
		try {
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
		return arrivalTimes;
	}
	public void startElement(String uri, String name, String qName,
			Attributes atts) {
		if (qName.trim().equals("AIMPredictionTrainInfo")) {
			arrivalTime = new WMATATime();
			isNull = false;
		} else if (qName.trim().equals("DestinationName")) {
			inDestination = true;
		} else if (qName.trim().equals("Group")) {
			inGroup = true;
		} else if (qName.trim().equals("Min")) {
			inMin = true;
		} else if (qName.trim().equals("DestinationCode")) {
			if (atts.getType("i:nil") != null && atts.getValue("i:nil").equals("true")) {
				isNull = true;
			}
		}
	}
	public void endElement(String uri, String name, String qName) {
		if (qName.trim().equals("AIMPredictionTrainInfo")) {
			if (!isNull) {
				arrivalTimes.add(arrivalTime);
			}
		} else if (qName.trim().equals("DestinationName")) {
			inDestination = false;
		} else if (qName.trim().equals("Group")) {
			inGroup = false;
		} else if (qName.trim().equals("Min")) {
			inMin = false;
		}
	}
	public void characters(char ch[], int start, int length) {
		String chars = (new String(ch).substring(start, start + length));
		if (inDestination) {
			arrivalTime.setDestination(chars);
		} else if (inGroup) {
			arrivalTime.setLine(chars);
		} else if (inMin) {
			arrivalTime.setTime(chars);
		}
	}
	
	public class WMATATime {
		private String destination;
		private String time;
		private String line;
		public String getDestination() {
			return destination;
		}
		public void setDestination(String destination) {
			this.destination = destination;
		}
		public String getTime() {
			return time;
		}
		public void setTime(String time) {
			this.time = time;
		}
		public String getLine() {
			return line;
		}
		public void setLine(String line) {
			this.line = line;
		}
		public int getTimeInt() {
			int i;
			try {
				i = Integer.parseInt(time);
			} catch (NumberFormatException e) {
				return 0;
			}
			return i;
		}
		
	}
}
