package kr.ac.kaist.isilab.kailos.location;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class Target {
	
	private EstimatedLocation m_location;
	private EstimatedLocation m_curLocation;
	private EstimatedLocation m_prevLocation;
	private LatLng m_velocity;
	private int m_cntStep = 0;
	
	
//	private static int dropRatio = 10;
	
	public void notifyWifi(EstimatedLocation location){
		
		m_prevLocation = m_curLocation;
		m_curLocation = location;
		
		if(m_location == null){
			m_location = location;
		}
		
		if(m_prevLocation != null){
			if(m_location == null)
				m_location = m_prevLocation;
			
			double x, y;
			x = (m_curLocation.getLongitude() - m_prevLocation.getLongitude())/4;
			y = (m_curLocation.getLatitude() - m_prevLocation.getLatitude())/4;
			setVelocity(new LatLng(y, x));
			
			Log.d("VELOCITY", "lng : " + x + ", lat : " + y);
			Log.d("VELOCITY", "size : " + Math.sqrt(x*x + y*y));
		}
	
	}
	
	public void notifyStep(){
		this.setCntStep(this.getCntStep() + 1);
	}
	
	public EstimatedLocation getSmoothingLocation(){
		if(updateCurLocation())
			return m_location;
		return null;
	}
	
	private boolean updateCurLocation(){
		
		if(m_velocity != null){
			LatLng velocity = this.getVelocity();
			double lng = m_location.getLongitude();
			double lat = m_location.getLatitude();
			
			lng += velocity.longitude*0.1;
			lat += velocity.latitude*0.1;
			
			m_location.setLongitude(lng);
			m_location.setLatitude(lat);
			
			return true;
		}
		
		return false;
	}


/*	public EstimatedLocation getLocation() {
		return m_location;
	}*/

	public LatLng getVelocity() {
		return m_velocity;
	}

	public EstimatedLocation getPrevLocation() {
		return m_prevLocation;
	}
	
	public int getCntStep(){
		return this.m_cntStep;
	}

	public void setLocation(EstimatedLocation m_location) {
		this.m_location = m_location;
	}

	public synchronized void setVelocity(LatLng m_velocity) {
		this.m_velocity = m_velocity;
	}

	public synchronized void setPrevLocation(EstimatedLocation m_prevLocation) {
		this.m_prevLocation = m_prevLocation;
	}

	public synchronized void setCntStep(int cntStep){
		this.m_cntStep = cntStep;
	}
	
}
