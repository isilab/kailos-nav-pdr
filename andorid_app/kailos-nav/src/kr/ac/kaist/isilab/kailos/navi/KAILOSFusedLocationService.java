package kr.ac.kaist.isilab.kailos.navi;

import java.util.Random;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import kr.ac.kaist.isilab.kailos.location.EstimatedLocation;
import kr.ac.kaist.isilab.kailos.location.Smoothor;
import kr.ac.kaist.isilab.kailos.location.Target;
import kr.ac.kaist.isilab.kailos.location.Target2;
import kr.ac.kaist.isilab.kailos.location.TargetInfo;
import kr.ac.kaist.isilab.kailos.pdr.StepDetector;
import kr.ac.kaist.isilab.kailos.pdr.TurnDetector;
import kr.ac.kaist.isilab.kailos.route.RouteManager;
import kr.ac.kaist.isilab.kailos.util.Constants;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.Vibrator;
import android.util.Log;

public class KAILOSFusedLocationService extends Service implements SensorEventListener {
	// Sensors..
	private SensorManager m_SensorManager;

	private Sensor m_sensorAccelerometer;
	private Sensor m_sensorGyroscope;
	private Sensor m_sensorMagneticField;

	private static final float NANO = 1.0f / 1000000000.0f;
	private float m_fPrevTimestamp;
	private float m_fAngle[];

	private float m_fAccelerometerValues[];
	private float m_fMagneticFieldValues[];

	private float m_fMatrixR[];
	private float m_fMatrixI[];

	private float m_fOrientation[];

	private StepDetector m_StepDetector;
	private TurnDetector m_TurnDetector;

	private double m_dbAzimuth;

	//	private static Target m_target = null;
	private static Target2 m_target2 = null;
	private static Smoothor m_smoother = null;

	// Java Source Code
	public static Vibrator vibe;

	private static Circle[] circle = new Circle[6]; 

	private static boolean isOn = true;
	private static Handler m_smoothingHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			if(isOn){
				switch (msg.what) {
				case Constants.MESSAGE_FUSED_LOCATION_SMOOTHING_REDRAW:
					//					EstimatedLocation smoothLocation = m_target.getSmoothingLocation();

					TargetInfo fusedMMLocation = m_target2.getTargetInfo();

					if(circle[0] != null){
						for(int i = 0 ; i < 4 ; i++){
							circle[i].remove();
						}
					}

					if(Target2.fuseSrcLoc[0] != null){

						CircleOptions circleOptions1 = new CircleOptions().center(new LatLng(Target2.fuseSrcLoc[0].latitude, Target2.fuseSrcLoc[0].longitude))
								.radius(0.1f)
								.strokeColor(Color.parseColor("#FF00FF"))
								.strokeWidth(5)
								.zIndex(15);

						CircleOptions circleOptions2 = new CircleOptions().center(new LatLng(Target2.fuseSrcLoc[1].latitude, Target2.fuseSrcLoc[1].longitude))
								.radius(0.1f)
								.strokeColor(Color.parseColor("#32CD32"))
								.strokeWidth(5)
								.zIndex(15);

						CircleOptions circleOptions3 = new CircleOptions().center(new LatLng(Target2.fuseSrcLoc[2].latitude, Target2.fuseSrcLoc[2].longitude))
								.radius(0.1f)
								.strokeColor(Color.parseColor("#0000FF"))
								.strokeWidth(5)
								.zIndex(15);

						CircleOptions circleOptions4 = new CircleOptions().center(new LatLng(Target2.locPdrM.latitude, Target2.locPdrM.longitude))
								.radius(0.1f)
								.strokeColor(Color.parseColor("#000000"))
								.strokeWidth(5)
								.zIndex(15);
/*
						CircleOptions circleOptions5 = new CircleOptions().center(new LatLng(Target2.fuseSrcLoc[4].latitude, Target2.fuseSrcLoc[4].longitude))
								.radius(0.5f)
								.strokeColor(Color.parseColor("#FFD700"))
								.strokeWidth(3)
								.zIndex(15);

						CircleOptions circleOptions6 = new CircleOptions().center(new LatLng(Target2.fuseSrcLoc[5].latitude, Target2.fuseSrcLoc[5].longitude))
								.radius(0.5f)
								.strokeColor(Color.parseColor("#DDA0DD"))
								.strokeWidth(3)
								.zIndex(15);*/

						circle[0] = GoogleMapFragment.m_map.addCircle(circleOptions1);
						circle[1] = GoogleMapFragment.m_map.addCircle(circleOptions2);
						circle[2] = GoogleMapFragment.m_map.addCircle(circleOptions3);
						circle[3] = GoogleMapFragment.m_map.addCircle(circleOptions4);
						/*	circle[4] = GoogleMapFragment.m_map.addCircle(circleOptions5);
						circle[5] = GoogleMapFragment.m_map.addCircle(circleOptions6);*/
					}

					if(fusedMMLocation != null){
						Message msgDrawing = new Message();		
						msgDrawing.what = Constants.MESSAGE_FUSED_LOCATION_RESPONSE;
						msgDrawing.obj = fusedMMLocation;
						KAILOSService.m_LocationUpdateHandler.sendMessage(msgDrawing);
					}

					Message msgSmoothing = new Message();
					msgSmoothing.what = Constants.MESSAGE_FUSED_LOCATION_SMOOTHING_REDRAW;
					m_smoothingHandler.sendMessageDelayed(msgSmoothing, 100);
					break;
				default:
					break;
				}
			}

		}

	};

	/*	private static EstimatedLocation temp;
	private static Handler m_smoothingHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			if(isOn){
				switch (msg.what) {
				case Constants.MESSAGE_FUSED_LOCATION_SMOOTHING_REDRAW:
					if(temp != null){
						randomChange(temp);
						Message msgDrawing = new Message();		
						msgDrawing.what = Constants.MESSAGE_FUSED_LOCATION_RESPONSE;
						msgDrawing.obj = temp;
						KAILOSService.m_LocationUpdateHandler.sendMessage(msgDrawing);
					}

					Message msgSmoothing = new Message();
					msgSmoothing.what = Constants.MESSAGE_FUSED_LOCATION_SMOOTHING_REDRAW;
					m_smoothingHandler.sendMessageDelayed(msgSmoothing, 100);
					break;
				default:
					break;
				}
			}

		}

	};

	private static void randomChange(EstimatedLocation temp){

		double lat = temp.getLatitude();
		double lng = temp.getLongitude();

		double random = (Math.random())/1000000;
		temp.setLongitude(lng + random);
		temp.setLatitude(lat + random);

	}*/

	// AIDL...
	public static IKAILOSFusedLocationService m_KAILOSFusedLocationServiceRPC;	
	private IKAILOSFusedLocationService.Stub m_IKAILOSFusedLocation = new IKAILOSFusedLocationService.Stub() {
		@Override
		public void startPDR() throws RemoteException {

			vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

			isOn = true;
			prepareDetectors();
			startSensors();
			//			m_target = new Target();
			m_target2 = new Target2();
			//			m_smoother = new Smoothor();

			Message msg = new Message();
			msg.what = Constants.MESSAGE_FUSED_LOCATION_SMOOTHING_REDRAW;
			m_smoothingHandler.sendMessageDelayed(msg, 100);
		}

		@Override
		public void stopPDR() throws RemoteException {
			isOn = false;
			stopSensors();
			m_target2 = null;
			//			m_target = null;
			//			m_smoothingHandler = null;
		}

		@Override
		public void notifyLocalizationResult(String strEstimatedLocation) throws RemoteException {
			EstimatedLocation location = EstimatedLocation.createFromString(strEstimatedLocation);

			notifyEstimatedLocation(location);
		}
	};

	public KAILOSFusedLocationService() {
		m_KAILOSFusedLocationServiceRPC = null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		// Getting system sensor manager..
		m_SensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);

		m_sensorAccelerometer = m_SensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		m_sensorGyroscope = m_SensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		m_sensorMagneticField = m_SensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);        
	}

	@Override
	public void onDestroy() {
		if( m_SensorManager != null )
			m_SensorManager.unregisterListener(this);

		super.onDestroy();
	}



	public int onStartCommand(Intent intent, int flags, int startId) {
		return Service.START_NOT_STICKY;
	};

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		switch( event.sensor.getType() ) {
		case Sensor.TYPE_ACCELEROMETER:
			System.arraycopy(event.values, 0, m_fAccelerometerValues, 0, 3);

			double dbNorm = 0.0f;

			dbNorm += Math.pow(event.values[0], 2.0f);
			dbNorm += Math.pow(event.values[1], 2.0f);
			dbNorm += Math.pow(event.values[2], 2.0f);

			dbNorm = Math.sqrt(dbNorm) - StepDetector.GRAVITY_ACCELERATION;				

			if( m_StepDetector.isStep(event.timestamp, dbNorm) )
				notifyStep(event.timestamp);

			break;			
		case Sensor.TYPE_GYROSCOPE:				
			if( m_fPrevTimestamp != 0 ) {
				float fDeltaT = (event.timestamp - m_fPrevTimestamp) * NANO;

				// Make it a little faster... No calculation for not used values...
				//					m_fAngle[0] += Math.toDegrees(event.values[0] * fDeltaT);
				//					m_fAngle[1] += Math.toDegrees(event.values[1] * fDeltaT);
				double dbDelta = Math.toDegrees(event.values[2] * fDeltaT);

				if( Math.abs(dbDelta) >= 1.0f){
					notifyTurn(event.timestamp, dbDelta);
				}

			}							

			m_fPrevTimestamp = event.timestamp;

			break;
			/*		case Sensor.TYPE_ORIENTATION:
			m_dbAzimuth = event.values[0];
			break;*/
		case Sensor.TYPE_MAGNETIC_FIELD:
			System.arraycopy(event.values, 0, m_fMagneticFieldValues, 0, 3);
		default:
			break;
		}

		boolean bSuccess = SensorManager.getRotationMatrix(m_fMatrixR, m_fMatrixI, m_fAccelerometerValues, m_fMagneticFieldValues);

		if( bSuccess ) {
			SensorManager.getOrientation(m_fMatrixR, m_fOrientation);
			//			m_dbAzimuth = m_fOrientation[0] * (180/Math.PI);
			double angle = Math.toDegrees(m_fOrientation[0]);
			m_dbAzimuth = (angle < 0)? 360 + angle : angle;
			//			Log.d("ANGLE", "" + angle + ", modified angle : " + m_dbAzimuth);
		}
	}    


	@Override
	public IBinder onBind(Intent intent) {
		if( IKAILOSFusedLocationService.class.getName().equals(intent.getAction())) {
			return m_IKAILOSFusedLocation;
		}

		return null;
	}

	public void prepareDetectors() {
		// Creating a step detector...
		m_StepDetector = new StepDetector();
		m_TurnDetector = new TurnDetector();

		m_StepDetector.init();
		m_TurnDetector.init();

		// Initialize variables for PDR...
		m_fAngle = new float[3];

		m_fAccelerometerValues = new float[3];
		m_fMagneticFieldValues = new float[3];

		m_fMatrixR = new float[9];
		m_fMatrixI = new float[9];   

		m_fOrientation = new float[3];

		m_fPrevTimestamp = 0;
		m_dbAzimuth = -999;

	}

	public void startSensors() {
		m_SensorManager.registerListener(this, m_sensorAccelerometer, SensorManager.SENSOR_DELAY_UI);
		m_SensorManager.registerListener(this, m_sensorGyroscope, SensorManager.SENSOR_DELAY_UI);        
		m_SensorManager.registerListener(this, m_sensorMagneticField, SensorManager.SENSOR_DELAY_UI);
	}

	public void stopSensors() {
		m_SensorManager.unregisterListener(this);
	}

	// "Kind of" callback...
	public void notifyStep(long lTimeStamp) {
		// TODO: Steps are reported through here...

		lTimeStamp = System.nanoTime();

		vibe.vibrate(100);

		/*Message msg = new Message();
		msg.what = GoogleMapFragment.HANDLER_TOAST;
		GoogleMapFragment.handler.sendMessage(msg);*/

		// m_target.notifyStep();
		m_target2.notifyStep(lTimeStamp);
		Log.d("FusedLocationTest", "Step! " + lTimeStamp + "/" + m_StepDetector.getStepCount());
	}

	public void notifyTurn(long lTimeStamp, double dbDegree) {
		// TODO: Turns are reported through here...

		lTimeStamp = System.nanoTime();

		m_target2.notifyTurn(lTimeStamp, dbDegree);
		Log.d("FusedLocationTest", "Turn! " + lTimeStamp + "/" + dbDegree);
	}

	public void notifyWiFi(long lTimeStamp, EstimatedLocation location) {
		// TODO: WiFi Locations are through here...


		/*		


		if(m_target.getVelocity() == null){

			m_smoother.addAzimuth(azimuth);
			LatLng startHeading = m_smoother.getHeadingVector(location);
			Log.d("ANGLE", "first azimuth : " + azimuth);
			m_target.setVelocity(startHeading);
		}*/

		/*		Log.d("FusedLocationTest", "Wi-Fi loc! " + lTimeStamp + "/" + location.getLatitude() + ", " + location.getLongitude());

		if(m_target == null)
			return;
		m_target.notifyWifi(location);

		double azimuth = getAzimuth();
		Log.d("ANGLE", "Azimuth! " + azimuth);

		if(m_target.getVelocity() == null){

			m_smoother.addAzimuth(azimuth);
			LatLng startHeading = m_smoother.getHeadingVector(location);
			Log.d("ANGLE", "first azimuth : " + azimuth);
			m_target.setVelocity(startHeading);
		}*/


		//		Log.d("FusedLocationTest", "heading lat : " + startHeading.latitude + ", heading lng : " + startHeading.longitude);
		//		temp = location;

		// Just for the test, 
		// We assume that this Wi-Fi location is the well-fused location...
		// i.e., We just return the Wi-Fi location back to m_LocationUpdateHandler@KAILOSService...
		/*		Message msg = new Message();		
		msg.what = Constants.MESSAGE_FUSED_LOCATION_RESPONSE;
		msg.obj = location;

		KAILOSService.m_LocationUpdateHandler.sendMessage(msg);*/
	}

	public void notifyEstimatedLocation(EstimatedLocation location) {
		// TODO: WiFi Locations are through here...

		Log.d("FusedLocationTest", "Wi-Fi loc! " + location.getTimeStamp() + "/" + location.getLatitude() + ", " + location.getLongitude());
		Log.d("FusedLocationTest", "Wi-Fi loc! " + System.nanoTime() + "/" + location.getLatitude() + ", " + location.getLongitude());

		long lTimeStamp = System.nanoTime();

		if(m_target2 == null)
			return;

		double azimuth = getAzimuth();
		if(m_target2.initialize(lTimeStamp, location, azimuth) == false)
			m_target2.notifyWifi(lTimeStamp, location);

		Log.d("ANGLE", "Azimuth! " + azimuth);		

		Log.d("FusedLocationTest", "Wi-Fi loc! " + location.getTimeStamp() + "/" + location.getLatitude() + ", " + location.getLongitude());
		Log.d("FusedLocationTest", "Azimuth! " + getAzimuth());

	}


	public double getAzimuth() {
		return m_dbAzimuth;
	}	
}
