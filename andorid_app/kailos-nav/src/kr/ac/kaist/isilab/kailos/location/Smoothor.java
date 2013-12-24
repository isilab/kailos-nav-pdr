package kr.ac.kaist.isilab.kailos.location;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class Smoothor {

	private List<Double> listAzimuth = new ArrayList<Double>();
	private boolean isFirstWifiScan = true;;
/*	private double m_azimuthMean = 0;
*/
	private static final double MAG_NORTH_LNG = -147;
	private static final double MAG_NORTH_LAT = 85.9;
	
	//public static final double ADDED_ANGLE = 20.5;
	public static final double ADDED_ANGLE = 35;
	
	public void addAzimuth(double azimuth){
		listAzimuth.add(azimuth);
	}

	public double getMeanOfAzimuth(){

		int size = listAzimuth.size();
		double sum = 0;
		for(int i = 0 ; i < size ; i++){
			sum += listAzimuth.get(i);
		}

		Log.d("azimuth", "average value : " + sum/size);

		return sum/size;

	}
	
	public static LatLng getHeading(EstimatedLocation currentLocation, double azimuth){
		
/*		if(isFirstWifiScan == false)
			return null;*/
		
		double radian = Math.toRadians(azimuth + ADDED_ANGLE);

		double curLng = currentLocation.getLongitude();
		double curLat = currentLocation.getLatitude();

		double vectLngToMagNorth = MAG_NORTH_LNG - curLng;
		double vectLatToMagNorth = MAG_NORTH_LAT - curLat;

		double norm = Math.sqrt(vectLngToMagNorth*vectLngToMagNorth + vectLatToMagNorth*vectLatToMagNorth);

		double unitVectLngToMagNorth = (MAG_NORTH_LNG - curLng)/norm;
		double unitVectLatToMagNorth = (MAG_NORTH_LAT - curLat)/norm;
		
		LatLng vel = getRotatedHeading(unitVectLngToMagNorth, unitVectLatToMagNorth, -1*(radian));
		return new LatLng(vel.latitude*Target2.MOVING_VELOCITY, vel.longitude*Target2.MOVING_VELOCITY);
/*
		double 
			a = getFirstCoeffi(unitVectLngToMagNorth, unitVectLatToMagNorth), 
			b = getSecondCoeffi(unitVectLngToMagNorth, unitVectLatToMagNorth, azimuthMean),
			c = getThirdCoeffi(unitVectLatToMagNorth, azimuthMean);

		double[] 
			lngs = getLngHeading(a, b, c),
			lats = getLatHeading(lngs);
		
		isFirstWifiScan = false;
		LatLng heading = computeHeading(unitVectLngToMagNorth, unitVectLatToMagNorth, lngs[0], lats[0], lngs[1], lats[1]);
		
		

	public LatLng getHeadingVector(EstimatedLocation currentLocation){
		
		if(isFirstWifiScan == false)
			return null;
		
		double azimuthMean = getMeanOfAzimuth() + ADDED_ANGLE;

		double curLng = currentLocation.getLongitude();
		double curLat = currentLocation.getLatitude();

		double vectLngToMagNorth = MAG_NORTH_LNG - curLng;
		double vectLatToMagNorth = MAG_NORTH_LAT - curLat;

		double norm = Math.sqrt(vectLngToMagNorth*vectLngToMagNorth + vectLatToMagNorth*vectLatToMagNorth);

		double unitVectLngToMagNorth = (MAG_NORTH_LNG - curLng)/norm;
		double unitVectLatToMagNorth = (MAG_NORTH_LAT - curLat)/norm;
		
		return getHeading(unitVectLngToMagNorth, unitVectLatToMagNorth, azimuthMean);
/*
		double 
			a = getFirstCoeffi(unitVectLngToMagNorth, unitVectLatToMagNorth), 
			b = getSecondCoeffi(unitVectLngToMagNorth, unitVectLatToMagNorth, azimuthMean),
			c = getThirdCoeffi(unitVectLatToMagNorth, azimuthMean);

		double[] 
			lngs = getLngHeading(a, b, c),
			lats = getLatHeading(lngs);
		
		isFirstWifiScan = false;
		LatLng heading = computeHeading(unitVectLngToMagNorth, unitVectLatToMagNorth, lngs[0], lats[0], lngs[1], lats[1]);
		
		return new LatLng(heading.latitude*scale, heading.longitude*scale);*/
	}
	
	public static LatLng getRotatedHeading(double refVectLng, double refVectLat, double radian){
		double lng = refVectLng * Math.cos(radian) + (-1) * refVectLat * Math.sin(radian);
		double lat = refVectLng * Math.sin(radian) + refVectLat * Math.cos(radian);
		return new LatLng(lat, lng);
	}

/*	private double getFirstCoeffi(double lng, double lat){
		return 1 + lng*lng / (lat * lat);
	}

	private double getSecondCoeffi(double lng, double lat, double azimuth){
		return -2*lng*Math.cos(Math.toRadians(azimuth))/(lat*lat);
	}

	private double getThirdCoeffi(double lat, double azimuth){
		double cosine = Math.cos(Math.toRadians(azimuth)); 
		return cosine*cosine/(lat*lat) - 1;
	}

	private double[] getLngHeading(double a, double b, double c){
		double[] lngs = new double[2];
		lngs[0] = (-1*b + Math.sqrt(b*b - 4*a*c))/(2*a);
		lngs[1] = (-1*b - Math.sqrt(b*b - 4*a*c))/(2*a);

		return lngs;
	}
	
	private double[] getLatHeading(double[] lngs){
		double[] lats = new double[2];
		lats[0] = Math.sqrt(1-lngs[0]*lngs[0]);
		lats[1] = -1 * lats[0];

		return lats;
	}*/
	
/*	private static LatLng getHeading(double refVectLng, double refVectLat, double azimuth){
		double azimuthInRadian = Math.toRadians(azimuth * -1);
		return getRotatedHeading(refVectLng, refVectLat, azimuthInRadian);
	}*/
	


	
	/**
	 * @param refLng
	 * @param refLat
	 * @param cand1Lng
	 * @param cand1Lat
	 * @param cand2Lng
	 * @param cand2Lat
	 * @return
	 * @description
	 *  followed http://gamedev.stackexchange.com/questions/45412/understanding-math-used-to-determine-if-vector-is-clockwise-counterclockwise-f
	 *//*
	private LatLng computeHeading(double refLng, double refLat, double cand1Lng, double cand1Lat, double cand2Lng, double cand2Lat){
		if(refLat * cand1Lng > refLng * cand1Lat)
			return new LatLng(cand1Lat, cand1Lng);
		return new LatLng(cand2Lat, cand2Lng);
	}
*/
}
