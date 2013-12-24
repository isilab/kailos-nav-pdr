package kr.ac.kaist.isilab.kailos.location;

public class TargetInfo {
	
	private double m_curLng;
	private double m_curLat;
	
	private double m_headLng;
	private double m_headLat;
	
	public TargetInfo(
			double m_curLng, 	double m_curLat, 
			double m_headLng,	double m_headLat) {
		super();
		this.m_curLng = m_curLng;
		this.m_curLat = m_curLat;
		this.m_headLng = m_headLng;
		this.m_headLat = m_headLat;
	}
	
	public double getCurLng() {
		return m_curLng;
	}
	
	public double getCurLat() {
		return m_curLat;
	}
	
	public double getHeadLng() {
		return m_headLng;
	}
	
	public double getHeadLat() {
		return m_headLat;
	}
	
	public void setCurLng(double curLng) {
		this.m_curLng = curLng;
	}
	
	public void setCurLat(double curLat) {
		this.m_curLat = curLat;
	}
	
	public void setHeadLng(double headLng) {
		this.m_headLng = headLng;
	}
	
	public void setHeadLat(double headLat) {
		this.m_headLat = headLat;
	}
	
}
