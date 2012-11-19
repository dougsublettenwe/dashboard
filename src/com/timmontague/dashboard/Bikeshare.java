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

public class Bikeshare extends DefaultHandler {
	private final static String TAG = "DASHBOARD";
	private static final String urlstring = "http://capitalbikeshare.com/data/stations/bikeStations.xml";
	private URL url;
	private ArrayList<BikeStation> stations;
	private BikeStation station;
	private String[] ids = { "52", "31" };
	private String topId = "52";
	
	private boolean inID = false;
	private boolean inName = false;
	private boolean inBikes = false;
	private boolean inDocks = false;
	
	public Bikeshare () {
		stations = new ArrayList<BikeStation>();
		try {
			url = new URL(urlstring);
		} catch (MalformedURLException e) {
			Log.e(TAG, "Error opening URL: " + e.getLocalizedMessage());
		}
	}
	
	public ArrayList<BikeStation> update () {
		stations.clear();
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
		// move the topId to the head of the array so it's displayed first
		for (BikeStation s : stations) {
			if (s.getId().equals(topId)) {
				station = s;
				break;
			}
		}
		stations.remove(station);
		stations.add(0, station);
		return stations;
	}
	public void startElement(String uri, String name, String qName,
			Attributes atts) {
		if (qName.trim().equals("station")) {
			station = new BikeStation();
		} else if (qName.trim().equals("id")) {
			inID = true;
		} else if (qName.trim().equals("name")) {
			inName = true;
		} else if (qName.trim().equals("nbBikes")) {
			inBikes = true;
		} else if (qName.trim().equals("nbEmptyDocks")) {
			inDocks = true;
		}
	}
	public void endElement(String uri, String name, String qName) {
		if (qName.trim().equals("station")) {
			for (String s : ids) {
				if (station.getId().equals(s)) {
					stations.add(station);
					break;
				}
			}
		} else if (qName.trim().equals("id")) {
			inID = false;
		} else if (qName.trim().equals("name")) {
			inName = false;
		} else if (qName.trim().equals("nbBikes")) {
			inBikes = false;
		} else if (qName.trim().equals("nbEmptyDocks")) {
			inDocks = false;
		}
	}
	public void characters(char ch[], int start, int length) {
		String chars = (new String(ch).substring(start, start + length));
		if (inID) {
			station.setId(chars);
		} else if (inName) {
			station.appendName(chars);
		} else if (inBikes) {
			station.setNumBikes(chars);
		} else if (inDocks) {
			station.setNumDocks(chars);
		}
	}
	public class BikeStation {
		private String stationName;
		private String numBikes;
		private String numDocks;
		private String id;
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getStationName() {
			return stationName;
		}
		public void appendName(String s) {
			if (stationName == null) {
				stationName = s;
			} else {
				stationName = stationName + s;
			}
		}
		public String getNumBikes() {
			return numBikes;
		}
		public void setNumBikes(String numBikes) {
			this.numBikes = numBikes;
		}
		public String getNumDocks() {
			return numDocks;
		}
		public void setNumDocks(String numDocks) {
			this.numDocks = numDocks;
		}
	}
}
