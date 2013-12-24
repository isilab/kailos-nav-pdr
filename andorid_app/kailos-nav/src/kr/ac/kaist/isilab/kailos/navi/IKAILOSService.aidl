package kr.ac.kaist.isilab.kailos.navi;

interface IKAILOSService {	
	void startLocalization();
	void stopLocationztion();

	boolean isTracking();	
	
	void setCurrentLocation(String strLocation);
	String getCurrentLocation();
}