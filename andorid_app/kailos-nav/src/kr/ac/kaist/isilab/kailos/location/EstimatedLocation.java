package kr.ac.kaist.isilab.kailos.location;

import java.util.Iterator;

import kr.ac.kaist.isilab.kailos.util.Constants;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import android.location.Location;

public class EstimatedLocation {
	private double m_dbALtitude;
	private double m_dbErrorDistance;
	private double m_dbLatitude;
	private double m_dbLongitude;
	private long m_lTimeStamp;	
	private int m_nLevel;	
	private String m_strBuildingName;	
	private String m_strFloorID;

	public EstimatedLocation() {
		m_nLevel = Constants.LEVEL_NOT_DEFINED;
		m_strBuildingName = "";
		m_strFloorID = "";						
	}

	public EstimatedLocation(Location location) {
		//m_lTimeStamp = System.currentTimeMillis();
		m_lTimeStamp = System.nanoTime();
		m_dbLatitude = location.getLatitude();
		m_dbLongitude = location.getLongitude();
		m_dbALtitude = location.getAltitude();
		m_nLevel = Constants.LEVEL_NOT_DEFINED;
		m_strBuildingName = "";
		m_strFloorID = "";
		m_dbErrorDistance = location.getAccuracy();		
	}

	public EstimatedLocation(String strWPSResponse) {
		//m_lTimeStamp = System.currentTimeMillis();
		m_lTimeStamp = System.nanoTime();
		m_dbLatitude = 0.0D;
		m_dbLongitude = 0.0D;
		m_dbALtitude = 0.0D;
		m_nLevel = Constants.LEVEL_NOT_DEFINED;
		m_strBuildingName = "";		
		m_dbErrorDistance = 9999.0D;
		//m_nFloorID = Constants.FLOOR_NOT_DEFINED;
		m_strFloorID = "";
		
		parseEstimatedLocationResponse(strWPSResponse);
	}
	
	private void parseEstimatedLocationResponse(String strData) {
		JSONObject json = (JSONObject) JSONValue.parse(strData);
		Iterator<?> iter = json.keySet().iterator();
		
		while( iter.hasNext() ) {
			String key = (String)iter.next();
			Object object = json.get(key);
			
			if( object == null )
				continue;
			
			if( key.equals("lnglat") ) {
				JSONObject geoJSON = (JSONObject)object;
				Iterator<?> geoJSONIter = geoJSON.keySet().iterator();
				
				while( geoJSONIter.hasNext() ) {
					String geoJSONKey = (String)geoJSONIter.next();
					Object geoJSONObject = geoJSON.get(geoJSONKey);
					
					if( geoJSONKey.equals("type") ) {
						// Nothing to do yet..
					} else if( geoJSONKey.equals("coordinate") ) {
						JSONArray coordinate = (JSONArray)geoJSONObject;
						
						if( coordinate.size() == 2 ) {
							m_dbLongitude = Double.parseDouble(String.format("%.6f", Double.parseDouble(coordinate.get(0).toString())));
							m_dbLatitude = Double.parseDouble(String.format("%.6f", Double.parseDouble(coordinate.get(1).toString())));
						}
					}
				}
				
			} else if( key.equals("alt") ) {
				m_dbALtitude = Double.parseDouble(object.toString());				
			} else if( key.equals("level") ) {
				m_nLevel = Integer.parseInt(object.toString());
			} else if( key.equals("bd") ) {
				m_strBuildingName = (String)object;				
			} else if( key.equals("err") ) {
				m_dbErrorDistance = Double.parseDouble(object.toString());
			} else if( key.equals("floorID") ) {			
				m_strFloorID = object.toString();
			}
		}
	}

	public static EstimatedLocation createFromString(String paramString)
	{
		EstimatedLocation estimatedLocation = new EstimatedLocation();
		
		String[] strTokens = paramString.split(",");
		
		if( strTokens.length == 8 ) {
			estimatedLocation.m_lTimeStamp = Long.parseLong(strTokens[0]);
			estimatedLocation.m_dbLatitude = Double.parseDouble(strTokens[1]);
			estimatedLocation.m_dbLongitude = Double.parseDouble(strTokens[2]);
			estimatedLocation.m_dbALtitude = Double.parseDouble(strTokens[3]);
			estimatedLocation.m_nLevel = Integer.parseInt(strTokens[4]);
			estimatedLocation.m_strBuildingName = strTokens[5];
			estimatedLocation.m_dbErrorDistance = Double.parseDouble(strTokens[6]);
			estimatedLocation.m_strFloorID = strTokens[7];			
		} else if( strTokens.length == 7 ) {
			estimatedLocation.m_lTimeStamp = Long.parseLong(strTokens[0]);
			estimatedLocation.m_dbLatitude = Double.parseDouble(strTokens[1]);
			estimatedLocation.m_dbLongitude = Double.parseDouble(strTokens[2]);
			estimatedLocation.m_dbALtitude = Double.parseDouble(strTokens[3]);
			estimatedLocation.m_nLevel = Integer.parseInt(strTokens[4]);
			estimatedLocation.m_strBuildingName = strTokens[5];
			estimatedLocation.m_dbErrorDistance = Double.parseDouble(strTokens[6]);
			estimatedLocation.m_strFloorID = "";						
		}
		
		return estimatedLocation;
	}
	
	public String toString() {
		String strData = "";
		
		strData += m_lTimeStamp + ",";
		strData += m_dbLatitude + ",";
		strData += m_dbLongitude + ",";
		strData += m_dbALtitude + ",";
		strData += m_nLevel + ",";
		strData += m_strBuildingName + ",";		
		strData += m_dbErrorDistance + ",";
		strData += m_strFloorID;
		
		return strData;		
	}

	public double getALtitude() {
		return m_dbALtitude;
	}

	public double getErrorDistance() {
		return m_dbErrorDistance;
	}

	public double getLatitude() {
		return m_dbLatitude;
	}

	public double getLongitude() {
		return m_dbLongitude;
	}

	public long getTimeStamp() {
		return m_lTimeStamp;
	}

	public int getLevel() {
		return m_nLevel;
	}

	public String getBuildingName() {
		return m_strBuildingName;
	}		
	
	public String getFloorID() {
		return m_strFloorID;
	}

	public void setALtitude(double m_dbALtitude) {
		this.m_dbALtitude = m_dbALtitude;
	}

	public void setErrorDistance(double m_dbErrorDistance) {
		this.m_dbErrorDistance = m_dbErrorDistance;
	}

	public void setLatitude(double m_dbLatitude) {
		this.m_dbLatitude = m_dbLatitude;
	}

	public void setLongitude(double m_dbLongitude) {
		this.m_dbLongitude = m_dbLongitude;
	}
}
