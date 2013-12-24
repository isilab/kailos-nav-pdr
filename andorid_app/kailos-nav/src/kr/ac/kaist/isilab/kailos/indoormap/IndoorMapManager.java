/*package kr.ac.kaist.isilab.kailos.indoormap;

import android.os.Handler;
import kr.ac.kaist.isilab.kailos.util.Constants;

public class IndoorMapManager {
	// Constructors...
	public IndoorMapManager() {
	}	
	
	// Member functions..
	//----------------------------------------------------------------------------------------------------
	// - Description : This function is called to request the representative floor map 
	//                of the neatest building from a given coordinate..  
	public void requestNearIndoorMaps(double dbLatitude, double dbLongitude, int nType, Handler resultHandler) {		
		StringBuffer strJSONRequestData = new StringBuffer();
		
		// Build jSON object for requesting...
		strJSONRequestData.append("{");
		
		if( nType == Constants.MAP_REQUEST_NEAREST_ENTIRE_BUILDING )
			strJSONRequestData.append("\"queryType\": \"NEAREST_ENTIRE\",");
		else
			strJSONRequestData.append("\"queryType\": \"NEAREST_REPRESENTATIVE\",");
		
		strJSONRequestData.append("\"lng\": \"" + dbLongitude + "\",");
		strJSONRequestData.append("\"lat\": \"" + dbLatitude + "\"");
		
		strJSONRequestData.append("}");
		
		// Create a thread..
		IndoorMapRequestThread thread = new IndoorMapRequestThread();
		
		thread.setRequestJSONData(strJSONRequestData.toString());
		thread.setResponseHandler(resultHandler);
		
		thread.start();
	}
	
	//----------------------------------------------------------------------------------------------------
	// - Description : This function is called to request the floor map with a given floorID..
	public void requestIndoorMapWithFloorID(double dbFloorID, Handler resultHandler) {
		StringBuffer strJSONRequestData = new StringBuffer();
		
		// Build jSON object for requesting...
		strJSONRequestData.append("{\"queryType\": \"FLOOR_ID\",");
		
		strJSONRequestData.append("\"lng\": \"0.000\",");
		strJSONRequestData.append("\"lat\": \"0.000\",");
		strJSONRequestData.append("\"floorID\": \"" + dbFloorID + "\"");
		
		strJSONRequestData.append("}");
		
		// Create a thread..
		IndoorMapRequestThread thread = new IndoorMapRequestThread();
		
		thread.setRequestJSONData(strJSONRequestData.toString());
		thread.setResponseHandler(resultHandler);
		thread.setUpdateFloorNameIndicator(true);
		
		thread.start();
	}
	
	//----------------------------------------------------------------------------------------------------
	// - Description : This function is called to request the floor map with a given floorID...
	public void requestIndoorMapWithFloorID(String strFloorID, Handler resultHandler) {
		StringBuffer strJSONRequestData = new StringBuffer();
		
		// Build jSON object for requesting...
		strJSONRequestData.append("{\"queryType\": \"FLOOR_ID\",");
		
		strJSONRequestData.append("\"lng\": \"0.000\",");
		strJSONRequestData.append("\"lat\": \"0.000\",");
		strJSONRequestData.append("\"floorID\": \"" + strFloorID + "\"");
		
		strJSONRequestData.append("}");
		
		// Create a thread..
		IndoorMapRequestThread thread = new IndoorMapRequestThread();
		
		thread.setRequestJSONData(strJSONRequestData.toString());
		thread.setResponseHandler(resultHandler);
		thread.setUpdateFloorNameIndicator(true);
		
		thread.start();
	}
	
}*/
