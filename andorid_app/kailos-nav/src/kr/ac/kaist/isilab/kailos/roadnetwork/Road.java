package kr.ac.kaist.isilab.kailos.roadnetwork;

import com.google.android.gms.maps.model.LatLng;

public class Road {
	private LatLng m_latlntStart;
	private LatLng m_latLngEnd;
	
	public Road(LatLng startPoint, LatLng endPoint) {
		m_latlntStart = startPoint;
		m_latLngEnd = endPoint;				
	}
	
	public Road() {
		m_latlntStart = null;
		m_latLngEnd = null;		
	}

	public LatLng getStartPoint() {
		return m_latlntStart;
	}

	public void setStartPoint(LatLng m_latlntStart) {
		this.m_latlntStart = m_latlntStart;
	}

	public LatLng getEndPoint() {
		return m_latLngEnd;
	}

	public void setEndPoint(LatLng m_latLngEnd) {
		this.m_latLngEnd = m_latLngEnd;
	}	
}
