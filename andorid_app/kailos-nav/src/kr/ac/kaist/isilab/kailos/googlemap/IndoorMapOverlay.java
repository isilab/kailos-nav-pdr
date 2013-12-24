package kr.ac.kaist.isilab.kailos.googlemap;

import kr.ac.kaist.isilab.kailos.indoormap.IndoorMap;
import kr.ac.kaist.isilab.kailos.navi.GoogleMapFragment;

import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLngBounds;

public class IndoorMapOverlay {
	private GroundOverlay m_overlay;
	private IndoorMap m_indoormap;
	
	public void setIndoorMapOverlay(IndoorMap map) {
		if( map == null || map.getBitmapDescriptor() == null )
			return;
		
		m_indoormap = map;
		
		// Remove current overlay...
		if( m_overlay != null ) {
			m_overlay.remove();		
			System.gc();			
		}
		
		// Create a new overlay...
		LatLngBounds bounds = new LatLngBounds(m_indoormap.getSWLatLng(),m_indoormap.getNELatLng());				
				
		m_overlay = GoogleMapFragment.getMap().addGroundOverlay(new GroundOverlayOptions().image(m_indoormap.getBitmapDescriptor())
																						.bearing((float)m_indoormap.getRotationalDegree())
																						.positionFromBounds(bounds)
																						.transparency(0.0f)
																						.zIndex(5));
		
	}
	
	public void removeIndoorMapOverlay() {
		if( m_overlay != null ) {
			m_overlay.remove();
			m_indoormap = null; 
		}
	}
	
	public String getOverlayMapFloorID() {
		if( m_indoormap != null )
			return m_indoormap.getFloorID();
		else
			return null;
	}
}
