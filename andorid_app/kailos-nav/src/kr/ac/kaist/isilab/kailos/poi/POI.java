package kr.ac.kaist.isilab.kailos.poi;

import com.google.android.gms.maps.model.LatLng;

public class POI {
	private String m_strCategory;			// What the fuck are these guys are thinking?
	
	private String m_strFloorID;			// Which floor this place is actually on?
	
	private double m_dbLatitude;			// A latitude on the floor..
	private double m_dbLongitude;			// A longitude on the floor.
	
	private String m_strPlaceNameKor;		// The name of the place in Korean..
	private String m_strPlaceNameEng;		// The name of the place in Englsih..
	private String m_strRoomNumber;			// Room Number;
	
	public POI() {
		m_strCategory = "";		
		m_strFloorID = "";
		
		m_dbLatitude = 0.0f;
		m_dbLongitude = 0.0f;
		
		m_strPlaceNameKor = "";
		m_strPlaceNameEng = "";
	}


	public String getCategory() {
		return m_strCategory;
	}


	public void setCategory(String m_strCategory) {
		this.m_strCategory = m_strCategory;
	}


	public String getFloorID() {
		return m_strFloorID;
	}


	public void setFloorID(String m_strFloorID) {
		this.m_strFloorID = m_strFloorID;
	}


	public double getLatitude() {
		return m_dbLatitude;
	}


	public void setLatitude(double m_dbLatitude) {
		this.m_dbLatitude = m_dbLatitude;
	}


	public double getLongitude() {
		return m_dbLongitude;
	}


	public void setLongitude(double m_dbLongitude) {
		this.m_dbLongitude = m_dbLongitude;
	}


	public String getPlaceNameKor() {
		return m_strPlaceNameKor;
	}


	public void setPlaceNameKor(String m_strPlaceNameKor) {
		this.m_strPlaceNameKor = m_strPlaceNameKor;
	}


	public String getPlaceNameEng() {
		return m_strPlaceNameEng;
	}


	public void setPlaceNameEng(String m_strPlaceNameEng) {
		this.m_strPlaceNameEng = m_strPlaceNameEng;
	}
	
	public String getRoomNumber() {
		return this.m_strRoomNumber;
	}
	
	public void setRoomNumber(String m_strRoomNumber) {
		this.m_strRoomNumber = m_strRoomNumber;
	}
	
	public LatLng getLatLng() {
		return new LatLng(m_dbLatitude, m_dbLongitude);
	}
}
