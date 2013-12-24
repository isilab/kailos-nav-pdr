package kr.ac.kaist.isilab.kailos.navi;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import kr.ac.kaist.isilab.kailos.indoormap.IndoorMap;
import kr.ac.kaist.isilab.kailos.indoormap.IndoorMapRequestThread;
import kr.ac.kaist.isilab.kailos.location.EstimatedLocation;
import kr.ac.kaist.isilab.kailos.location.LocationRequestThread;
import kr.ac.kaist.isilab.kailos.location.TargetInfo;
import kr.ac.kaist.isilab.kailos.pdr.StepDetector;
import kr.ac.kaist.isilab.kailos.pdr.TurnDetector;
import kr.ac.kaist.isilab.kailos.roadnetwork.RoadNetworkManager;
import kr.ac.kaist.isilab.kailos.roadnetwork.RoadNetworkRequestThread;
import kr.ac.kaist.isilab.kailos.route.RouteManager;
import kr.ac.kaist.isilab.kailos.route.RouteVertex;
import kr.ac.kaist.isilab.kailos.util.Constants;

import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class KAILOSService extends Service {
	// Member Variables..	
	private LocationManager m_LocationManager;
	private KAILOSLocationListener m_LocationListener;	
	private GoogleWPSLocationListener m_WPSLocationListner;
	private WifiManager m_WiFiManager;
	
	private static EstimatedLocation m_currentLocation;
	
	// for listing up GPS fixes in an interval...
	private ArrayList<Location> m_lstGPSFix;

	private boolean m_bLocationTracking;
	private static boolean m_bNavigating;	
	
	private SharedPreferences m_preferences;

	public static IKAILOSService m_KAILOSServiceRPC;
	
	// For AIDL..
	private IKAILOSService.Stub m_IKAILOSService = new IKAILOSService.Stub() {
		@Override
		public void startLocalization() throws RemoteException {
			startTrackingLocation();
		}

		@Override
		public void stopLocationztion() throws RemoteException {
			stopTrackingLocation();

		}

		@Override
		public boolean isTracking() throws RemoteException {
			return m_bLocationTracking;
		}

		@Override
		public void setCurrentLocation(String strLocation) throws RemoteException {
			m_currentLocation = EstimatedLocation.createFromString(strLocation);			
		}

		@Override
		public String getCurrentLocation() throws RemoteException {
			if( m_currentLocation != null )
				return m_currentLocation.toString();			
			else
				return null;
		}
	};
	
	// Handle the current location response from the server...	
	public static Handler m_LocationUpdateHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			EstimatedLocation location = null;
			
			switch( msg.what ) {
				case Constants.MESSAGE_LOCATION_THREAD_RESPONSE:
					location = (EstimatedLocation)msg.obj;
	
					// If it is not a valid location response, quit!						
					if( location == null ) 
						return;
					
					// store the current location..
					m_currentLocation = location;
					
					// Request the corresponding indoor map and road network if they are needed...
					boolean bFloorChanged = false;
					String strCurrentFloorID = GoogleMapFragment.getIndoorMapOverlayFloorID();
	
					if( location.getFloorID().compareTo("") == 0 || strCurrentFloorID == null )		// GPS location or no current map overlay... 
						bFloorChanged = true;
					else if( strCurrentFloorID.compareTo(location.getFloorID()) != 0 ) // Floor is changed...
						bFloorChanged = true;
	
					if( bFloorChanged ) {
						// Map request...						
						IndoorMapRequestThread mapThread = new IndoorMapRequestThread();
						
						mapThread.setResponseHandler(m_MapHandler);
						
						if( location.getFloorID().compareTo("") == 0 )
							mapThread.setRequestDataWithGPSCoordinate(location.getLatitude(), location.getLongitude(), false);
						else {
							mapThread.setRequestDataWithFloorID(location.getFloorID());
							mapThread.setUpdateFloorNameIndicator(true);
						}
						
						mapThread.start();
						
						// Road network request...						
						RoadNetworkRequestThread roadThread = new RoadNetworkRequestThread();
						
						roadThread.setResponseHandler(RoadNetworkManager.m_handlerResponse);						
						roadThread.setRequestData(location.getFloorID());
						roadThread.start();
					}			
					
	               /*GoogleMapFragment.setKAILOSMarker(new LatLng(location.getLatitude(), location.getLongitude()));
	               RoadNetworkManager.FindNearestLocationOnRoad(new LatLng(location.getLatitude(), location.getLongitude()));*/
					
	//				// or it is a really lousy location estimation result..
	//				if( location.getErrorDistance() >= 25.0f ) 
	//					return;
					
					// Pass the location to the fused service...
					try {
						KAILOSFusedLocationService.m_KAILOSFusedLocationServiceRPC.notifyLocalizationResult(location.toString());
					} catch (RemoteException e) {
						// What are you going to do? shutting down the whole application? ha-ha..
					}
					
					break;					
//					// or it is a really lousy location estimation result..
//					if( location.getErrorDistance() >= 25.0f ) 
//						return;
					
					// Test
					// 127.36569669724118, 36.374090738764615;
//					location.setLatitude(36.3740901);
//					location.setLongitude(127.365620);
					
					// Test ��...
//					location.setLatitude(36.3740901);
//					location.setLongitude(127.362620);
				
					// draw estimated point
				case Constants.MESSAGE_FUSED_LOCATION_RESPONSE:
	                TargetInfo info = (TargetInfo)msg.obj;               
	                GoogleMapFragment.setCurrentFusedLocation(info);   					


					// Set the location marker!
/*					switch( GoogleMapFragment.getCurrentMode() ) {
						case Constants.NORMAL_MODE:
							//--------------- set marker
							GoogleMapFragment.setKAILOSMarker(location);
//							GoogleMapFragment.updateKAILOSMarkerLoaction(location);
							break;
							
						case Constants.NAVIGATION_MODE:
							// Shows start marker only when a user is currently on the departure floor..							
							if( location.getFloorID().compareTo(RouteManager.getDepartureFloorID()) != 0 ) {
								GoogleMapFragment.hideDepartureMarker();
							}
							
							
							// Highlights the routes on the current floor...
							GoogleMapFragment.removeAllNormalRoute();
							GoogleMapFragment.addNormalRoute(RouteManager.getRoutesOnFloor(location.getFloorID()));
							
							// Map matching...
							location = RouteManager.FindNearestLocationOnRoute(location);
							
							double dbDistanceToDestination = RouteManager.distanceToDestination(location);
							
							// Near to the destination?
							if( dbDistanceToDestination <= 7.0f ) {								
								// You have arrived at your destination.
								// Mode..
								GoogleMapFragment.setCurrentMode(Constants.NORMAL_MODE);
								
								// Hide option buttons...
								GoogleMapFragment.hideStartNavigationButton();					
								GoogleMapFragment.hideStopNavigationButton();
								
								// Hide the departure and destination markers...
								GoogleMapFragment.hideDepartureMarker();
								GoogleMapFragment.hideDestinationMarker();
								
								// Remove all indoor maps for navigation...
								GoogleMapFragment.removeAllIndoorMapOverlayForNavigation();
								
								// remove all routes which are currently showing on the map...
								GoogleMapFragment.removeAllTransparentRoute();
								GoogleMapFragment.removeAllNormalRoute();
								
								AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.m_MainActivityContext);
								alert.setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
								    @Override
								    public void onClick(DialogInterface dialog, int which) {
								    	dialog.dismiss();
								    }
								});							
								
								alert.setMessage("You have arrived at your destination!");
								alert.show();
								
							}
							else if( dbDistanceToDestination < 15.0f  ) {
								// You are near your destination..
								Toast.makeText(MainActivity.m_MainActivityContext, "You are near your destination!", Toast.LENGTH_SHORT).show();							
							}
														
							GoogleMapFragment.setKAILOSMarker(location);	
//							GoogleMapFragment.moveCamera(location, 17, 20);
							break;
					}	*/		
										
					break;
				default:
					break;
			}			
		}		
	};	
	
	
	// Handle the current indoor map response from the server...
	public static Handler m_MapHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch( msg.what ) {				
				case Constants.MESSAGE_INDOOR_MAP_THREAD_RESPONSE:					
					IndoorMap map = (IndoorMap)msg.obj;
					
					GoogleMapFragment.setIndoorMapOverlay(map);					
					break;
					
				case Constants.MESSAGE_FLOOR_NAME_INDICATOR_UPDATE:
					String strFloorName = (String)msg.obj;					
					
					GoogleMapFragment.setIndicator(strFloorName);					
					break;
					
				default:
					break;
			}
		}
	};	
	
	// Handler for drawing route...
	public static Handler m_handleRoute = new Handler() {
		public void handleMessage(Message msg) {
			switch( msg.what ) {
				case Constants.MESSAGE_DRAW_ENTIRE_ROUTE:
					// Remove all the previous routes..
					GoogleMapFragment.removeAllTransparentRoute();
					GoogleMapFragment.removeAllNormalRoute();
					GoogleMapFragment.removeAllIndoorMapOverlayForNavigation();
					
					LatLngBounds.Builder bounds = new LatLngBounds.Builder();
					
					// Draw the whole path transparently..
					
					ArrayList<RouteVertex> lstPath = RouteManager.getRoutePath();					
					
					PolylineOptions options = new PolylineOptions();
					options.color(Constants.RED_COLOR_TRANSPARENT);
					options.width(10.0f);
					options.zIndex(5);
					
					for(int i=0; i<lstPath.size(); i++) {
						RouteVertex vertex = lstPath.get(i);						
						options.add(vertex.getLocation());
						
						bounds.include(vertex.getLocation());
					}
					
					GoogleMapFragment.addTransparentRoute(options);
					
					
					// Overlap the routes on the departure floor with non-transparent color...					
					options = new PolylineOptions();
					options.color(Constants.RED_COLOR);
					options.width(10.0f);
					options.zIndex(10);
					
					ArrayList<RouteVertex> lstDepartureFloorPath = RouteManager.getDepartureFloorVertexList();
					
					for(int i=0; i<lstDepartureFloorPath.size(); i++) {
						RouteVertex vertex = lstDepartureFloorPath.get(i);
						
						options.add(vertex.getLocation());
					}
					
					GoogleMapFragment.addNormalRoute(options);
					
					
					// Overlap the routes on the destination floor with non-transparent color...
					if( RouteManager.AreDepartureAndDestinationOnTheSameBuilding() ) {
						options = new PolylineOptions();
						options.color(Constants.RED_COLOR);
						options.width(10.0f);
						options.zIndex(10);
						
						ArrayList<RouteVertex> lstDestinationFloorPath = RouteManager.getDestinationFloorVertexList();

						for(int i=0; i<lstDestinationFloorPath.size(); i++) {
							RouteVertex vertex = lstDestinationFloorPath.get(i);
							
							options.add(vertex.getLocation());						
						}
						
						GoogleMapFragment.addNormalRoute(options);						
					}
					
					// Buttons...				
					GoogleMapFragment.showStartNavigationButton();
					GoogleMapFragment.showStopNavigationButton();
					
					// Move the map camera to show all routes...
					GoogleMapFragment.getMap().animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100));
					
					break;
				default:
					break;
			}
		}
		
	};
	
	// Constructor(s)...
	public KAILOSService() {
		m_lstGPSFix = new ArrayList<Location>();
		
		m_KAILOSServiceRPC = null;
		m_bLocationTracking = false;
		m_bNavigating = false;

		m_preferences = null;		
	}
	
	// Sorts of listeners...
	private class KAILOSLocationListener implements LocationListener {
		@Override
		public void onLocationChanged(Location location) {
			synchronized( m_lstGPSFix ) {
				if( m_lstGPSFix.size() > 5 )
					m_lstGPSFix.remove(0);
				
				m_lstGPSFix.add(location);
			}			
		}

		@Override
		public void onProviderDisabled(String provider) {			
		}

		@Override
		public void onProviderEnabled(String provider) {		
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};
	
	private class GoogleWPSLocationListener implements LocationListener {
		@Override
		public void onLocationChanged(Location location) {			
			GoogleMapFragment.setGoogleWPSMarker(new LatLng(location.getLatitude(), location.getLongitude()));
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
		
	}
	
	private BroadcastReceiver m_BraodcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();						
			int nExtraWifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
			
			if( action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) ) {
				if( m_bLocationTracking == false )
					return;
			
				
				if( LocationRequestThread.m_bIsLocationRequestThreadWorking ) {
					m_WiFiManager.startScan();
					return;
				}

				// Copy the gps fixes list to send....
				ArrayList<Location> lstTemp = null;
				synchronized( m_lstGPSFix ) {
					lstTemp = m_lstGPSFix;
					m_lstGPSFix = new ArrayList<Location>();
				}
				
				// Build-up fingerprint JSON which contains the just-scanned-fingerprint and the gps fixes...			
				LocationRequestThread thread = new LocationRequestThread();
				
				thread.setRequestData(m_WiFiManager.getScanResults(), lstTemp);
				thread.setResultHandler(m_LocationUpdateHandler);
				
				thread.start();
				
				// re-scan!
				if( m_bLocationTracking )
					m_WiFiManager.startScan();			
			} else if( action.equals("android.net.wifi.WIFI_STATE_CHANGED") ) {
				if( m_preferences != null && m_preferences.getBoolean("checkbox_only_scanning", false) ) {
					if( nExtraWifiState == WifiManager.WIFI_STATE_ENABLED ) {
						// Prevent Wi-Fi from associating from all saved profiles..(Kind of neat trick!)
					    List<WifiConfiguration> m_lstWifi = m_WiFiManager.getConfiguredNetworks();
					    
					    if( m_lstWifi != null ) {
						    for(int i=0; i<m_lstWifi.size(); i++) {
						    	WifiConfiguration wifiConf = m_lstWifi.get(i);
						    	m_WiFiManager.disableNetwork(wifiConf.networkId);
						    }					    	
					    }
					}					
				}
				
				sendBroadcast(new Intent("wifi.ON_NETWORK_STATE_CHANGED"));
			}			
		}
		
	};
	
	// Member Functions..	
	private void startTrackingLocation() {
		m_bLocationTracking = true;
		
	    // For Wi-Fi scanning...
	    IntentFilter localIntentFilter = new IntentFilter("android.net.wifi.SCAN_RESULTS");
	    localIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
	    
	    registerReceiver(m_BraodcastReceiver, localIntentFilter);		    		    
		
	    // If it is turned off..
	    if( m_WiFiManager.isWifiEnabled() == false )
	    	m_WiFiManager.setWifiEnabled(true);
	    		    
		if( m_preferences != null && m_preferences.getBoolean("checkbox_only_scanning", false) ) {
			
			m_WiFiManager.setWifiEnabled(false);
		    m_WiFiManager.setWifiEnabled(true);			    
		}
	    
		// GPS Listener...
		if( m_LocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) == true ) 
			m_LocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Constants.GPS_UPDATE_INTERVAL, Constants.GPS_MINIMAL_DISTANCE, m_LocationListener);
		
		// Google WPS???
		if( m_preferences != null && m_preferences.getBoolean("checkbox_show_google_wps_result", false) ) {
			if( m_LocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true )
				m_LocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Constants.GOOGLE_WPS_UPDATE_INTERVAL, Constants.GOOGLE_WPS_MINIMAL_DISTANCE, m_WPSLocationListner);
		}
		
		m_WiFiManager.startScan();	
		
		// start PDR...
		try {
			KAILOSFusedLocationService.m_KAILOSFusedLocationServiceRPC.startPDR();
		} catch (RemoteException e) {
			// What are you going to do, eh?
		}
		
	}
	
	private void stopTrackingLocation() {
		m_bLocationTracking = false;			
		
		m_LocationManager.removeUpdates(m_LocationListener);			
		m_LocationManager.removeUpdates(m_WPSLocationListner);
		
		unregisterReceiver(m_BraodcastReceiver);
				
		GoogleMapFragment.removeGoogleWPSMarker();
		
    	if( m_preferences != null && m_preferences.getBoolean("checkbox_only_scanning", false) ) {
    		m_WiFiManager.setWifiEnabled(false);
    		m_WiFiManager.setWifiEnabled(true);	    		
    	}
    	
    	// Stop PDR...
    	try {
			KAILOSFusedLocationService.m_KAILOSFusedLocationServiceRPC.stopPDR();
		} catch (RemoteException e) {
			// What are you going to do, eh?
		}
	}
	
	public static void setNavigating(boolean newValue) {
		m_bNavigating = newValue;
	}
	
	public static boolean isNavigating() {
		return m_bNavigating;
	}
	
    @Override
    public void onCreate() {
        super.onCreate();        
    }

    @Override
    public void onDestroy() {    	
    	if( m_LocationListener != null )
    		m_LocationManager.removeUpdates(m_LocationListener);
    	
    	if( m_WPSLocationListner != null ) 
    		m_LocationManager.removeUpdates(m_WPSLocationListner);
    	
    	if( m_bLocationTracking )
    		unregisterReceiver(m_BraodcastReceiver);
    	
    	super.onDestroy();
    }    
    
    public int onStartCommand(Intent intent, int flags, int startId) {
    	// For GPS...
		m_LocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		m_LocationListener = new KAILOSLocationListener();
		m_WPSLocationListner = new GoogleWPSLocationListener();
		
	    // Turn on Wi-Fi for only scanning!
		m_WiFiManager = ((WifiManager)getSystemService("wifi"));		
		m_preferences = PreferenceManager.getDefaultSharedPreferences(KAILOSService.this);
		
    	return Service.START_NOT_STICKY;    	
    }
    
    @Override
    public IBinder onBind(Intent intent) {
    	if( IKAILOSService.class.getName().equals(intent.getAction())) {
    		return m_IKAILOSService;
    	}
    	
    	return null;
    }
}
