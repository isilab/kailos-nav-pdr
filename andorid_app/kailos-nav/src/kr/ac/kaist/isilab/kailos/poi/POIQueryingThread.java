package kr.ac.kaist.isilab.kailos.poi;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import kr.ac.kaist.isilab.kailos.util.Constants;
import android.os.Handler;
import android.os.Message;

public class POIQueryingThread extends Thread {
	private String m_strReuqestData;
	private Handler m_responseHandler;
	
	public void setRequestData(String newData) {
		m_strReuqestData = newData;
		m_responseHandler = null;
	}
	
	public void setRequestDataWithKeyword(String strKeyword) {
		m_strReuqestData = "{ \"queryType\": \"POI\",";		
		m_strReuqestData += "\"keyword\": \"" + strKeyword + "\" }";
	}
	
	public void setResponseHandler(Handler _newHandler) {
		m_responseHandler = _newHandler;
	}
	
	public POIQueryingThread() {
		m_strReuqestData = null;
	}	

	
	@Override
	public void run() {
		BufferedReader in = null;
		OutputStreamWriter os = null;
		String strLine = null;
		StringBuffer strBufJSONResponse = new StringBuffer();
		ArrayList<POI> lstPlaces = null;
		
		// If no response handler is specified, then quit!
		if( m_responseHandler == null )
			return;
		
		try {			
			URL url = new URL(Constants.KAILOS_POI_SERVER);
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setDoOutput(true);
			
			connection.setConnectTimeout(Constants.SOCKET_TIME_OUT_MS);
			connection.setReadTimeout(Constants.SOCKET_TIME_OUT_MS);
			
			connection.connect();
			
			os = new OutputStreamWriter(connection.getOutputStream());
			
			os.write(m_strReuqestData);
			os.flush();
			
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			 
			while ((strLine = in.readLine()) != null) 
				strBufJSONResponse.append(strLine);
			
			lstPlaces = parseResponse(strBufJSONResponse.toString());
			
			
		} catch( Exception e ) {
			// ...
		} finally {			
			try {
				if( os != null )
					os.close();
				
				if( in != null )
					in.close();
			} catch( Exception e ) {
				// Nothing to do...
			}
			
			Message msg = new Message();
			
			msg.what = Constants.MESSAGE_POI_QUERY_THREAD_RESPONSE;
			msg.obj = (Object)lstPlaces;
			
			m_responseHandler.sendMessage(msg);
			
		}
	}
	
	public ArrayList<POI> parseResponse(String strResponse) {
		
		JSONObject json = (JSONObject) JSONValue.parse(strResponse);
		Iterator<?> iter = json.keySet().iterator();
		
		ArrayList<POI> lstRet = new ArrayList<POI>();
		
		while( iter.hasNext() ) {						
			String key = (String) iter.next();
			Object value = json.get(key);
			
			if( key.compareToIgnoreCase("poi") == 0 ) {
				JSONArray poiArray = (JSONArray)value;

				for(int i=0; i<poiArray.size(); i++) {
					JSONObject poiJSON = (JSONObject)poiArray.get(i);
					POI newPoi = new POI();
					
					Iterator<?> poiIter = poiJSON.keySet().iterator();
					
					while( poiIter.hasNext() ) {
						String poiKey = (String) poiIter.next();
						Object poiValue = poiJSON.get(poiKey);
						
						if( poiKey.compareToIgnoreCase("category") == 0 ) {
							newPoi.setCategory(poiValue.toString());	
						} else if( poiKey.compareToIgnoreCase("floorId") == 0 ) {
							newPoi.setFloorID(poiValue.toString());
						} else if( poiKey.compareToIgnoreCase("geometry") == 0 ) {
							JSONObject geometryObject = (JSONObject)poiValue;
							Iterator<?> geometryIter = geometryObject.keySet().iterator();
							
							while( geometryIter.hasNext() ) {
								String geometryKey = (String)geometryIter.next();
								Object geometryValue = geometryObject.get(geometryKey);
								
								if( geometryKey.compareToIgnoreCase("type") == 0 ) {
									// Nothing to do.. yet? probably never?									
								} else if( geometryKey.compareToIgnoreCase("coordinates") == 0 ) {
									JSONArray coordinates = (JSONArray)geometryValue;
									
									// Default value... to check error...
									newPoi.setLatitude(-99999.0f);
									
									// Not a formal GeoJSON specification!!!!!
									if( coordinates.size() == 1 ) {
										JSONArray coordinate = (JSONArray) coordinates.get(0);
										
										if( coordinate.size() == 2 ) {
											newPoi.setLongitude(Double.parseDouble(coordinate.get(0).toString()));
											newPoi.setLatitude(Double.parseDouble(coordinate.get(1).toString()));
										} 
									} 
								}
							}							
						} else if( poiKey.compareToIgnoreCase("name") == 0 ) {
							JSONObject nameObject = (JSONObject)poiValue;
							Iterator<?> nameIter = nameObject.keySet().iterator();
							
							while( nameIter.hasNext() ) {
								String nameKey = (String)nameIter.next();
								Object nameValue = (Object)nameObject.get(nameKey);
								
								if( nameKey.compareToIgnoreCase("kr") == 0 ) {
									newPoi.setPlaceNameKor(nameValue.toString());
								} else if( nameKey.compareToIgnoreCase("en") == 0 ) {
									newPoi.setPlaceNameEng(nameValue.toString());
								}
							}
						} else if( poiKey.compareToIgnoreCase("roomNumber") == 0 ) {
							newPoi.setRoomNumber(poiValue.toString());
						}						
					}
					
					// Add the parsed poi into the list...										
					if( newPoi.getLatitude() != -99999.0f )					
						lstRet.add(newPoi);
				}
			}						
		}		
		
		return lstRet;
	}
}
