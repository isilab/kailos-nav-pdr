package kr.ac.kaist.isilab.kailos.roadnetwork;

import java.util.ArrayList;
import java.util.Iterator;

import kr.ac.kaist.isilab.kailos.location.EstimatedLocation;
import kr.ac.kaist.isilab.kailos.navi.GoogleMapFragment;
import kr.ac.kaist.isilab.kailos.util.Constants;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import android.os.Handler;
import android.os.Message;

import com.google.android.gms.maps.model.LatLng;

public class RoadNetworkManager {
   public static String m_strFloorID;
   public static ArrayList<Road> m_lstRoads;
   
   public static Handler m_handlerResponse = new Handler() {
      public void handleMessage(Message msg) {
         switch( msg.what ) {
            case Constants.MESSAGE_ROAD_NETWORK_RESPONSE:
               String strRouteJSON = (String)msg.obj;
   
               // Create a new list for storing the received road network..
               m_lstRoads = new ArrayList<Road>();
               
               // Parse the vertices and add them into the list....
               try {
                  JSONObject json = (JSONObject) JSONValue.parse(strRouteJSON);
                  Iterator<?> iter = json.keySet().iterator();
                  
                  while( iter.hasNext() ) {                  
                     String key = (String) iter.next();
                     Object value = json.get(key);
                     
                     if( key.compareToIgnoreCase("road") == 0 ) {
                        JSONArray roadArray = (JSONArray)value;
                        
                        for(int i=0; i<roadArray.size(); i++) {
                           JSONArray road = (JSONArray)roadArray.get(i);
                           
                           if( road.size() != 2 )      // Each road must contain both start and end points... 
                              continue;
                           
                           JSONArray startPoint = (JSONArray)road.get(0);
                           JSONArray endPoint = (JSONArray)road.get(1);
                           
                           LatLng latlngStart = new LatLng(Double.parseDouble(startPoint.get(1).toString()), Double.parseDouble(startPoint.get(0).toString())); 
                           LatLng latlngEnd = new LatLng(Double.parseDouble(endPoint.get(1).toString()), Double.parseDouble(endPoint.get(0).toString()));
                           
                           
                           Road newRoad = new Road(latlngStart, latlngEnd);
                           
                           // FloorID Check!
                           if( startPoint.get(2).toString().compareTo(endPoint.get(2).toString()) != 0 )
                              if( startPoint.get(2).toString().compareTo("null") != 0 && endPoint.get(2).toString().compareTo("null") != 0 )
                                 continue;
                           
                           m_lstRoads.add(newRoad);
                        }                        
                     }
                  }
               } catch( Exception e ) {
                  // No handling!.. yet..
               }                  
            default:
               break;
         }
      }
   };
   
   // Also called, map matching..
   public static LatLng FindNearestLocationOnRoad(LatLng prevLocation) {
      LatLng projectedLocation = null;

      if( m_lstRoads == null || m_lstRoads.size() == 0 ) 
         return prevLocation;         
      
      synchronized(m_lstRoads) {               
         ModifiedCoordinate loc = new ModifiedCoordinate();
         
         loc.y = (int)(prevLocation.latitude * 1000000);
         loc.x = (int)(prevLocation.longitude * 1000000);
         
         // 1) Find the nearest link form prevLoctaion...
         ModifiedCoordinate nearestStart = null;
         ModifiedCoordinate nearestEnd = null;
         
         double dbShortestDist = Double.MAX_VALUE;
         
         for(int i=0; i<m_lstRoads.size(); i++) {
            ModifiedCoordinate startCoordinate = new ModifiedCoordinate();
            ModifiedCoordinate endCoordinate = new ModifiedCoordinate();
            Road road = m_lstRoads.get(i);
            
            double dbEdgeLength = 0.0f;
            double distanceFormLocation = 0.0f;
            
            // Integerize(?)...
            startCoordinate.x = (int)(road.getStartPoint().longitude * 1000000);
            startCoordinate.y = (int)(road.getStartPoint().latitude * 1000000);
                     
            endCoordinate.x = (int)(road.getEndPoint().longitude * 1000000);
            endCoordinate.y = (int)(road.getEndPoint().latitude * 1000000);

            
            dbEdgeLength = Math.sqrt(Math.pow(startCoordinate.x-endCoordinate.x, 2.0f) + Math.pow(startCoordinate.y-endCoordinate.y, 2.0f));
            
            if( dbEdgeLength == 0 ) 
               distanceFormLocation = Math.sqrt(Math.pow(startCoordinate.y-loc.y, 2.0f) + Math.pow(startCoordinate.x-loc.x, 2.0f));            
            else {
               distanceFormLocation = (loc.x-startCoordinate.x) * (endCoordinate.x-startCoordinate.x) + (loc.y-startCoordinate.y) * (endCoordinate.y-startCoordinate.y);
               distanceFormLocation /= dbEdgeLength;
               
               if( distanceFormLocation <= 0 )
                  distanceFormLocation = Math.sqrt(Math.pow(startCoordinate.y-loc.y, 2.0f) + Math.pow(startCoordinate.x-loc.x, 2.0f));
               else if( distanceFormLocation >= dbEdgeLength )                
                  distanceFormLocation = Math.sqrt(Math.pow(endCoordinate.y-loc.y, 2.0f) + Math.pow(endCoordinate.x-loc.x, 2.0f));
               else 
                  distanceFormLocation = Math.abs( (-1)*(loc.x-startCoordinate.x)*(endCoordinate.y-startCoordinate.y)+(loc.y-startCoordinate.y)*(endCoordinate.x-startCoordinate.x)) / dbEdgeLength;         
            }
            
            // New shortest?
            if( distanceFormLocation < dbShortestDist ) {
               dbShortestDist = distanceFormLocation;
               nearestStart = startCoordinate;
               nearestEnd = endCoordinate;
            }
         }
         
         // 2) Projection onto the line going through both nearestStart and nearestEnd...
         if( nearestStart != null && nearestEnd != null ) {         
            /*GoogleMapFragment.setTempMarker1(new LatLng(nearestStart.y/1000000.0f, nearestStart.x/1000000.0f));
            GoogleMapFragment.setTempMarker2(new LatLng(nearestEnd.y/1000000.0f, nearestEnd.x/1000000.0f));*/
            
            double A = loc.x - nearestStart.x;
            double B = loc.y - nearestStart.y;
            double C = nearestEnd.x - nearestStart.x;
            double D = nearestEnd.y - nearestStart.y;
            double dot = A*C + B*D;
            double len_sq = C*C + D*D;
            double xx = 0;
            double yy = 0;
            
            double param = dot / len_sq;
            
            if( param < 0 || (nearestStart.x == nearestEnd.x && nearestStart.y == nearestEnd.y) ) {
               xx = nearestStart.x;
               yy = nearestStart.y;               
            } else if ( param > 1 ) {
               xx = nearestEnd.x;
               yy = nearestEnd.y;
            } else {
               xx = nearestStart.x + param*C;
               yy = nearestStart.y + param*D;
            }
            
            projectedLocation = new LatLng(yy/1000000.0f, xx/1000000.0f);            
         //   GoogleMapFragment.setTempMarker(new LatLng(yy/1000000.0f, xx/1000000.0f));
         }         
      }
      
      if( projectedLocation != null )
         return projectedLocation;
      else
         return prevLocation;
   }   

   private static class ModifiedCoordinate {
      public int x;
      public int y;
      
      public static double distance(ModifiedCoordinate x, ModifiedCoordinate y) {
         return Math.sqrt(Math.pow(x.x - y.x, 2.0f) + Math.pow(x.y - y.y, 2.0f));
      }
      
   }

}