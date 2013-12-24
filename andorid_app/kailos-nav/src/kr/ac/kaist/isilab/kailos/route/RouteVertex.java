package kr.ac.kaist.isilab.kailos.route;

import com.google.android.gms.maps.model.LatLng;

public class RouteVertex {
	private LatLng m_latlngLocation;
	private String m_strFloorID;
	private int m_nType;
	
	public RouteVertex() {
		m_latlngLocation = null;
		m_nType = -1;
		m_strFloorID = null;
	}
	
	public RouteVertex(double dbLat, double dbLng, String strFloor) {
		m_latlngLocation = new LatLng(dbLat, dbLng);
		m_strFloorID = strFloor;
		m_nType = -1;		
	}
	
	public RouteVertex(double dbLat, double dbLng, String strFloor, int nType) {
		m_latlngLocation = new LatLng(dbLat, dbLng);
		m_strFloorID = strFloor;
		m_nType = nType;		
	}
	
	public RouteVertex(LatLng latlng, String strFloor) {
		m_latlngLocation = latlng;
		m_strFloorID = strFloor;
	}
	
	public RouteVertex(LatLng latlng, String strFloor, int nType) {
		m_latlngLocation = latlng;
		m_strFloorID = strFloor;
		m_nType = nType;
	}
	
	
	public void setLocation(LatLng newLocation) {
		m_latlngLocation = newLocation;
	}
	
	public LatLng getLocation() {
		return m_latlngLocation;
	}		
	
	public void setType(int nNewType) {
		m_nType = nNewType;
	}
	
	public int getType() {
		return m_nType;
	}
	
	public void setFloorID(String strFloor) {
		m_strFloorID = strFloor;
	}
	
	public String getFloorID() {
		return m_strFloorID;
	}
}
