package kr.ac.kaist.isilab.kailos.indoormap;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.google.android.gms.maps.model.LatLng;

import kr.ac.kaist.isilab.kailos.util.Constants;
import android.os.Handler;
import android.os.Message;

public class IndoorMapRequestThread extends Thread {
	private String m_strRequestJSON;
	private Handler m_responseHandler;
	private boolean m_bUpdateFloorName;
	private long m_lRequestedTime;
	
	public IndoorMapRequestThread() {
		m_strRequestJSON = "";
		m_responseHandler = null;
		
		m_bUpdateFloorName = false;
		m_lRequestedTime = 0;
	}
	
	public void setRequestedTime(long lTime) {
		m_lRequestedTime = lTime;
	}
	
	public long getRequestedTime() {
		return m_lRequestedTime;
	}
	
	public void setRequestJSONData(String strData) {
		m_strRequestJSON = strData;
	}	
	
	public void setRequestDataWithFloorID(String strFloorID) {
		m_strRequestJSON = "{";
		
		m_strRequestJSON += "\"queryType\": \"FLOOR_OBJECTID\",";
		
		m_strRequestJSON += "\"lng\": \"0.000\",";
		m_strRequestJSON += "\"lat\": \"0.000\",";
		m_strRequestJSON += "\"floorID\": \"" + strFloorID + "\"";
		
		m_strRequestJSON += "}";
	}
	
	public void setRequestDataWithGPSCoordinate(double dbLat, double dbLng, boolean bEntireBuilding) {
		m_strRequestJSON = "{";
		
		if( bEntireBuilding )
			m_strRequestJSON += "\"queryType\": \"NEAREST_ENTIRE\",";
		else
			m_strRequestJSON += "\"queryType\": \"NEAREST_REPRESENTATIVE\",";
		
		m_strRequestJSON += "\"lng\": \"" + dbLng+ "\",";
		m_strRequestJSON += "\"lat\": \"" + dbLat + "\"";
		
		m_strRequestJSON += "}";
	}
	
	public void setResponseHandler(Handler newHandler) {
		m_responseHandler = newHandler;
	}
	
	public void setUpdateFloorNameIndicator(boolean newValue) {
		m_bUpdateFloorName = newValue;
	}
	
	// ----------------------------------------------------------------------------------------
	// Request floor map to a server, parse the JSON response and notify it to a handler...	
	public void run() {
		HttpURLConnection connection = null;;
		
		BufferedReader in = null;
		OutputStreamWriter os = null;
		
		String strLine = null;
		StringBuffer strBufJSONResponse = new StringBuffer();
		
		IndoorMap receivedMap = null;
		
		try {
			// If no response handler is specified, quit!
			if( m_responseHandler == null ) 
				return;			
			
			URL url = new URL(Constants.KAILOS_MAP_SERVER);
			
			connection = (HttpURLConnection)url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setDoOutput(true);
			connection.setConnectTimeout(Constants.SOCKET_TIME_OUT_MS);
			connection.setReadTimeout(Constants.SOCKET_TIME_OUT_MS);
			
			System.setProperty("http.keepAlive", "false");
			System.setProperty("java.net.preferIPv4Stack" , "true");
			
			connection.connect();
			
			// Write out the request data...
			os = new OutputStreamWriter(connection.getOutputStream());

			os.write(m_strRequestJSON);
			os.flush();
			
			// Read response...			
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			 
			while ((strLine = in.readLine()) != null) 
				strBufJSONResponse.append(strLine);
			
			// Parse the JSON;
			receivedMap = parseJSONResponseOnlyOneMap(strBufJSONResponse.toString());
			receivedMap.setRequestedTime(m_lRequestedTime);			
		} catch( Exception e ) {
			// Nothing to do... 
			// Exceptions, such as time-out, are ignored!			
		} finally {
			try {
				if( in != null )
					in.close();
				
				if( os != null )
					os.close();	
				
				// Generate a indoor map response message..
				Message msg = new Message();
				msg.what = Constants.MESSAGE_INDOOR_MAP_THREAD_RESPONSE;
				msg.obj = receivedMap;
				
				m_responseHandler.sendMessage(msg);
				
				// Generate a floor name indicator update message..
				Message indicatorMsg = new Message();
				indicatorMsg.what = Constants.MESSAGE_FLOOR_NAME_INDICATOR_UPDATE;
				
				if( m_bUpdateFloorName && receivedMap != null )
					indicatorMsg.obj = receivedMap.getFloorName();
				else
					indicatorMsg.obj = "";
				
				m_responseHandler.sendMessage(indicatorMsg);
			
			} catch( Exception e ) {
				// Do Nothing!
			}			
		}
	}

	// Deprecated!
/*	// Parse the JSON response and store the parsed indoor map information to m_lstNearIndoorMaps...
	// -cf) Since we do not have an actual map server, we might will implement this part again!	
	public ArrayList<IndoorMap> parseJSONMapResponse(String strJSONMapResponse) throws Exception {
		JSONObject json = (JSONObject) JSONValue.parse(strJSONMapResponse);
		Iterator<?> iter = json.keySet().iterator();
		
		ArrayList<IndoorMap> lstParsedMapInfo = new ArrayList<IndoorMap>();		
		
		while( iter.hasNext() ) {						
			String key = (String) iter.next();
			Object value = json.get(key);
			
			if( key.compareToIgnoreCase("floormaps") == 0 ) {
				JSONArray arrMaps = (JSONArray)value;
				
				for(int i=0; i<arrMaps.size(); i++) {
					IndoorMap indoormap = new IndoorMap();
					JSONObject mapObject = (JSONObject)arrMaps.get(i);
					Iterator<?> mapIter = mapObject.keySet().iterator();
					
					while( mapIter.hasNext() ) {
						String mapKey = (String)mapIter.next();
						Object mapValue = mapObject.get(mapKey);
						
						if( mapKey.equals("neLat") )
							indoormap.setNELatitude(Double.parseDouble(mapValue.toString()));				
						else if( mapKey.equals("neLng") )
							indoormap.setNELongitude(Double.parseDouble(mapValue.toString()));
						else if( mapKey.equals("swLat") )
							indoormap.setSWLatitude(Double.parseDouble(mapValue.toString()));
						else if( mapKey.equals("swLng") )
							indoormap.setSWLongitude(Double.parseDouble(mapValue.toString()));
						else if( mapKey.equals("imageURL") )
							indoormap.setImageURL(mapValue.toString());
						else if( mapKey.equals("buildingID") )
							indoormap.setBuldingID(Integer.parseInt(mapValue.toString()));
						else if( mapKey.equals("buildingName") )
							indoormap.setBuildingName(mapValue.toString());
						else if( mapKey.equals("floorID") )
							indoormap.setFloorID(Integer.parseInt(mapValue.toString()));
						else if( mapKey.equals("floorName") )
							indoormap.setFloorName(mapValue.toString());	
						else if( mapKey.equals("angle") )
							indoormap.setRotationalDegree(Double.parseDouble(mapValue.toString()));
					}		
					
					indoormap.createBitmapDescriptor();
					lstParsedMapInfo.add(indoormap);
				}
			}						
		}		
		
		return lstParsedMapInfo;
	}*/	
	
	public IndoorMap parseJSONResponseOnlyOneMap(String strJSONMapResponse) throws Exception {
		JSONObject json = (JSONObject) JSONValue.parse(strJSONMapResponse);
		Iterator<?> iter = json.keySet().iterator();
		
		IndoorMap indoormap = null;
		
		while( iter.hasNext() ) {						
			String key = (String) iter.next();
			Object value = json.get(key);
			
			if( key.compareToIgnoreCase("floormaps") == 0 ) {
				JSONArray arrMaps = (JSONArray)value;
				
				if( arrMaps.size() > 0 ) {					
					indoormap = new IndoorMap();
					JSONObject mapObject = (JSONObject)arrMaps.get(0);
					Iterator<?> mapIter = mapObject.keySet().iterator();
					
					while( mapIter.hasNext() ) {
						String mapKey = (String)mapIter.next();
						Object mapValue = mapObject.get(mapKey);
						
						if( mapValue == null )
							continue;
						
						if( mapKey.equals("_id") )
							indoormap.setFloorID(mapValue.toString());
						else if( mapKey.equals("buildingId") )
							indoormap.setBuildingID(mapValue.toString());
						else if( mapKey.equals("name") )
							indoormap.setFloorName(mapValue.toString());							
						else if( mapKey.equals("floorNumber") )
							indoormap.setFloorLevel(Integer.parseInt(mapValue.toString()));
						else if( mapKey.equals("floorPlan") )
							indoormap.setFloorImg(mapValue.toString());							
						else if( mapKey.equals("description") )							
							indoormap.setDescription(mapValue.toString());
						else if( mapKey.equals("geometry") ) {							
							JSONObject geoJSON = (JSONObject)mapValue;
							Iterator<?> geoJSONIter = geoJSON.keySet().iterator();
							
							while( geoJSONIter.hasNext() ) {
								String geoJSONKey = (String)geoJSONIter.next();
								Object geoJSONObject = geoJSON.get(geoJSONKey);
								
								if( geoJSONKey.equals("type") ) {
									// Nothing to do yet..
								} else if( geoJSONKey.equals("coordinates") ) {
									JSONArray coordinates = (JSONArray)geoJSONObject;
									
									if( coordinates.size() == 2 ) {
										JSONArray NECoordinate = (JSONArray)coordinates.get(0);
										JSONArray SWCoordinate = (JSONArray)coordinates.get(1);
										
										if( SWCoordinate.size() == 2 && NECoordinate.size() == 2 ) {
											// Well, finally!
											LatLng sw = new LatLng(Double.parseDouble(SWCoordinate.get(1).toString()), Double.parseDouble(SWCoordinate.get(0).toString()));
											LatLng ne = new LatLng(Double.parseDouble(NECoordinate.get(1).toString()), Double.parseDouble(NECoordinate.get(0).toString()));;
											
											indoormap.setSWLatLng(sw);											
											indoormap.setNELatLng(ne);
										}											
									}
								} else if( geoJSONKey.equals("angle") ) {
									indoormap.setRotationalDegree(Double.parseDouble(geoJSONObject.toString()));
								}
							}
						}
					}					
					
					indoormap.createBitmapDescriptor();
						
					// Valid?
					if( indoormap.getSWLatLng() == null || indoormap.getNELatLng() == null )
						indoormap = null;					
					
					return indoormap;
				}
			}						
		}		
		
		return null;
	}
}
