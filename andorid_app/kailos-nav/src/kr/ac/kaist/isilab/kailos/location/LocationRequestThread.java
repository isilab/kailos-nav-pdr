package kr.ac.kaist.isilab.kailos.location;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import android.location.Location;
import android.net.wifi.ScanResult;
import android.os.Handler;
import android.os.Message;

import kr.ac.kaist.isilab.kailos.location.EstimatedLocation;
import kr.ac.kaist.isilab.kailos.util.Constants;

public class LocationRequestThread extends Thread {
	public static boolean m_bIsLocationRequestThreadWorking = false;
	
	private String m_strReuqestData;
	private Handler m_resultHandler;
	
	public void setRequestData(String newData) {
		m_strReuqestData = newData;
	}
	
	public void setResultHandler(Handler _newHandler) {
		m_resultHandler = _newHandler;
	}
	
	public LocationRequestThread() {
		m_strReuqestData = null;
		m_resultHandler = null;
	}	
	
	public void setRequestData(List<ScanResult> lstScanendAP, List<Location> lstGPSFix) {
		StringBuffer strBuf = new StringBuffer();
		
		if( lstScanendAP == null || lstGPSFix == null )
			return;
		
		strBuf.append("{\"wifi\": [ ");
		
		for(int i=0; i<lstScanendAP.size(); i++) {
			ScanResult scanResult = lstScanendAP.get(i);
			
			strBuf.append("{");
			strBuf.append("\"bssid\": " + "\"" + scanResult.BSSID + "\""  + ",");
			strBuf.append("\"rss\": " + "\"" + scanResult.level + "\"" + ",");
			strBuf.append("\"ssid\": " + "\"" +scanResult.SSID + "\"");					
			strBuf.append("}");
			
			if( i != lstScanendAP.size() - 1 )
				strBuf.append(",");
		}
		
		strBuf.append("], \"gps\":[");
		
		for(int i=0; i<lstGPSFix.size(); i++) {
			Location loc = lstGPSFix.get(i);
			
			strBuf.append("{");
			
			strBuf.append("\"lng\":" + "\"" + loc.getLongitude() + "\",");
			strBuf.append("\"lat\":" + "\"" + loc.getLatitude() + "\",");
			strBuf.append("\"acc\":" + "\"" + loc.getAccuracy() + "\",");
			strBuf.append("\"alt\":" + "\"" + loc.getAltitude() + "\",");
			strBuf.append("\"bear\":" + "\"" + loc.getBearing() + "\",");
			strBuf.append("\"elap\":" + "\"0.0\",");
			strBuf.append("\"speed\":" + "\"" + loc.getSpeed() + "\",");
			strBuf.append("\"time\":" + "\"" + loc.getTime() + "\"");
								
			strBuf.append("}");
			
			if( i != lstGPSFix.size()-1 ) 
				strBuf.append(",");
		}
		
		strBuf.append("]}");				
						
		// Finalize...
		m_strReuqestData = strBuf.toString();
		strBuf = null;
		
	}
	
	@Override 
	public void start() {
		m_bIsLocationRequestThreadWorking = true;
		
		super.start();
	}
	
	@Override
	public void run() {
		BufferedReader in = null;
		
		OutputStreamWriter os = null;
		String strLine = null;
		StringBuffer strBufJSONResponse = new StringBuffer();
		EstimatedLocation estimatedLocation = null;
		
		// If no handler to pass the result, quit!		
		if( m_resultHandler == null ) {
			m_bIsLocationRequestThreadWorking = false;
			return;
		}
		
		try {			
			URL url = new URL(Constants.KAILOS_WPS_SERVER);
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setUseCaches(false);
			connection.setDoOutput(true);
			
			connection.setConnectTimeout(Constants.SOCKET_TIME_OUT_MS);
			connection.setReadTimeout(Constants.SOCKET_TIME_OUT_MS);
			
			connection.connect();
			
			os = new OutputStreamWriter(connection.getOutputStream());
			
			os.write(m_strReuqestData);
			os.flush();
			
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			 
			while ((strLine = in.readLine()) != null) 
				strBufJSONResponse.append(strLine);
			
			String strResponse = strBufJSONResponse.toString();
			
			if( strResponse != null && !strResponse.equals("null") )
				estimatedLocation = new EstimatedLocation(strResponse);			
			
		} catch( Exception e ) {
			//Toast.makeText(MainActivity.m_MainActivityContext, "Exception: " + e.toString(), Toast.LENGTH_SHORT).show();
		} finally {
			try {
				if( os != null )
					os.close();
				
				if( in != null )
					in.close();
				
				// Generate response message...
				Message msg = new Message();
				msg.what = Constants.MESSAGE_LOCATION_THREAD_RESPONSE;
				msg.obj = estimatedLocation;
				
				m_resultHandler.sendMessage(msg);
		
			} catch( Exception e ) {
				// Nothing to do...
			}
			
			m_bIsLocationRequestThreadWorking = false;
		}		
	}
}
