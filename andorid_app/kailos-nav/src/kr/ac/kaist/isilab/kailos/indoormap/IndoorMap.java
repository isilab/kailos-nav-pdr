package kr.ac.kaist.isilab.kailos.indoormap;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import kr.ac.kaist.isilab.kailos.util.Constants;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

public class IndoorMap {
	// Member Variables..
	private String m_strFloorID;

	private String m_strBuildingID;
	private int m_nOldID;
	private int m_nFloorLevel;
	private String m_strFloorName;
	private String m_strDescription;
	
	private String m_strFloorImg;
	private LatLng m_latlngSW;
	private LatLng m_latlngNE;
	private double m_dbRotationalDegree;
	
	private long m_lRequestedTime;
	private BitmapDescriptor m_bitmapDescriptor;

	
	// Constructors...
	public IndoorMap() {
		m_strFloorID = null;
		m_strBuildingID = null;
		m_nOldID = Constants.FLOOR_NOT_DEFINED;	
		m_strFloorName = null;
		m_strDescription = null;
		
		m_strFloorImg = null;		
		m_latlngSW = null;
		m_latlngNE = null;
		m_dbRotationalDegree = 0.0f;
		m_nFloorLevel = Constants.FLOOR_NOT_DEFINED;
		m_bitmapDescriptor = null;
		m_lRequestedTime = 0;
	}
	
	// Member Function...
	// ----------------------------------------------------------------------------------------------
	// - Description : This function is to called to retrieve the specified indoor map image and
	//                 create a bitmap descriptor of it..
	public void createBitmapDescriptor() {		
		Bitmap bitmap = null;
		
		if( m_strFloorImg == null )
			return;			
		
		String strImageURL = Constants.MAP_IMAGE_ROOT_URL + m_strFloorImg;
        BitmapFactory.Options options = new BitmapFactory.Options();
        
        options.inSampleSize = 1;
		
		try {
			URL urlImage = new URL(strImageURL);
            URLConnection conn = urlImage.openConnection();
            conn.setReadTimeout(Constants.SOCKET_TIME_OUT_MS);
            conn.setConnectTimeout(Constants.SOCKET_TIME_OUT_MS);
            conn.connect();
            
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);

            bitmap = BitmapFactory.decodeStream(bis, null, options);
            
            bis.close();
            is.close();
            
            m_bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);            
            bitmap.recycle();
            
		} catch( Exception e ) {
			if( bitmap != null )
				bitmap.recycle();			
			
			m_bitmapDescriptor = null;
		}
	}
	
	public String getFloorID() {
		return m_strFloorID;
	}

	public void setFloorID(String m_strFloorID) {
		this.m_strFloorID = m_strFloorID;
	}

	public String getBuildingID() {
		return m_strBuildingID;
	}

	public void setBuildingID(String m_strBuildingID) {
		this.m_strBuildingID = m_strBuildingID;
	}

	public int getOldID() {
		return m_nOldID;
	}

	public void setOldID(int nOldID) {
		this.m_nOldID = nOldID;
	}

	public String getFloorName() {
		return m_strFloorName;
	}

	public void setFloorName(String m_strFloorName) {
		this.m_strFloorName = m_strFloorName;
	}

	public String getDescription() {
		return m_strDescription;
	}

	public void setDescription(String m_strDescription) {
		this.m_strDescription = m_strDescription;
	}

	public String getFloorImg() {
		return m_strFloorImg;
	}

	public void setFloorImg(String m_strFloorImg) {
		this.m_strFloorImg = m_strFloorImg;
	}

	public LatLng getSWLatLng() {
		return m_latlngSW;
	}
	
	public void setSWLatLng(LatLng latlng) {
		m_latlngSW = latlng;
	}

	public LatLng getNELatLng() {
		return m_latlngNE;
	}

	public void setNELatLng(LatLng latlng) {
		m_latlngNE = latlng;
	}
	
	public double getRotationalDegree() {
		return m_dbRotationalDegree;
	}

	public void setRotationalDegree(double m_dbRotationalDegree) {
		this.m_dbRotationalDegree = m_dbRotationalDegree;
	}
	
	public void setFloorLevel(int nLevel) {
		m_nFloorLevel = nLevel;
	}
	
	public int getFloorLevel() {
		return m_nFloorLevel;
	}

	public BitmapDescriptor getBitmapDescriptor() {
		return m_bitmapDescriptor;
	}

	public void setBitmapDescriptor(BitmapDescriptor m_bitmapDescriptor) {
		this.m_bitmapDescriptor = m_bitmapDescriptor;
	}
	
	public long getRequestedTime() {
		return m_lRequestedTime;
	}
	
	public void setRequestedTime(long lTime) {
		m_lRequestedTime = lTime;
	}
}
