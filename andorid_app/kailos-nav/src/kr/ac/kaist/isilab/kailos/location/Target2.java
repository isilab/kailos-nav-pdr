package kr.ac.kaist.isilab.kailos.location;

import kr.ac.kaist.isilab.kailos.roadnetwork.RoadNetworkManager;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class Target2 {


	/**
	 *	store the latest estimated location from WPS 
	 */
	private EstimatedLocation m_wifiLocation;

	@Deprecated
	private LatLng m_curLocation;

	/**fused current location */
	private double m_curLng;

	/**fused current location */
	private double m_curLat;
	
	/**fused current location */
	private double m_prevLng = Double.MAX_VALUE;

	/**fused current location */
	private double m_prevLat = Double.MAX_VALUE;

	private boolean isInit = false; 

	/**
	 *  finally fused map matched location
	 */
	private EstimatedLocation m_mm_curLocation;

	/**
	 * angle for heading
	 */
	private double m_angle = Double.MAX_VALUE;

	/* below value is used as weight when fused location is estimated*/
	private static final float ERROR_WIFI = 10;
	private static final float ERROR_PDR = 1;
	private static final float ERROR_MM = 10;
	private static final float ERROR_MM5 = 50;
	//private static final double STEP_LENGTH = 6.950719387441772E-6;			// This value should be recalculated
	private static final double STEP_LENGTH = 6.42319E-06;			// This value should be recalculated

	private static final int FUSE_SIZE = 6;
	private static final int L_PDR = 0;
	private static final int L_WIFI = 1;
	private static final int L_PDR_MM = 2;
	private static final int L_PDR_M_MM = 3;
	private static final int L_PDR_R_MM = 4;
	private static final int L_PDR_L_MM = 5;

	public static LatLng locPdrM;

	public static LatLng[] fuseSrcLoc = new LatLng[FUSE_SIZE];
	public static double[] fuseSrcLocErr = new double[FUSE_SIZE];

	@Deprecated
	private LatLng m_velocity;		// wgs84 per seconds. This is used for heading
	@Deprecated
	private long m_curTime;			// nano
	@Deprecated
	public static final double MOVING_VELOCITY = 6.950719387441772E-6;	

	public Target2() {
		super();
	}


	/**
	 * @param location
	 * 	the location that is coming from notifyWiFi@KAILOSFusedLocationService
	 * @param azimuth
	 * 	the azimuth value
	 * @description
	 * 	this is used to initialize first position and heading
	 */
	public boolean initialize(long timeStamp, EstimatedLocation location, double azimuth){
		if(isInit == false){
			m_velocity = Smoothor.getHeading(location, azimuth);
			m_wifiLocation = location;
			/*m_curLocation.longitude = location.getLongitude();
			m_curLocation.latitude = location.getLatitude();
			 */
			m_curLng = location.getLongitude();
			m_curLat = location.getLatitude();

			m_curTime = timeStamp;
			m_angle = azimuth - Smoothor.ADDED_ANGLE - 90;

			Log.d("ANGLE", "modified angle : " + m_angle);

			isInit = true;

			return true;
		}
		else
			return false;
	}

	public void notifyWifi(long timeStamp, EstimatedLocation location){
		if(m_wifiLocation == null)
			return;

		m_wifiLocation.setLongitude(location.getLongitude());
		m_wifiLocation.setLatitude(location.getLatitude());
		//		m_curTime = timeStamp;
		/*double lng = location.getLongitude() - m_wifiLocation.getLongitude();
		double lat = location.getLatitude() - m_wifiLocation.getLatitude();
		m_velocity = convertToScaledVelocity(lng, lat); */
		/*		return ;*/ 
	}

	public void notifyTurn(long timeStamp, double degree){
		if(m_angle == Double.MAX_VALUE)
			return;

		m_angle += degree;
		//		m_curTime = timeStamp;
		/*		m_velocity = Smoothor.getRotatedHeading(m_velocity.longitude, m_velocity.latitude, Math.toRadians(degree));*/
	}

	public void notifyStep(long timeStamp){
		if(isInit == false)
			return;
		
/*		if(m_prevLng != Double.MAX_VALUE){
			int compensatedAngle = getCompensationAngle(doMamMatching(m_prevLng, m_prevLat), fuseSrcLoc[L_PDR_MM], fuseSrcLoc[L_PDR]);
			m_angle -= compensatedAngle;
			Log.d("ANGLE2", "conpensate angle : " + compensatedAngle);
		}*/

		/*		LatLng[] fuseSrcLoc = new LatLng[FUSE_SIZE];
		double[] fuseSrcLocErr = new double[FUSE_SIZE];*/

		// wifi location
		fuseSrcLoc[L_WIFI] = new LatLng(m_wifiLocation.getLatitude(), m_wifiLocation.getLongitude());
		double wifiBearing = Math.toDegrees(
				getAngle(m_curLng, m_curLat, 
						m_wifiLocation.getLongitude(), m_wifiLocation.getLatitude()));
		fuseSrcLocErr[L_WIFI] = (float) (ERROR_WIFI * (Math.abs(wifiBearing - m_angle)/50));

		// pdr
		fuseSrcLoc[L_PDR] = getAhead(
				m_curLng, m_curLat, 
				STEP_LENGTH, m_angle);
		fuseSrcLocErr[L_PDR] = ERROR_PDR;

		// pdr mm
		fuseSrcLoc[L_PDR_MM] = doMamMatching(fuseSrcLoc[L_PDR].longitude, fuseSrcLoc[L_PDR].latitude);
		fuseSrcLocErr[L_PDR_MM] = ERROR_MM;

		// pdr middle after 5 steps
		locPdrM = getAhead(
				m_curLng, m_curLat, 
				STEP_LENGTH*5, m_angle);

		fuseSrcLoc[L_PDR_M_MM] = doMamMatching(locPdrM.longitude, locPdrM.latitude);
		fuseSrcLocErr[L_PDR_M_MM] = ERROR_MM5;

		// pdr right after 5 steps
		LatLng 	locPdrR = getAhead(
				m_curLng, m_curLat, 
				STEP_LENGTH*5, m_angle-20);

		fuseSrcLoc[L_PDR_R_MM] = doMamMatching(locPdrR.longitude, locPdrR.latitude);
		fuseSrcLocErr[L_PDR_R_MM] = ERROR_MM5;


		// pdr left after 5 steps
		LatLng 	locPdrL = getAhead(
				m_curLng, m_curLat, 
				STEP_LENGTH*5, m_angle+20);

		fuseSrcLoc[L_PDR_L_MM] = doMamMatching(locPdrL.longitude, locPdrL.latitude);
		fuseSrcLocErr[L_PDR_L_MM] = ERROR_MM5;

		LatLng fusedLoc = fuseLocations(fuseSrcLoc, fuseSrcLocErr);

		m_prevLng = m_curLng;
		m_prevLat = m_curLat;
		
		m_curLng = fusedLoc.longitude;
		m_curLat = fusedLoc.latitude;
		
		// conpensate angle
		
		
		
		/*		m_curLocation.setLongitude(fusedLoc.longitude);
		m_curLocation.setLatitude(fusedLoc.latitude);
		 */		
		/*		LatLng fusedMMLoc = doMamMatching(fusedLoc.longitude, fusedLoc.latitude);

		m_mm_curLocation.setLongitude(fusedMMLoc.longitude);
		m_mm_curLocation.setLatitude(fusedMMLoc.latitude);*/

	}

	private int getCompensationAngle(LatLng refSrc, LatLng refDst, LatLng normalHeading){
		double refVectLng = refDst.longitude - refSrc.longitude;
		double refVectLat = refDst.latitude - refSrc.latitude;
		
		double normalVectLng = normalHeading.longitude - refSrc.longitude;
		double normalVectLat = normalHeading.latitude - refSrc.latitude;
		
		return getDirectionOfTwoVectors(refVectLng, refVectLat,	normalVectLng, normalVectLat);
	}

	private int getDirectionOfTwoVectors(double dX1, double dY1, double dX2, double dY2){
		double dMagnitude1 = Math.sqrt(dX1 * dX1 + dY1 * dY1);
		double dMagnitude2 = Math.sqrt(dX2 * dX2 + dY2 * dY2);

		double dNormalizedX1 = dX1 / dMagnitude1;
		double dNormalizedY1 = dY1 / dMagnitude1;

		double dNormalizedX2 = dX2 / dMagnitude2;
		double dNormalizedY2 = dY2 / dMagnitude2;

		return (dNormalizedX1 * dNormalizedY2 - dNormalizedY1 * dNormalizedX2) > 0 ? -5 : 5; 
	}   

	/**
	 * @param fuseSrcLoc
	 * @param fuseSrcLocErr
	 * @return
	 * @description
	 * 	fusing location
	 */
	private LatLng fuseLocations(LatLng[] fuseSrcLoc, double[] fuseSrcLocErr){

		double lng = 0, lat = 0;
		double esum = 0;

		// compute inverted-error sum;
		for(int i = 0 ; i < fuseSrcLocErr.length ; i++){
			esum += (1 / fuseSrcLocErr[i]);
		}

		// weighted average of locations
		for(int i = 0 ; i < fuseSrcLoc.length ; i++){
			LatLng latLng = fuseSrcLoc[i];
			double w = (1 / fuseSrcLocErr[i]) / esum;		// weight

			lng += latLng.longitude * w;
			lat += latLng.latitude * w;
		}

		return new LatLng(lat, lng);
	}

	/**
	 * @param timeStamp
	 * @param location
	 * @description
	 * 	when map matching function call
	 */
	@Deprecated
	private void notifyMapMatching(long timeStamp, EstimatedLocation location){
		if(location == m_wifiLocation)
			return;

		double lat = location.getLatitude() - m_wifiLocation.getLatitude();
		double lng = location.getLongitude() - m_wifiLocation.getLongitude();
		m_velocity = convertToScaledVelocity(lng, lat); 
	}

	/**
	 * @param timeStamp
	 * @param location : Map matched location
	 * @return
	 * @description
	 */
	@Deprecated
	public EstimatedLocation getCalculatedLocation(long timeStamp, EstimatedLocation location){
		if(m_wifiLocation == null)
			return null;

		notifyMapMatching(timeStamp, location);

		double timeInterval = (timeStamp - m_curTime)/1000000000.0;

		// current position + movedLength
		double lng = m_wifiLocation.getLongitude() + m_velocity.longitude*timeInterval;
		double lat = m_wifiLocation.getLatitude() + m_velocity.latitude*timeInterval;

		m_wifiLocation.setLongitude(lng);
		m_wifiLocation.setLatitude(lat);

		m_curTime = timeStamp;

		return m_wifiLocation;
	}

	@Deprecated
	private LatLng convertToScaledVelocity(double lng, double lat){
		double raito = MOVING_VELOCITY/Math.sqrt(lat*lat + lng*lng);
		return new LatLng(lat*raito, lng*raito);
	}

	/**
	 * @param lng
	 * @param lat
	 * @return  
	 * @description
	 * 	temporary method. This will be implemented by RM.
	 */
	private LatLng doMamMatching(double lng, double lat){

		Log.d("LNGLAT", "lng : " + lng + ", lat :" + lat);

		LatLng mapmatched = RoadNetworkManager.FindNearestLocationOnRoad(new LatLng(lat, lng));

		Log.d("LNGLAT", "lng : " + mapmatched.longitude + ", lat :" + mapmatched.latitude);
		return mapmatched;
		// return RoadNetworkManager.FindNearestLocationOnRoad(new LatLng(lat, lng));
		// return new LatLng(lat, lng);
	}

	private LatLng getAhead(double lng, double lat, double stride, double degree){
		double newLng = lng + stride * Math.cos(Math.toRadians(degree));
		double newLat = lat + stride * Math.sin(Math.toRadians(degree));
		return new LatLng(newLat, newLng);
	}

	/**
	 * @return returns as Radians
	 * @description
	 */
	private double getAngle(double lng1, double lat1, double lng2, double lat2){
		double dLng = lng2 - lng1;
		double dLat = lat2 - lat1;
		return Math.atan2(dLat, dLng);
	}


	/**
	 * @return
	 * @description
	 * 	This function the location of the fused and followed by MMed location
	 */
	public LatLng getFusedLocation() {
		if(isInit)
			return doMamMatching(m_curLng, m_curLat);
		return null;
	}

	/**
	 * @return	
	 * @description
	 * 	This method is temporary
	 */
	public EstimatedLocation tGetLoc(){

		if(isInit){
			LatLng tLatLng = doMamMatching(m_curLng, m_curLat);
			EstimatedLocation tLoc = new EstimatedLocation();
			tLoc.setLongitude(tLatLng.longitude);
			tLoc.setLatitude(tLatLng.latitude);
			return tLoc;
		}

		return null;

	}

	public TargetInfo getTargetInfo(){

		if(isInit){
			// LatLng curPosition = new LatLng(m_curLat, m_curLng);
			LatLng curPosition = doMamMatching(m_curLng, m_curLat);
			LatLng curHeading = getAhead(curPosition.longitude, curPosition.latitude, STEP_LENGTH*3, m_angle);

			return new TargetInfo(
					curPosition.longitude, curPosition.latitude, 
					curHeading.longitude, curHeading.latitude);
		}

		return null;

		/*		if(isInit){
			LatLng curPosition = doMamMatching(m_curLng, m_curLat);
			LatLng curHeading = getAhead(curPosition.longitude, curPosition.latitude, STEP_LENGTH*3, m_angle);

			return new TargetInfo(
					curPosition.longitude, curPosition.latitude, 
					curHeading.longitude, curHeading.latitude);
		}

		return null;*/

	}


	public LatLng[] getFuseSrcLoc() {
		return fuseSrcLoc;
	}


	public double[] getFuseSrcLocErr() {
		return fuseSrcLocErr;
	}


	public void setFuseSrcLoc(LatLng[] fuseSrcLoc) {
		this.fuseSrcLoc = fuseSrcLoc;
	}


	public void setFuseSrcLocErr(double[] fuseSrcLocErr) {
		this.fuseSrcLocErr = fuseSrcLocErr;
	}



}
