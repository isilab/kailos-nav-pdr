package kr.ac.kaist.isilab.kailos.util;

import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;

public class Constants {
	public static final LatLng KAIST = new LatLng(36.37320, 127.36506);
	
	/*	public static final String KAILOS_WPS_SERVER = "http://143.248.56.151:3000/api/location";	
	public static final String KAILOS_MAP_SERVER = "http://143.248.56.151:3000/api/map/getFloormaps";
	public static final String MAP_IMAGE_ROOT_URL = "http://143.248.56.151/";
	public static final String KAILOS_POI_SERVER = "http://143.248.56.151:3000/api/map/directory";
	public static final String KAILOS_ROUTE_SERVER = "http://143.248.56.151:3000/api/map/route";	*/	

	public static final String KAILOS_WPS_SERVER = "http://143.248.55.81:3000/api/location";	
	public static final String KAILOS_MAP_SERVER = "http://143.248.55.81:3000/api/map/getFloormaps";
	public static final String MAP_IMAGE_ROOT_URL = "http://143.248.56.151/";
	public static final String KAILOS_POI_SERVER = "http://143.248.55.81:3000/api/map/directory";
	public static final String KAILOS_ROUTE_SERVER = "http://143.248.55.81:3000/api/map/route";
	
	public static final int MESSAGE_LOCATION_THREAD_RESPONSE = 10001;
	public static final int MESSAGE_INDOOR_MAP_THREAD_RESPONSE = 10002;
	public static final int MESSAGE_POI_QUERY_THREAD_RESPONSE = 10003;
	public static final int MESSAGE_FLOOR_NAME_INDICATOR_UPDATE = 10004;
	public static final int MESSAGE_ROUTE_THREAD_RESPONSE = 10005;	
	public static final int MESSAGE_DRAW_ENTIRE_ROUTE = 10006;
	public static final int MESSAGE_DISMISS_ROUTING_CALCULATION_PROGRESS_DIALOG = 10007;
	public static final int MESSAGE_FUSED_LOCATION_RESPONSE = 10008;
	public static final int MESSAGE_FUSED_LOCATION_SMOOTHING_REDRAW = 10009;		// KSH
	public static final int MESSAGE_ROAD_NETWORK_RESPONSE = 10010;
	
	public static final int MAP_REQUEST_NEAREST_ENTIRE_BUILDING = 4001;
	public static final int MAP_REQUEST_NEAREST_REPRESENTATIVE_FLOOR= 4002;
	public static final int MAP_REQUEST_WITH_FLOOR_ID = 4003;
	
	public static final int MAX_INDOOR_MAP_COUNT = 15;
	
	public static final int FLOOR_NOT_DEFINED = -9999;
	public static final int LEVEL_NOT_DEFINED = -9999;
	
	public static final int SOCKET_TIME_OUT_MS = 10000;
	
	public static long GPS_UPDATE_INTERVAL = 1000L;
	public static long GOOGLE_WPS_UPDATE_INTERVAL = 1000L; 
	
	public static final float GPS_MINIMAL_DISTANCE = 0.0F;
	public static final float GOOGLE_WPS_MINIMAL_DISTANCE = 0.0F;
		
	public static final int NORMAL_MODE = 0;
	public static final int SHOW_ROUTE_MODE = 1;
	public static final int NAVIGATION_MODE = 2;
	
	// Colors..
	public static final int BLUE_COLOR = Color.rgb(0,162,232);
	public static final int BLUE_COLOR_TRANSPARENT = Color.argb(80,0,162,232);
	public static final int RED_COLOR = Color.rgb(237,28,36);
	public static final int RED_COLOR_TRANSPARENT = Color.argb(80,237,28,36);
	
	public static String LOG_TAG = "KAILOS_DEBUG";
	
}
