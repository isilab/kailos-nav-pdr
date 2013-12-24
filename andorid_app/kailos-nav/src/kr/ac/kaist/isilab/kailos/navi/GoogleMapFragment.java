package kr.ac.kaist.isilab.kailos.navi;

import java.util.ArrayList;
import java.util.LinkedList;

import kr.ac.kaist.isilab.kailos.googlemap.IndoorMapOverlay;
import kr.ac.kaist.isilab.kailos.indoormap.IndoorMap;
import kr.ac.kaist.isilab.kailos.location.EstimatedLocation;
import kr.ac.kaist.isilab.kailos.location.Target;
import kr.ac.kaist.isilab.kailos.location.TargetInfo;
import kr.ac.kaist.isilab.kailos.route.RouteVertex;
import kr.ac.kaist.isilab.kailos.util.Constants;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class GoogleMapFragment extends Fragment {
   private KAILOSGoogleMapFragment m_MapFragment;

   private ImageButton m_imgBtnTrackLocation;
   private ImageButton m_imgBtnGotoCurrentLocation;

   private static ImageButton m_imgBtnStopNavigation;
   private static ImageButton m_imgBtnStartNavigation;

   private static TextView m_textViewLocationIndicator;   
   public static GoogleMap m_map;

   private static int m_nMode;
   private static Target target;

   // Overlays...
   private static Polyline m_polylineRoute;
   private static Marker m_markerWPS;
   private static Marker m_markerKAILOS;
   private static Marker m_markerDeparture;
   private static Marker m_markerDestination;
   private static Marker m_markerTemp;

   private static Marker m_markerTemp1;
   private static Marker m_markerTemp2;

   private static Circle m_circleEstimatedError;
   private static IndoorMapOverlay m_overlayIndoorMap;
   
   private static Circle m_circleLocation;
   private static Polyline m_polyLineHeading;

   private static LinkedList<IndoorMapOverlay> m_lstFloorMapsForNavigation;
   private static ArrayList<Polyline> m_lstCurrentlyShowingTransparentRoute;
   private static ArrayList<Polyline> m_lstCurrentlyShowingNonTransparentRoute;

   // KSH made
   public static final int HANDLER_TOAST = 1;
   public static final int HANDLER_TOAST_CANCEL = 2; 
   public static Handler handler = new Handler(){

      @Override
      public void handleMessage(Message msg) {
         switch (msg.what) {
         case HANDLER_TOAST:
            break;
         default:
            break;
         }
      }

   };

   private OnClickListener m_listenerTrackLocation = new OnClickListener() {
      @Override
      public void onClick(View v) {
         try {         
            boolean bTracking = KAILOSService.m_KAILOSServiceRPC.isTracking();

            if( bTracking ) {
               KAILOSService.m_KAILOSServiceRPC.stopLocationztion();
               Toast.makeText(MainActivity.m_MainActivityContext, "Stop tracking!", Toast.LENGTH_SHORT).show();
            } else {
               KAILOSService.m_KAILOSServiceRPC.startLocalization();
               Toast.makeText(MainActivity.m_MainActivityContext, "Tracking the current location!", Toast.LENGTH_SHORT).show();
            }
         } catch (RemoteException e) {
            // ...   
         }   
      }
   };

   private OnClickListener m_listenerGoToCurrentLocation = new OnClickListener() {
      @Override
      public void onClick(View v) {

         Marker marker = GoogleMapFragment.m_markerKAILOS;

         if( marker != null )            
            GoogleMapFragment.moveCamera(marker.getPosition(), 20, 20);
      }
   };

   private OnClickListener m_listenerStartNavigation = new OnClickListener() {
      @Override
      public void onClick(View v) {
         AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.m_MainActivityContext);
         String strMsg = "Do you really want to start navigation?\n";      

         alert.setTitle("Start navigation?");
         alert.setMessage(strMsg);
         alert.setCancelable(false);      
         alert.setPositiveButton("Yes", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               // Remove indoor maps for navigations...
               // Well, they will just automatically show up as the current location is updated...
               GoogleMapFragment.removeAllIndoorMapOverlayForNavigation();
               GoogleMapFragment.removeAllNormalRoute();

               // Start Navigation Mode!
               GoogleMapFragment.setCurrentMode(Constants.NAVIGATION_MODE);               
               m_imgBtnStartNavigation.setVisibility(View.INVISIBLE);
            }            
         });

         alert.setNegativeButton("No", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {               
            }
         });

         alert.show();                     
      }      
   };


   private OnClickListener m_listenerStopNavigation = new OnClickListener() {
      @Override
      public void onClick(View v) {
         AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.m_MainActivityContext);
         String strMsg = "Do you really want to cancel the route?\n";      

         alert.setTitle("Stop navigation?");
         alert.setMessage(strMsg);
         alert.setCancelable(false);      
         alert.setPositiveButton("Yes", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               // Mode..
               GoogleMapFragment.setCurrentMode(Constants.NORMAL_MODE);

               // Hide option buttons...
               m_imgBtnStartNavigation.setVisibility(View.INVISIBLE);               
               m_imgBtnStopNavigation.setVisibility(View.INVISIBLE);

               // Hide the departure and destination markers...
               hideDepartureMarker();
               hideDestinationMarker();

               // Remove all indoor maps for navigation...
               removeAllIndoorMapOverlayForNavigation();

               // remove all routes which are currently showing on the map...
               removeAllTransparentRoute();
               removeAllNormalRoute();
            }            
         });

         alert.setNegativeButton("No", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {               
            }
         });

         alert.show();                     
      }      
   };

   public static void setIndicator(String strMsg) {
      if( m_textViewLocationIndicator != null ) {
         if( strMsg == null || strMsg.compareTo("") == 0 ) {
            m_textViewLocationIndicator.setVisibility(View.INVISIBLE);
            m_textViewLocationIndicator.requestLayout();
         } else {
            m_textViewLocationIndicator.setText(strMsg);
            m_textViewLocationIndicator.setVisibility(View.VISIBLE);
            m_textViewLocationIndicator.requestLayout();
         }
      }
   }

   public static void setGoogleWPSMarker(LatLng loc) {
      if( loc == null )
         return;

      if( m_markerWPS == null ) {
         Bitmap markerImg = BitmapFactory.decodeResource(MainActivity.m_Resources, R.drawable.ic_goole_wps_marker);
         BitmapDescriptor descirptor  = BitmapDescriptorFactory.fromBitmap(markerImg);


         MarkerOptions  option = new MarkerOptions().position(loc)
               .icon(descirptor);         

         m_markerWPS = GoogleMapFragment.m_map.addMarker(option);         
      } else {
         m_markerWPS.setPosition(loc);
      }       
   }

   public static void removeGoogleWPSMarker() {
      if( m_markerWPS != null ) {
         m_markerWPS.remove();
         m_markerWPS = null;
      }
   }

   /*public static void updateKAILOSMarkerLoaction(EstimatedLocation location){

   }*/

   public static void setKAILOSMarker(EstimatedLocation location) {
      if( m_markerKAILOS != null ) { // Just move the marker...
         m_markerKAILOS.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));         

         // Estimated error representation...
         /*         m_circleEstimatedError.remove();

         CircleOptions circleOptions = new CircleOptions().center(new LatLng(location.getLatitude(), location.getLongitude()))
               .radius((int)location.getErrorDistance())
               .fillColor(Color.parseColor("#641E90FF"))
               .strokeColor(Color.parseColor("#1E90FF"))
               .strokeWidth(1)
               .zIndex(10);

         m_circleEstimatedError = GoogleMapFragment.m_map.addCircle(circleOptions);*/         

      } else { // Create a new marker..
         Bitmap markerImg = BitmapFactory.decodeResource(MainActivity.m_Resources, R.drawable.ic_kailos_marker);
         BitmapDescriptor descirptor  = BitmapDescriptorFactory.fromBitmap(markerImg);


         MarkerOptions  option = new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude()))
               .icon(descirptor);         

         m_markerKAILOS = GoogleMapFragment.m_map.addMarker(option);   

         // Show estimated error distance...
         /*      CircleOptions circleOptions = new CircleOptions().center(new LatLng(location.getLatitude(), location.getLongitude()))
               .radius((int)location.getErrorDistance())
               .fillColor(Color.parseColor("#641E90FF"))
               .strokeColor(Color.parseColor("#1E90FF"))
               .strokeWidth(1)
               .zIndex(10);

         m_circleEstimatedError = GoogleMapFragment.m_map.addCircle(circleOptions);*/
      }      
   }

   public static void setKAILOSMarker(LatLng latlng) {
      if( m_markerKAILOS != null ) { // Just move the marker...
         m_markerKAILOS.setPosition(latlng);         

         // Estimated error representation...
         /*         m_circleEstimatedError.remove();

               CircleOptions circleOptions = new CircleOptions().center(new LatLng(location.getLatitude(), location.getLongitude()))
                     .radius((int)location.getErrorDistance())
                     .fillColor(Color.parseColor("#641E90FF"))
                     .strokeColor(Color.parseColor("#1E90FF"))
                     .strokeWidth(1)
                     .zIndex(10);

               m_circleEstimatedError = GoogleMapFragment.m_map.addCircle(circleOptions);*/         

      } else { // Create a new marker..
         Bitmap markerImg = BitmapFactory.decodeResource(MainActivity.m_Resources, R.drawable.ic_kailos_marker);
         BitmapDescriptor descirptor  = BitmapDescriptorFactory.fromBitmap(markerImg);


         MarkerOptions  option = new MarkerOptions().position(latlng)
               .icon(descirptor);         

         m_markerKAILOS = GoogleMapFragment.m_map.addMarker(option);   

         // Show estimated error distance...
         /*      CircleOptions circleOptions = new CircleOptions().center(new LatLng(location.getLatitude(), location.getLongitude()))
                     .radius((int)location.getErrorDistance())
                     .fillColor(Color.parseColor("#641E90FF"))
                     .strokeColor(Color.parseColor("#1E90FF"))
                     .strokeWidth(1)
                     .zIndex(10);

               m_circleEstimatedError = GoogleMapFragment.m_map.addCircle(circleOptions);*/
      }      
   }
   
   public static void setCurrentFusedLocation(TargetInfo info) {
      if( info == null )
         return;
      
      if( m_circleLocation != null )
         m_circleLocation.remove();
      
      if( m_polyLineHeading != null )
         m_polyLineHeading.remove();
      
      CircleOptions circleOptions = new CircleOptions().center(new LatLng(info.getCurLat(), info.getCurLng()))
                                             .radius(1.5f)
                                             .strokeColor(Color.parseColor("#1E90FF"))
                                             .strokeWidth(1)
                                             .zIndex(15);
      
      m_circleLocation = m_map.addCircle(circleOptions);      

      PolylineOptions polyOptions = new PolylineOptions();
      polyOptions.color(Constants.RED_COLOR);
      polyOptions.width(5.0f);
      polyOptions.zIndex(15);
      
      polyOptions.add(new LatLng(info.getCurLat(), info.getCurLng()));
      polyOptions.add(new LatLng(info.getHeadLat(), info.getHeadLng()));
      
      m_polyLineHeading = m_map.addPolyline(polyOptions);

         
   }

   public static void removeKAILOSMarker() {
      if( m_markerKAILOS != null ) {
         m_markerKAILOS.remove();
         m_markerKAILOS = null;
      }

      if( m_circleEstimatedError != null ) {
         m_circleEstimatedError.remove();
         m_circleEstimatedError = null;
      }

   }

   public static void setIndoorMapOverlay(IndoorMap map) {      
      if( m_overlayIndoorMap == null )
         m_overlayIndoorMap = new IndoorMapOverlay();

      m_overlayIndoorMap.setIndoorMapOverlay(map);   
   }

   public static void removeIndoorMapOverlay() {
      if( m_overlayIndoorMap != null )
         m_overlayIndoorMap.removeIndoorMapOverlay();
   }

   public static String getIndoorMapOverlayFloorID() {
      if( m_overlayIndoorMap != null )
         return m_overlayIndoorMap.getOverlayMapFloorID();

      return null;
   }

   public static void moveCamera(EstimatedLocation location, int nMinZoom, int nTargetZoom) {
      if( m_map != null ) {
         CameraUpdate update = null;

         if( m_map.getCameraPosition().zoom < nMinZoom )
            update =  CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), nTargetZoom);
         else
            update =  CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude()));

         m_map.animateCamera(update);                     
      }
   }

   public static void moveCamera(LatLng location, int nMinZoom, int nTargetZoom) {
      if( m_map != null ) {
         CameraUpdate update = null;

         if( m_map.getCameraPosition().zoom < nMinZoom )
            update =  CameraUpdateFactory.newLatLngZoom(location, nTargetZoom);
         else
            update =  CameraUpdateFactory.newLatLng(location);

         m_map.animateCamera(update);                     
      }
   }

   public static void addIndoorMapOverlayForNavigation(IndoorMap map) {
      if( map == null )
         return;

      IndoorMapOverlay overlay = new IndoorMapOverlay();

      overlay.setIndoorMapOverlay(map);

      m_lstFloorMapsForNavigation.add(overlay);
   }

   public static void removeAllIndoorMapOverlayForNavigation() {
      for(int i=0; i<m_lstFloorMapsForNavigation.size(); i++) {
         IndoorMapOverlay overlay = m_lstFloorMapsForNavigation.get(i);

         overlay.removeIndoorMapOverlay();
      }

      m_lstFloorMapsForNavigation = new LinkedList<IndoorMapOverlay>();      
   }

   public static void addTransparentRoute(PolylineOptions options) {
      if( m_map == null || options == null ) 
         return;

      m_lstCurrentlyShowingTransparentRoute.add(m_map.addPolyline(options));
   }

   public static void removeAllTransparentRoute() {
      if( m_lstCurrentlyShowingTransparentRoute == null ) 
         return;

      for(int i=0; i<m_lstCurrentlyShowingTransparentRoute.size(); i++) {
         Polyline line = m_lstCurrentlyShowingTransparentRoute.get(i);

         if( line != null )
            line.remove();
      }
   }

   public static void addNormalRoute(PolylineOptions options) {
      if( m_map == null || options == null )
         return;

      m_lstCurrentlyShowingNonTransparentRoute.add(m_map.addPolyline(options));
   }

   public static void addNormalRoute(ArrayList<RouteVertex> lstVertex) {
      PolylineOptions options = new PolylineOptions();
      options.color(Constants.RED_COLOR);
      options.width(10.0f);
      options.zIndex(10);

      if( lstVertex == null ) 
         return;

      for(int i=0; i<lstVertex.size(); i++) { 
         RouteVertex vertex = lstVertex.get(i);

         options.add(vertex.getLocation());
      }

      addNormalRoute(options);      
   }

   public static void removeAllNormalRoute() {
      if( m_lstCurrentlyShowingNonTransparentRoute == null ) 
         return;

      for(int i=0; i<m_lstCurrentlyShowingNonTransparentRoute.size(); i++) {
         Polyline line = m_lstCurrentlyShowingNonTransparentRoute.get(i);

         if( line != null )
            line.remove();
      }
   }

   public static void showStartNavigationButton() {
      if( m_imgBtnStartNavigation != null )
         m_imgBtnStartNavigation.setVisibility(View.VISIBLE);

   }

   public static void hideStartNavigationButton() {
      if( m_imgBtnStartNavigation != null )
         m_imgBtnStartNavigation.setVisibility(View.INVISIBLE);
   }

   public static void showStopNavigationButton() {
      if( m_imgBtnStopNavigation != null )
         m_imgBtnStopNavigation.setVisibility(View.VISIBLE);
   }

   public static void hideStopNavigationButton() {
      if( m_imgBtnStopNavigation != null )
         m_imgBtnStopNavigation.setVisibility(View.INVISIBLE);      
   }

   public static int getCurrentMode() {
      return m_nMode;
   }

   public static void setCurrentMode(int nNewMode) {
      m_nMode = nNewMode;
   }

   public static void showDepartureMarker(LatLng loc) {
      if( loc == null ) 
         return;

      if( m_markerDeparture != null )
         m_markerDeparture.remove();

      // Set..
      Bitmap markerImg = BitmapFactory.decodeResource(MainActivity.m_Resources, R.drawable.ic_marker_start);
      BitmapDescriptor descirptor  = BitmapDescriptorFactory.fromBitmap(markerImg);

      MarkerOptions  option = new MarkerOptions().position(loc)
            .icon(descirptor);         

      m_markerDeparture = GoogleMapFragment.m_map.addMarker(option);   

   }

   public static void hideDepartureMarker() {
      if( m_markerDeparture != null )
         m_markerDeparture.remove();      
   }

   public static void showDestinationMarker(LatLng loc) {
      if( loc == null ) 
         return;

      if( m_markerDestination != null )
         m_markerDestination.remove();

      // Set..
      Bitmap markerImg = BitmapFactory.decodeResource(MainActivity.m_Resources, R.drawable.ic_marker_finish);
      BitmapDescriptor descirptor  = BitmapDescriptorFactory.fromBitmap(markerImg);

      MarkerOptions  option = new MarkerOptions().position(loc)
            .icon(descirptor);         

      m_markerDestination = GoogleMapFragment.m_map.addMarker(option);         
   }

   public static void setTempMarker(LatLng loc) {
      if( loc == null ) 
         return;

      if( m_markerTemp != null )
         m_markerTemp.remove();

      // Set..
      Bitmap markerImg = BitmapFactory.decodeResource(MainActivity.m_Resources, R.drawable.ic_marker_finish);
      BitmapDescriptor descirptor  = BitmapDescriptorFactory.fromBitmap(markerImg);

      MarkerOptions  option = new MarkerOptions().position(loc)
            .icon(descirptor);         

      m_markerTemp = GoogleMapFragment.m_map.addMarker(option);         
   }

   public static void setTempMarker1(LatLng loc) {
      if( loc == null ) 
         return;

      if( m_markerTemp1 != null )
         m_markerTemp1.remove();

      // Set..
      //      Bitmap markerImg = BitmapFactory.decodeResource(MainActivity.m_Resources, R.drawable.ic);
      //      BitmapDescriptor descirptor  = BitmapDescriptorFactory.fromBitmap(markerImg);

      MarkerOptions  option = new MarkerOptions().position(loc);         

      m_markerTemp1 = GoogleMapFragment.m_map.addMarker(option);         
   }

   public static void setTempMarker2(LatLng loc) {
      if( loc == null ) 
         return;

      if( m_markerTemp2 != null )
         m_markerTemp2.remove();

      // Set..
      //      Bitmap markerImg = BitmapFactory.decodeResource(MainActivity.m_Resources, R.drawable.ic);
      //      BitmapDescriptor descirptor  = BitmapDescriptorFactory.fromBitmap(markerImg);

      MarkerOptions  option = new MarkerOptions().position(loc);         

      m_markerTemp2 = GoogleMapFragment.m_map.addMarker(option);         
   }

   public static void hideDestinationMarker() {
      if( m_markerDestination != null )
         m_markerDestination.remove();
   }

   public GoogleMapFragment() {      
      m_polylineRoute = null;
      m_markerWPS = null;
      m_markerKAILOS = null;
      m_circleEstimatedError = null;
      m_overlayIndoorMap = null;      
      m_markerTemp = null;

      m_markerTemp1 = null;
      m_markerTemp2 = null;
      
      m_circleLocation = null;
      m_polyLineHeading = null;

      m_nMode = Constants.NORMAL_MODE;

      m_lstFloorMapsForNavigation = new LinkedList<IndoorMapOverlay>();
      m_lstCurrentlyShowingTransparentRoute = new ArrayList<Polyline>();
      m_lstCurrentlyShowingNonTransparentRoute = new ArrayList<Polyline>();
   }   

   public static GoogleMap getMap() {
      return m_map;
   }

   public static Polyline getPolylineRoute() {
      return m_polylineRoute;
   }

   public static void setPolylineRoute(Polyline newRoute) {
      m_polylineRoute = newRoute;
   }

   public static Marker getMarkerWPS() {
      return m_markerWPS;
   }

   public static Marker getMarkerKAILOS() {
      return m_markerKAILOS;
   }

   public static IndoorMapOverlay getOverlayIndoorMap() {
      return m_overlayIndoorMap;
   }


   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View view = null;

      view = inflater.inflate(R.layout.map_fragment, container, false);

      m_MapFragment = new KAILOSGoogleMapFragment();

      FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
      fragmentTransaction.add(R.id.map_root, m_MapFragment);
      fragmentTransaction.commit();      

      // Getting references...
      m_textViewLocationIndicator = (TextView)view.findViewById(R.id.textViewLocationIndicator);
      m_imgBtnTrackLocation = (ImageButton)view.findViewById(R.id.image_button_track_location);
      m_imgBtnGotoCurrentLocation = (ImageButton)view.findViewById(R.id.image_button_go_to_current_location);
      m_imgBtnStopNavigation = (ImageButton)view.findViewById(R.id.image_button_stop_navigation);
      m_imgBtnStartNavigation = (ImageButton)view.findViewById(R.id.image_button_start_navigation);

      // Listeners...
      m_imgBtnTrackLocation.setOnClickListener(m_listenerTrackLocation);
      m_imgBtnGotoCurrentLocation.setOnClickListener(m_listenerGoToCurrentLocation);
      m_imgBtnStopNavigation.setOnClickListener(m_listenerStopNavigation);
      m_imgBtnStartNavigation.setOnClickListener(m_listenerStartNavigation);

      return view;
   }   

   @Override 
   public void onStart() {
      super.onStart();      
   }

   @Override
   public void onDestroyView() {
      super.onDestroy();
   }


   public static class KAILOSGoogleMapFragment extends SupportMapFragment {
      @Override
      // [2013-10-17] Rakmin.
      // - Description : We need to know when the fragment transaction is committed successfully!
      public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);

         m_map = this.getMap();

         // if map is null, that is a problem with google service library in the client!
         if( m_map != null ) {            
            m_map.moveCamera(CameraUpdateFactory.newLatLngZoom(Constants.KAIST, 12));
            m_map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
         }           
      }
   }
}