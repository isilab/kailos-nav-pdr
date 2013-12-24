package kr.ac.kaist.isilab.kailos.roadnetwork;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import kr.ac.kaist.isilab.kailos.util.Constants;
import android.os.Handler;
import android.os.Message;

public class RoadNetworkRequestThread extends Thread {
	private String m_strRequestJSON;
	private Handler m_responseHandler;
	
	public RoadNetworkRequestThread() {
		m_strRequestJSON = "";
		m_responseHandler = null; 		
	}
	
	public void setResponseHandler(Handler _handler) {
		m_responseHandler = _handler;
	}
	
	public void setRequestData(String strFloorID) {
		m_strRequestJSON = "{";
		
		m_strRequestJSON += "\"queryType\": \"GET_ROADS_BY_FLOORID\",";
		m_strRequestJSON += "\"floorId\":" + "\"" + strFloorID + "\" " ; 	
				
		m_strRequestJSON += "}";
	}
	
	public String getRequestData() {
		return m_strRequestJSON;
	}
	
	@Override
	public void run() {
		HttpURLConnection connection = null;;
		
		BufferedReader in = null;
		OutputStreamWriter os = null;
		
		String strLine = null;
		StringBuffer strBufJSONResponse = new StringBuffer();
		
		try {
			// If no response handler is specified, quit!
			if( m_responseHandler == null ) 
				return;			
			
			URL url = new URL(Constants.KAILOS_ROUTE_SERVER);
			
			connection = (HttpURLConnection)url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setDoOutput(true);
			connection.setConnectTimeout(Constants.SOCKET_TIME_OUT_MS);
			connection.setReadTimeout(Constants.SOCKET_TIME_OUT_MS);
			connection.connect();
			
			// Write out the request data...
			os = new OutputStreamWriter(connection.getOutputStream());

			os.write(m_strRequestJSON);
			os.flush();
			
			// Read response...			
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			 
			while( (strLine = in.readLine()) != null ) 
				strBufJSONResponse.append(strLine);
			
		} catch( Exception e ) {
			// Nothing to do... 
			// Exceptions, such as time-out, are ignored!
		} finally {
			try {
				if( in != null )
					in.close();
				
				if( os != null )
					os.close();	
				
				// Generate a response message..
				Message msg = new Message();
				msg.what = Constants.MESSAGE_ROAD_NETWORK_RESPONSE;
				msg.obj = strBufJSONResponse.toString();
				
				m_responseHandler.sendMessage(msg);	
			} catch( Exception e ) {
				// Do Nothing!
			}			
		}		
	}

}
