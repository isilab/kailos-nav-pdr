// Deprecated!
/*package kr.ac.kaist.isilab.kailos.location;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import kr.ac.kaist.isilab.kailos.util.Constants;
import android.os.Message;

public class WPSThread extends Thread {
	private String m_strFingerprintJSON;
	
	public void setFingerprintJSON(String strNew) {
		m_strFingerprintJSON = strNew;
	}
	
	@Override	
	public void run() {		
		BufferedReader in = null;
		OutputStreamWriter os = null;
		String strLine = null;
		StringBuffer strBufJSONResponse = new StringBuffer();
		
		try {			
			URL url = new URL(Constants.KAILOS_WPS_SERVER);
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setDoOutput(true);
			connection.connect();
			
			os = new OutputStreamWriter(connection.getOutputStream());
			
			os.write(m_strFingerprintJSON);
			os.flush();
			
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			 
			while ((strLine = in.readLine()) != null) 
				strBufJSONResponse.append(strLine);
			
			EstimatedLocation estimatedLocation = null;
			String strResponse = strBufJSONResponse.toString();
			
			if( strResponse != null && !strResponse.equals("null") )
				estimatedLocation = new EstimatedLocation(strResponse);
			
			// Send the estimated location to KAILOSLocationManager..
			Message msg = new Message();
			msg.what = Constants.MESSAGE_WIFI_LOCATION_UPDATE;
			msg.obj = estimatedLocation;
			
			KAILOSLocationManager.m_Handler.sendMessage(msg);
						
		} catch( Exception e ) {
			//Toast.makeText(null, "Erorr: " + e.toString(), Toast.LENGTH_LONG).show();
		} finally {
			try {
				if( os != null )
					os.close();
				
				if( in != null )
					in.close();
			} catch( Exception e ) {
				// Nothing to do...
			}
		}
	}
}*/
