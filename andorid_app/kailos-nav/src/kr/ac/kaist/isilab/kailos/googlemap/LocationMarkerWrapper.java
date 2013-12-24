// [2013-11-21] Deprecated!
// - For the better object-oriented design!
/*package kr.ac.kaist.isilab.kailos.googlemap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import kr.ac.kaist.isilab.kailos.location.EstimatedLocation;
import kr.ac.kaist.isilab.kailos.navi.GoogleMapFragment;
import kr.ac.kaist.isilab.kailos.navi.MainActivity;
import kr.ac.kaist.isilab.kailos.navi.R;

public class LocationMarkerWrapper {
	private Marker m_Marker;
	private Circle m_circleErrorDistance;
	
	public LocationMarkerWrapper() {
		m_Marker = null;
		m_circleErrorDistance = null;
	}
	
	public void setMarker(EstimatedLocation location, boolean bMoveCamera) {		
		if( m_Marker == null ) {
			Bitmap markerImg = BitmapFactory.decodeResource(MainActivity.m_Resources, R.drawable.ic_kailos_marker);
			BitmapDescriptor descirptor  = BitmapDescriptorFactory.fromBitmap(markerImg);
			
			markerImg.recycle();			
			
			MarkerOptions option = new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude()))
					                                   .icon(descirptor);
			
			CircleOptions circleOptions = new CircleOptions().center(new LatLng(location.getLatitude(), location.getLongitude()))
													            .radius((int)location.getErrorDistance())
													            .fillColor(Color.parseColor("#641E90FF"))
													            .strokeColor(Color.parseColor("#1E90FF"))
													            .strokeWidth(1)
													            .zIndex(1);
			
			m_Marker = GoogleMapFragment.m_map.addMarker(option);
			m_circleErrorDistance = GoogleMapFragment.m_map.addCircle(circleOptions);
		} else {			
			m_Marker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
			
			m_circleErrorDistance.setCenter(new LatLng(location.getLatitude(), location.getLongitude()));
			m_circleErrorDistance.setRadius((int)location.getErrorDistance());			
		}
		
		if( bMoveCamera ) {
			CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
			GoogleMapFragment.m_map.animateCamera(cameraUpdate);
		}
	}
	
	public void setStockMarker(EstimatedLocation location, boolean bMoveCamera) {	
		if( m_Marker == null ) {			
			Bitmap markerImg = BitmapFactory.decodeResource(MainActivity.m_Resources, R.drawable.ic_goole_wps_marker);
			BitmapDescriptor descirptor  = BitmapDescriptorFactory.fromBitmap(markerImg);

			
			MarkerOptions  option = new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude()))
													   .icon(descirptor);			
			
			m_Marker = GoogleMapFragment.m_map.addMarker(option);			
		} else {
			m_Marker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
		}		
		
		if( bMoveCamera ) {
			CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
			GoogleMapFragment.m_map.animateCamera(cameraUpdate);
		}
	}
	
	public Marker getMarker() {
		return m_Marker;
	}
	
	public void removeMarker() {
		if( m_Marker != null ) {
			m_Marker.remove();
			m_Marker = null;
		}
	}
}
*/