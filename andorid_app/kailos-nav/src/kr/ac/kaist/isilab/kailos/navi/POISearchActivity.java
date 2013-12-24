package kr.ac.kaist.isilab.kailos.navi;

import java.util.ArrayList;

import kr.ac.kaist.isilab.kailos.indoormap.IndoorMap;
import kr.ac.kaist.isilab.kailos.indoormap.IndoorMapRequestThread;
import kr.ac.kaist.isilab.kailos.location.EstimatedLocation;
import kr.ac.kaist.isilab.kailos.poi.POI;
import kr.ac.kaist.isilab.kailos.poi.POIItemAdapter;
import kr.ac.kaist.isilab.kailos.poi.POIQueryingThread;
import kr.ac.kaist.isilab.kailos.route.RouteManager;
import kr.ac.kaist.isilab.kailos.route.RouteRequestThread;
import kr.ac.kaist.isilab.kailos.util.Constants;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class POISearchActivity extends Activity {
	private static EditText m_editTextFrom;
	private static EditText m_editTextTo;
	private static ListView m_listViewQueryResult;
	private static ProgressDialog m_queryingDialog;
	private static ProgressDialog m_routingDialog;
	private static Context m_context;
	
	private TextView m_textViewSplitter;
	private LinearLayout m_llResizing;
	private LinearLayout m_llItemList;
	
	private static GoogleMap m_Map;
	private static GroundOverlay m_overlayIndoormap;
	private Marker m_PlaceMarker;
	private POI m_CurrentlySelected;
	
	private POI m_POIDeparture;
	private POI m_POIDestination;
	
	private boolean m_bIsDepartureSetting;
	private boolean m_bIsDestinationSetting;
		
	private POIItemClickListener m_ItemClickListener;
	private static POIItemAdapter m_ListItemAdapter;
	
	public static Handler m_responseHanlder = new Handler() {
		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			switch( msg.what ) {
				case Constants.MESSAGE_POI_QUERY_THREAD_RESPONSE:
					// Build up list items...
					ArrayList<POI> lstPOI = (ArrayList<POI>)msg.obj;
					
					// Dismiss the progress dialog...
					m_queryingDialog.dismiss();
					
					if( lstPOI == null ) {						
						Toast.makeText(POISearchActivity.m_context, "Sorry, an error occured while searching!", Toast.LENGTH_SHORT).show();
						return;
					}
					
					if( lstPOI.size() == 0 ) {
						Toast.makeText(POISearchActivity.m_context, "Sorry, no search result!", Toast.LENGTH_SHORT).show();
						return;						
					}
					
					m_ListItemAdapter = new POIItemAdapter(POISearchActivity.m_context, R.layout.query_result_list_item, lstPOI);
					
					// Put list items...
					m_listViewQueryResult.setAdapter(m_ListItemAdapter);
					m_listViewQueryResult.setChoiceMode(ListView.CHOICE_MODE_SINGLE);	
					
					break;					
				case Constants.MESSAGE_INDOOR_MAP_THREAD_RESPONSE:
					IndoorMap map = (IndoorMap)msg.obj;
					
					if( map == null || m_Map == null )
						return;
					
					LatLngBounds bounds = new LatLngBounds(map.getSWLatLng(), map.getNELatLng());				
					
					if( m_overlayIndoormap != null )
						m_overlayIndoormap.remove();
					
					m_overlayIndoormap = m_Map.addGroundOverlay(new GroundOverlayOptions().image(map.getBitmapDescriptor())
																									.bearing((float)map.getRotationalDegree())
																									.positionFromBounds(bounds)
																									.transparency(0.0f));
					break;
				case Constants.MESSAGE_DISMISS_ROUTING_CALCULATION_PROGRESS_DIALOG:
					if( m_routingDialog != null ) {
						m_routingDialog.dismiss();
						
						((POISearchActivity)m_context).finish();
					}
					
					break;
				default:
					break;
			}
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		m_context = this;
		
		m_overlayIndoormap = null;
		
		// Adding a back button at the action bar...
		getActionBar().setHomeButtonEnabled(true);
		
		// Load layout...
		setContentView(R.layout.poisearch_activity);
		
		// Getting references...		
		m_editTextFrom = (EditText)findViewById(R.id.editText_from);
		m_editTextTo = (EditText)findViewById(R.id.editText_to);
		m_listViewQueryResult = (ListView)findViewById(R.id.listview_serach_results);
		
		m_Map = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
		m_textViewSplitter = (TextView)findViewById(R.id.textView_resize);
		m_llResizing = (LinearLayout)findViewById(R.id.ll_resizeing);
		m_llItemList = (LinearLayout)findViewById(R.id.ll_place_list);
		
		// Register listeners...
		m_textViewSplitter.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int nAction = event.getAction();
				
				if( nAction == MotionEvent.ACTION_MOVE ) {					
					LayoutParams param = m_llItemList.getLayoutParams();
					
					int nHeight = m_llItemList.getHeight();					
					nHeight += (int)event.getY();
					
					param.height = nHeight;
					m_llItemList.setLayoutParams(param);	
					
				} else if( nAction == MotionEvent.ACTION_UP ) {										
					m_llResizing.setVisibility(View.INVISIBLE);
					
				} else if( nAction == MotionEvent.ACTION_DOWN ) {					
					m_llResizing.setVisibility(View.VISIBLE);
				}
				
				return true;
			}		
		});
		
		// Map Setting...
		m_Map.moveCamera(CameraUpdateFactory.newLatLngZoom(Constants.KAIST, 6));
		m_Map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
			@Override
			public void onInfoWindowClick(Marker marker) {
				if( m_bIsDepartureSetting ) {
					m_POIDeparture = m_CurrentlySelected;
					m_editTextFrom.setText(m_CurrentlySelected.getPlaceNameEng());
				} else {
					m_POIDestination = m_CurrentlySelected;
					m_editTextTo.setText(m_CurrentlySelected.getPlaceNameEng());
				}
				
				if( (m_POIDeparture == null && m_POIDestination != null) || (m_POIDeparture != null && m_POIDestination != null) )
					ShowStartNavigationYesNoAlert();									
			}			
		});
		
		// Handling Item click...
		m_listViewQueryResult.setOnItemClickListener(m_ItemClickListener);
		
		// Handling key event..		
		m_editTextFrom.setOnEditorActionListener(m_EditorActionListener);
		m_editTextTo.setOnEditorActionListener(m_EditorActionListener);
		
		m_editTextFrom.setOnFocusChangeListener(m_FocusChangeListener);
		m_editTextTo.setOnFocusChangeListener(m_FocusChangeListener);		
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch( item.getItemId() ) {
	        case android.R.id.home:
	        	onBackPressed();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
        }
    }
    
    public POISearchActivity() {
    	m_queryingDialog = null;
    	m_listViewQueryResult = null;
    	m_context = null;
    	m_ListItemAdapter = null;
    	m_textViewSplitter = null;
    	
    	m_Map = null;    	
    	m_PlaceMarker = null;    	
    	
    	m_CurrentlySelected = null;
    	
    	m_POIDeparture = null;
    	m_POIDestination = null;
    	
    	m_bIsDepartureSetting = false;
    	m_bIsDestinationSetting = false;
    	
    	m_ItemClickListener = new POIItemClickListener();
    	
    	m_POIDeparture = null;
    	m_POIDestination = null;
    }
    
	private OnFocusChangeListener m_FocusChangeListener = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			POI poiTarget = null;
			EditText editTextCurrent = null;
			
			if( v.getId() == R.id.editText_from ) {
				poiTarget = m_POIDeparture;
				editTextCurrent = m_editTextFrom;
			}				
			else {
				poiTarget = m_POIDestination;
				editTextCurrent = m_editTextTo;
			}
			
			// remove marker...
			if( m_PlaceMarker != null )
				m_PlaceMarker.remove();
				
			m_CurrentlySelected = null;
			
			if( hasFocus == false ) {				
				// which means that the user did not chose any searched poi yet..
				if( poiTarget == null || editTextCurrent.getText().toString().trim().compareTo("") == 0 ) {
					// Initialize the field..
					poiTarget = null;
					editTextCurrent.setText("");
				} else {
					editTextCurrent.setText(poiTarget.getPlaceNameEng());
				}
				
				m_listViewQueryResult.setAdapter(null);
			}
		}		
	};
	
    private class POIItemClickListener implements OnItemClickListener {
        public void onItemClick(AdapterView<?> parentView, View clickedView, int position, long id) {
        	POI poiPrevious = m_CurrentlySelected;
        	
        	m_CurrentlySelected = m_ListItemAdapter.getItem(position);
        	        	        	
        	CameraPosition cameraPosition = m_Map.getCameraPosition();
        	CameraUpdate cameraUpdate = null;
        	
        	// Move camera to the selected place...
        	if( cameraPosition.zoom <= 18 ) {
        		cameraUpdate = CameraUpdateFactory.newLatLngZoom(m_CurrentlySelected.getLatLng(), 20);
        	} else {
        		cameraUpdate = CameraUpdateFactory.newLatLng(m_CurrentlySelected.getLatLng());
        	}
        	        	
        	m_Map.animateCamera(cameraUpdate);
        	
        	// Request indoor map...   
        	if( (poiPrevious == null) || (poiPrevious.getFloorID().compareTo(m_CurrentlySelected.getFloorID()) != 0) ) {
            	IndoorMapRequestThread thread = new IndoorMapRequestThread();
            	
            	thread.setResponseHandler(m_responseHanlder);
            	thread.setRequestDataWithFloorID(m_CurrentlySelected.getFloorID());
            	
            	thread.start();
        	}
        	
        	// Set marker...
        	if( m_PlaceMarker != null )
        		m_PlaceMarker.remove();
        	
        	MarkerOptions markerOptions = new MarkerOptions();            	
        	
        	// Prepare marker image...
        	Bitmap markerImg = BitmapFactory.decodeResource(MainActivity.m_Resources, R.drawable.ic_poi_location);
			BitmapDescriptor descirptor  = BitmapDescriptorFactory.fromBitmap(markerImg);
			markerImg.recycle();
			
			String strEditting = null;
			
			if( m_bIsDepartureSetting ) 
				strEditting = " departure";
			else if( m_bIsDestinationSetting )
				strEditting = " destination";
        	
        	markerOptions.position(m_CurrentlySelected.getLatLng());
        	markerOptions.icon(descirptor);
        	markerOptions.title("Tap here to set " + strEditting);
        	
        	m_PlaceMarker = m_Map.addMarker(markerOptions);
        	m_PlaceMarker.showInfoWindow();
        }
    }
    
	private OnEditorActionListener m_EditorActionListener = new OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if( actionId == EditorInfo.IME_ACTION_SEARCH ) {				
				String strKeyword = v.getText().toString().trim();
				
				if( strKeyword.compareTo("") != 0 ) {					
					// Hide keyboard..
					InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
					
					// Remove maker...
					if( m_PlaceMarker != null )
						m_PlaceMarker.remove();
					
					m_CurrentlySelected = null;
					
					
					// Store which edittext is currently setting...
					setSettingStatus(v);

					// Send poi search request....					
					POIQueryingThread thread = new POIQueryingThread();
					
					thread.setRequestDataWithKeyword(strKeyword);
					thread.setResponseHandler(m_responseHanlder);
											
					thread.start();
					
					// Showing up waiting dialog...
					m_queryingDialog = ProgressDialog.show(POISearchActivity.this, "Please wait a few seconds!", "Searching places....", true);
				} else {
					Toast.makeText(MainActivity.m_MainActivityContext, "Please enter a keyword!", Toast.LENGTH_SHORT).show();
				}
				
				return true;
			}
			
			return false;
		}
		
	};
	
	private void setSettingStatus(TextView v) {
		m_bIsDepartureSetting = false;
		m_bIsDestinationSetting = false;

		if( v.getId() == R.id.editText_from ) 
			m_bIsDepartureSetting = true;
		else if( v.getId() == R.id.editText_to ) 
			m_bIsDestinationSetting = true;		
	}

	public void ShowStartNavigationYesNoAlert() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		String strMsg = "Do you want to start navigation?\n";		
		
		if( m_POIDeparture == null )
			strMsg += "From: Current location\n";
		else
			strMsg += "From: " + m_POIDeparture.getPlaceNameEng() +"\n";
		
		strMsg += "To: " + m_POIDestination.getPlaceNameEng();
		
		alert.setTitle("Find Route?");
		alert.setMessage(strMsg);
		alert.setCancelable(false);		
		alert.setPositiveButton("Yes", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// If no departure point is specified...
				if( m_POIDeparture == null ) {
					try {
						String strLoc = KAILOSService.m_KAILOSServiceRPC.getCurrentLocation();
						
						if( strLoc != null ) {
							EstimatedLocation currentLocation = EstimatedLocation.createFromString(strLoc);
							
							m_POIDeparture = new POI();
							
							m_POIDeparture.setLatitude(currentLocation.getLatitude());
							m_POIDeparture.setLongitude(currentLocation.getLongitude());
							m_POIDeparture.setFloorID(currentLocation.getFloorID());							
						}
					} catch (RemoteException e) {
						Toast.makeText(POISearchActivity.this, "FATAL ERORR - Cannot call remote procedure!", Toast.LENGTH_SHORT).show();
						return;
					}
				}
				
				// If m_POIDeparture is still null, that means that no current location is available yet..
				if( m_POIDeparture == null ) {
					AlertDialog.Builder alert = new AlertDialog.Builder(POISearchActivity.this);
					alert.setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
					    @Override
					    public void onClick(DialogInterface dialog, int which) {
					    	dialog.dismiss();
					    }
					});
					
					alert.setMessage("Cannot find a routing path utill the current location is estimated.\nPlease wait a few more seconds and try again!");
					alert.show();
					
					return;
				}
				
				
				// show progress dialog...				
				m_routingDialog = ProgressDialog.show(POISearchActivity.this, "Please wait a few seconds!", "Searching for the optimal route....", true);
				
				RouteRequestThread thread = new RouteRequestThread();
				
				// Store currently selected departure / destination...
				RouteManager.setDeparturePOI(m_POIDeparture);
				RouteManager.setDestinationPOI(m_POIDestination);
								
				thread.setResponseHandler(RouteManager.m_handlerResponse);
				thread.setRequestData(m_POIDeparture.getFloorID(), m_POIDeparture.getLatLng(), m_POIDestination.getFloorID(), m_POIDestination.getLatLng());
				
				thread.start();
			}					
		});

		alert.setNegativeButton("No", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
			}					
		});
		
		alert.show();					
	}
}
