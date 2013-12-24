// [2013-10-13] Depricated!
// D
/*package kr.ac.kaist.isilab.kailos.location;

import kr.ac.kaist.isilab.kailos.util.Constants;
import android.os.Handler;
import android.os.Message;

public class KAILOSLocationManager {
	private static EstimatedLocation locationGPS = null;
	private static EstimatedLocation locationWiFi = null;
	private static Object LOCK = new Object();				// Used as a semaphore..
		
	// A handler for location update...
	// - Description : Every location update for this location manager (e.g, Wi-Fi, GPS or vision updates)
	// 				   has to be done via this handler.
	public static Handler m_Handler = new Handler() {
		public void handleMessage(Message msg) {
			EstimatedLocation location = null;
			
			synchronized(LOCK) {
				switch( msg.what ) { 
					case Constants.MESSAGE_GPS_FIX_UPDATE:
						location = (EstimatedLocation)msg.obj;
						
						if( locationGPS == null ) {
							locationGPS = location;
						}
						else {
							synchronized (locationGPS) {
								if( locationGPS.getTimeStamp() < location.getTimeStamp() ) {
									locationGPS = location;
								}							
							}						
						}
							
						break;
					case Constants.MESSAGE_WIFI_LOCATION_UPDATE:
						location = (EstimatedLocation)msg.obj;
						
						if( locationWiFi == null ) {
							locationWiFi = location;
						}
						else {
							synchronized (locationWiFi) {
								if( locationWiFi.getTimeStamp() < location.getTimeStamp() ) {
									locationWiFi = location;
								}
							}						
						}
						
						break;
					default:
						break;
				}
			}
		}
	};
	
	public KAILOSLocationManager() {		
	}
	
	// A function to retrieve the latest location update.
	// - Description : This function is called to retrieve the latest location update
	// 				   regardless of the type of the location (i.e., regardless of GPS, Wi-Fi or etc).
	public EstimatedLocation getLatestLocation() {		
		synchronized(LOCK) {
			if( locationGPS == null &&	locationWiFi == null )
				return null;
			
			if( locationGPS == null && locationWiFi != null )
				return locationWiFi;
			
			if( locationGPS != null && locationWiFi == null )
				return locationGPS;
			
			if( locationWiFi.getTimeStamp() >= locationGPS.getTimeStamp() )
				return locationWiFi;
			
			return locationGPS;		
		}
	}
	
	// A function to retrieve the optimal current location.
	// - Description : This function is called to retrieve the optimal location based on
	//				   the result of post-processed (i.e., weighting) various updated locations. 
	public EstimatedLocation getOptimalLocation() {
		synchronized(LOCK) {
			
			return null;			
		}
	}
	
	public EstimatedLocation getGPSLocation() {
		return locationGPS;
	}
	
	public EstimatedLocation getWiFiLocation() {
		return locationWiFi;
	}	
}*/
