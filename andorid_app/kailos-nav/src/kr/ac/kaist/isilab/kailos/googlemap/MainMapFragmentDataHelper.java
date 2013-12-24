// [2013-11-21] Deprecated!
// - For the better object-oriented design!
/*package kr.ac.kaist.isilab.kailos.googlemap;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Polyline;

public class MainMapFragmentDataHelper {
	// Member Variables..
	private static GoogleMap m_GoogleMap;
	private static LocationMarkerWrapper m_Marker;
	private static GroundOverlayWrapper m_Overlay;	
	private static LocationMarkerWrapper m_GoogleWPSMarker;
	private static Polyline m_polygoneRoute;
	
	// [INPORTANT] Should be called before using this manager..
	public static void initialize() {
		m_GoogleMap = null;
		m_polygoneRoute = null;
		m_Marker = new LocationMarkerWrapper();
		m_Overlay = new GroundOverlayWrapper();
		m_GoogleWPSMarker = new LocationMarkerWrapper();
	}

	public static void setGoogleMap(GoogleMap map) {
		m_GoogleMap = map;
	}
	
	public static GoogleMap getGoogleMap() {
		return m_GoogleMap;
	}  
	
	public static Polyline getRoutePloygon() {
		return m_polygoneRoute;
	}
	
	public static void setRoutePloygon(Polyline polyline) {
		m_polygoneRoute = polyline;
	}
	
	public static LocationMarkerWrapper getMarkerWrapper() {
		return m_Marker;
	}
	
	public static GroundOverlayWrapper getOverlayWrapper() {
		return m_Overlay;
	}
	
	public static LocationMarkerWrapper getWPSMarkerWrapper() {
		return m_GoogleWPSMarker;
	}
}*/
