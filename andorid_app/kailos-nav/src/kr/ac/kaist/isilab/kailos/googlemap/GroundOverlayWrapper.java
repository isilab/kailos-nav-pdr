// [2013-11-21] Deprecated!
// - For the better object-oriented design!
/*package kr.ac.kaist.isilab.kailos.googlemap;

import kr.ac.kaist.isilab.kailos.indoormap.IndoorMap;
import kr.ac.kaist.isilab.kailos.navi.GoogleMapFragment;

import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLngBounds;

public class GroundOverlayWrapper {
	private GroundOverlay m_overlay;
	private IndoorMap m_map;
	
	public GroundOverlayWrapper() {
		m_overlay = null;
		m_map = null;
	}
	
	public GroundOverlay getGroundOverlay() {
		return m_overlay;
	}
	
	public IndoorMap getIndoorMap() {
		return m_map;
	}
	
	public void setGroundOverlayWithCurrentMap() {
		if( m_map == null )
			return;
		
		// Remove current overlay...
		if( m_overlay != null ) {
			m_overlay.remove();		
			System.gc();			
		}
		
		// Create a new overlay...
		LatLngBounds bounds = new LatLngBounds(m_map.getSWLatLng(), m_map.getNELatLng());				
				
		m_overlay = GoogleMapFragment.m_map.addGroundOverlay(new GroundOverlayOptions().image(m_map.getBitmapDescriptor())
																						.bearing((float)m_map.getRotationalDegree())
																						.positionFromBounds(bounds)
																						.transparency(0.0f));
	}
	
	public void setIndoorMap(IndoorMap newMap) {
		m_map = newMap;
	}
}*/
