package kr.ac.kaist.isilab.kailos.route;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.google.android.gms.maps.model.LatLng;

import kr.ac.kaist.isilab.kailos.indoormap.IndoorMap;
import kr.ac.kaist.isilab.kailos.indoormap.IndoorMapRequestThread;
import kr.ac.kaist.isilab.kailos.location.EstimatedLocation;
import kr.ac.kaist.isilab.kailos.navi.GoogleMapFragment;
import kr.ac.kaist.isilab.kailos.navi.KAILOSService;
import kr.ac.kaist.isilab.kailos.navi.POISearchActivity;
import kr.ac.kaist.isilab.kailos.poi.POI;
import kr.ac.kaist.isilab.kailos.util.Constants;

import android.location.Location;
import android.os.Handler;
import android.os.Message;

public class RouteManager {
	// Routing data, all of them, oh god, that is a lot of statics, damn! 
	private static ArrayList<RouteVertex> m_lstEntirePath;
	private static ArrayList<FloorPath> m_lstFloorPath;
	private static POI m_poiDeparture;
	private static POI m_poiDestination;	
	
	public static Handler m_handlerResponse = new Handler() {
		public void handleMessage(Message msg) {
			switch( msg.what ) {
				case Constants.MESSAGE_ROUTE_THREAD_RESPONSE:
					String strRouteJSON = (String)msg.obj;
					
					if( strRouteJSON == null || strRouteJSON.compareTo("") == 0 ) {
						// Generate message for dismiss routing calculation dialog...					
						Message newMsg = new Message();
						newMsg.what = Constants.MESSAGE_DISMISS_ROUTING_CALCULATION_PROGRESS_DIALOG;
						
						POISearchActivity.m_responseHanlder.sendMessage(newMsg);

						return;						
					}						
			
					// Create a new list for storing the path...
					m_lstEntirePath = new ArrayList<RouteVertex>();
					
					// Parse the vertices and add them into the list....
					try {
						JSONObject json = (JSONObject) JSONValue.parse(strRouteJSON);
						Iterator<?> iter = json.keySet().iterator();
						
						while( iter.hasNext() ) {						
							String key = (String) iter.next();
							Object value = json.get(key);
							
							if( key.compareToIgnoreCase("path") == 0 ) {
								JSONArray pathArray = (JSONArray)value;
								
								for(int i=0; i<pathArray.size(); i++) {
									JSONArray path = (JSONArray)pathArray.get(i);
									
									if( path.size() != 3 ) 
										continue;
									
									RouteVertex newVertex = new RouteVertex(Double.parseDouble(path.get(1).toString()),
																			Double.parseDouble(path.get(0).toString()),
																			path.get(2).toString());
									
									if( newVertex.getFloorID().compareTo("null") == 0 )
										newVertex.setFloorID("");
									
									m_lstEntirePath.add(newVertex);
								}								
							}
						}
					} catch( Exception e ) {
						// No handling!.. yet..
					}
					
					// No result..
					if( m_lstEntirePath.size() == 0 ) {
						// Generate message for dismiss routing calculation dialog...					
						Message newMsg = new Message();
						newMsg.what = Constants.MESSAGE_DISMISS_ROUTING_CALCULATION_PROGRESS_DIALOG;
						
						POISearchActivity.m_responseHanlder.sendMessage(newMsg);
						
						return;
					}
						
					
					// Sift out the floor connecting vertices...
					for(int i=1; i<m_lstEntirePath.size()-1; i++) {
						RouteVertex predecessor = m_lstEntirePath.get(i-1);
						RouteVertex current = m_lstEntirePath.get(i);
						RouteVertex successor = m_lstEntirePath.get(i+1);
												
						if( predecessor.getFloorID().compareTo(current.getFloorID()) != 0 &&
							successor.getFloorID().compareTo(current.getFloorID()) != 0 ) {
							
							m_lstEntirePath.remove(i);
							i--;
						}
					}
					
					
					m_lstFloorPath = new ArrayList<FloorPath>();
					
					// Group those paths by floorID!
					RouteVertex startVertex = m_lstEntirePath.get(0);
					FloorPath floorPath = new FloorPath();
					
					floorPath.setFloorID(startVertex.getFloorID());
					floorPath.getPath().add(startVertex);
					
					for(int i=1; i<m_lstEntirePath.size(); i++) {
						RouteVertex currentVertex = m_lstEntirePath.get(i);
						
						floorPath.getPath().add(currentVertex);
						
						if( startVertex.getFloorID().compareTo(currentVertex.getFloorID()) != 0 ) {
							m_lstFloorPath.add(floorPath);
							
							// new ploy line for the different floor..
							floorPath = new FloorPath();							
							
							floorPath.setFloorID(currentVertex.getFloorID());
							floorPath.getPath().add(currentVertex);
							
							startVertex = currentVertex;							
						}
						
						if( i == m_lstEntirePath.size()-1 )
							m_lstFloorPath.add(floorPath);
					}
					
					// Request indoor maps for both the departure floor and the destination floor!
					String strDepartureFloorID = getDepartureFloorID();
					String strDestinationFloorID = getDestinationFloorID();
					
					if( strDepartureFloorID != null && strDepartureFloorID.compareTo("") != 0 && strDepartureFloorID.compareTo("null") != 0 ) {
						IndoorMapRequestThread departureFloorMapRequestThread = new IndoorMapRequestThread();
						
						departureFloorMapRequestThread.setRequestDataWithFloorID(strDepartureFloorID);
						departureFloorMapRequestThread.setResponseHandler(this);
						
						departureFloorMapRequestThread.start();						
					}
					
					if( strDestinationFloorID != null && strDestinationFloorID.compareTo("") != 0 && strDestinationFloorID.compareTo("null") != 0 ) {
						IndoorMapRequestThread destinationFloorMapRequestThread = new IndoorMapRequestThread();
						
						destinationFloorMapRequestThread.setRequestDataWithFloorID(strDestinationFloorID);
						destinationFloorMapRequestThread.setResponseHandler(this);
						
						destinationFloorMapRequestThread.start();
					}
					
					// Change the mode of the google map...
					GoogleMapFragment.setCurrentMode(Constants.SHOW_ROUTE_MODE);
					
					// Hide all location markers...
					GoogleMapFragment.removeGoogleWPSMarker();
					GoogleMapFragment.removeKAILOSMarker();
					
					// Maker for the departure and the destination...
					GoogleMapFragment.showDepartureMarker(new LatLng(m_poiDeparture.getLatitude(), m_poiDeparture.getLongitude()));
					GoogleMapFragment.showDestinationMarker(new LatLng(m_poiDestination.getLatitude(), m_poiDestination.getLongitude()));
					
					// Generate message for drawing route...
					Message newMsg1 = new Message();
					newMsg1.what = Constants.MESSAGE_DRAW_ENTIRE_ROUTE;
					
					KAILOSService.m_handleRoute.sendMessage(newMsg1);					

					// Generate message for dismiss routing calculation dialog...					
					Message newMsg2 = new Message();
					newMsg2.what = Constants.MESSAGE_DISMISS_ROUTING_CALCULATION_PROGRESS_DIALOG;
					
					POISearchActivity.m_responseHanlder.sendMessage(newMsg2);					
					break;
					
				case Constants.MESSAGE_INDOOR_MAP_THREAD_RESPONSE:
					IndoorMap indoormap = (IndoorMap)msg.obj;
					
					GoogleMapFragment.addIndoorMapOverlayForNavigation(indoormap);
					
					break;
				default:
					break;
			}
		}
	};
	
	public RouteManager() {
	}
	
	public static ArrayList<RouteVertex> getRoutePath() {
		return m_lstEntirePath;
	}
	
	public static void setRoutePath(ArrayList<RouteVertex> newPath) {
		m_lstEntirePath = newPath;
	}
	
	public static void setDeparturePOI(POI poi) {
		m_poiDeparture = poi;
	}
	
	public static void setDestinationPOI(POI poi) {
		m_poiDestination = poi;
	}
	
	// Also called, map matching..
	public static EstimatedLocation FindNearestLocationOnRoute(EstimatedLocation prevLocation) {
		if( m_lstEntirePath == null || m_lstEntirePath.size() == 0 || m_lstFloorPath == null ) 
			return prevLocation;
		
		EstimatedLocation projectedLocation = EstimatedLocation.createFromString(prevLocation.toString());		
		ModifiedCoordinate loc = new ModifiedCoordinate();		
		ArrayList<RouteVertex> lstCurrentFloorVertices = null;		
		
		loc.y = (int)(prevLocation.getLatitude() * 1000000);
		loc.x = (int)(prevLocation.getLongitude() * 1000000);
		
		for(int i=0; i<m_lstFloorPath.size(); i++) {
			FloorPath floorPath = m_lstFloorPath.get(i);
			
			if( floorPath.getFloorID().compareTo(prevLocation.getFloorID()) == 0 ) {
				lstCurrentFloorVertices = floorPath.getPath();
				break;
			}
		}
		
//		lstCurrentFloorVertices = RouteManager.getRoutePath();
		
		// Somehow cannot find routes...
		if( lstCurrentFloorVertices == null )
			return prevLocation;		
		
		// 1) Find the nearest link form prevLoctaion...
		ModifiedCoordinate nearestStart = null;
		ModifiedCoordinate nearestEnd = null;
		
		double dbShortestDist = Double.MAX_VALUE;
		
		for(int i=1; i<lstCurrentFloorVertices.size(); i++) {
			ModifiedCoordinate startCoordinate = new ModifiedCoordinate();
			ModifiedCoordinate endCoordinate = new ModifiedCoordinate();
			
			double dbEdgeLength = 0.0f;
			double distanceFormLocation = 0.0f;
			
			// Prepare an edge connecting (i-1)-th vertex to i-th vertex...
			startCoordinate.x = (int)(lstCurrentFloorVertices.get(i-1).getLocation().longitude * 1000000);
			startCoordinate.y = (int)(lstCurrentFloorVertices.get(i-1).getLocation().latitude * 1000000);
						
			endCoordinate.x = (int)(lstCurrentFloorVertices.get(i).getLocation().longitude * 1000000);
			endCoordinate.y = (int)(lstCurrentFloorVertices.get(i).getLocation().latitude * 1000000);
			
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
//			GoogleMapFragment.setTempMarker1(new LatLng(nearestStart.y/1000000.0f, nearestStart.x/1000000.0f));
//			GoogleMapFragment.setTempMarker2(new LatLng(nearestEnd.y/1000000.0f, nearestEnd.x/1000000.0f));
			
			// I do know this is not pretty but it works..			
			double dbSlope = 0.0f;
			
			if( nearestEnd.x == nearestStart.x )
				dbSlope = 1.0f;				
			else
				dbSlope = ((double)(nearestEnd.y-nearestStart.y)) / ((double)(nearestEnd.x-nearestStart.x));
			
			// Little filtering here...
			if( dbSlope == Double.POSITIVE_INFINITY )
				dbSlope = 1/0.0000001f;
			else if( dbSlope == Double.NEGATIVE_INFINITY )
				dbSlope = -1/0.0000001f;
			else if( dbSlope == 0.0f )
				dbSlope = 0.0000001f;
			
			// Calculate a line equation which cross the edge (nearestStart, neartestEnd)..
			// Equation: (dbSlope)x - C = y
			
			double C = dbSlope*nearestStart.x - nearestStart.y;

			double a = loc.x;
			double b = loc.y;
			
			double c = (a + b*dbSlope + C*dbSlope) / (Math.pow(dbSlope, 2) + 1.0f);
			double d = (a*dbSlope + b*Math.pow(dbSlope, 2) + C*Math.pow(dbSlope, 2)) / (Math.pow(dbSlope, 2)+1.0f) - C;			
						
			// 3) Range check... depending on slope s...
			double dbMin = 0.0f;
			double dbMax = 0.0f;			

			if( dbSlope == 1.0f ) {
				c = nearestStart.x;				
				
				dbMin = nearestStart.y;
				dbMax = nearestEnd.y;
				
//				GoogleMapFragment.setTempMarker1(new LatLng(dbMinD / 1000000.0f, nearestStart.x / 1000000.0f));
//				GoogleMapFragment.setTempMarker2(new LatLng(dbMaxD / 1000000.0f, nearestEnd.x / 1000000.0f));				
				
				// Normalize!
				if( dbMin > dbMax ) {
					double temp = dbMin;
					dbMin = dbMax;
					dbMax = temp;
				}
				
				// Line segment range check...
				if( dbMin > d || dbMax < d ) {
					double dbDistanceFromStart = ModifiedCoordinate.distance(nearestStart, loc);
					double dbDistanceFromEnd = ModifiedCoordinate.distance(nearestEnd, loc);
					
					if( dbDistanceFromStart > dbDistanceFromEnd ) {
						c = nearestEnd.x;
						d = nearestEnd.y;						
					} else {
						c = nearestStart.x;
						d = nearestStart.y;
					}	
				}				
			} else if( dbSlope == 0.0f ) {
				d = nearestStart.y;
						
				dbMin = nearestStart.x;
				dbMax = nearestEnd.x;
				
				// Normalize..
				if( dbMin > dbMax ) {
					double temp = dbMin;
					dbMin = dbMax;
					dbMax = temp;
				}				
			
//				GoogleMapFragment.setTempMarker1(new LatLng(nearestStart.y / 1000000.0f, dbMinC / 1000000.0f));
//				GoogleMapFragment.setTempMarker2(new LatLng(nearestEnd.y / 1000000.0f, dbMaxC / 1000000.0f));
				
				// Line segment range check...
				if( dbMin > c || dbMax < c ) {
					double dbDistanceFromStart = ModifiedCoordinate.distance(nearestStart, loc);
					double dbDistanceFromEnd = ModifiedCoordinate.distance(nearestEnd, loc);
					
					if( dbDistanceFromStart > dbDistanceFromEnd ) {
						c = nearestEnd.x;
						d = nearestEnd.y;						
					} else {
						c = nearestStart.x;
						d = nearestStart.y;
					}	
				}
			} else {
				dbMin = nearestStart.x * dbSlope - C;
				dbMax = nearestEnd.x * dbSlope - C;
				
//				GoogleMapFragment.setTempMarker1(new LatLng(dbMinD / 1000000.0f, nearestStart.x / 1000000.0f));
//				GoogleMapFragment.setTempMarker2(new LatLng(dbMaxD / 1000000.0f, nearestEnd.x / 1000000.0f));				
				
				if( dbMin > dbMax ) {
					double temp = dbMin;
					dbMin = dbMax;
					dbMax = temp;
				}
				
				// Line segment range check...				
				if( dbMin > d || dbMax < d ) {
					double dbDistanceFromStart = ModifiedCoordinate.distance(nearestStart, loc);
					double dbDistanceFromEnd = ModifiedCoordinate.distance(nearestEnd, loc);
					
					if( dbDistanceFromStart > dbDistanceFromEnd ) {
						c = nearestEnd.x;
						d = nearestEnd.y;						
					} else {
						c = nearestStart.x;
						d = nearestStart.y;
					}	
				}
				
			}
			
			projectedLocation.setLatitude(d/1000000.0f);
			projectedLocation.setLongitude(c/1000000.0f);
			
//			GoogleMapFragment.setTempMarker(new LatLng(d/1000000.0f, c/1000000.0f));
		}
		
		return projectedLocation;
	}	

	public static String getDepartureFloorID() {		
		if( m_lstFloorPath == null || m_lstFloorPath.size() == 0 )
			return "";
				
		FloorPath pathOnDepartureFloor = m_lstFloorPath.get(0);
		
		if( pathOnDepartureFloor == null || pathOnDepartureFloor.getPath() == null || pathOnDepartureFloor.getPath().size() == 0 )
			return "";
				
		return pathOnDepartureFloor.getPath().get(0).getFloorID();
	}
	
	public static String getDestinationFloorID() {
		if( m_lstFloorPath == null || m_lstFloorPath.size() == 0 )
			return "";
		
		FloorPath pathOnDestinationFloor = m_lstFloorPath.get(m_lstFloorPath.size()-1);
		
		if( pathOnDestinationFloor == null || pathOnDestinationFloor.getPath() == null || pathOnDestinationFloor.getPath().size() == 0 )
			return "";
				
		return pathOnDestinationFloor.getPath().get(0).getFloorID();
	}
	
	public static ArrayList<RouteVertex> getDepartureFloorVertexList() {
		if( m_lstFloorPath == null || m_lstFloorPath.size() == 0 )
			return null;
		
		FloorPath pathOnFloor = m_lstFloorPath.get(0);		
		
		if( pathOnFloor == null || pathOnFloor.getPath() == null || pathOnFloor.getPath().size() == 0 )
			return null;
		
		return pathOnFloor.getPath();		
	}
	
	public static ArrayList<RouteVertex> getDestinationFloorVertexList() {
		if( m_lstFloorPath == null || m_lstFloorPath.size() == 0 )
			return null;
		
		FloorPath pathOnFloor = m_lstFloorPath.get(m_lstFloorPath.size()-1);
		
		if( pathOnFloor == null || pathOnFloor.getPath() == null || pathOnFloor.getPath().size() == 0 )
			return null;
				
		return pathOnFloor.getPath();
	}
	
	public static ArrayList<RouteVertex> getOutdoorVertexList() {
		if( m_lstEntirePath == null || m_lstEntirePath.size() == 0 )
			return null;
		
		ArrayList<RouteVertex> lstRet = new ArrayList<RouteVertex>();		
		Iterator<RouteVertex> iter = m_lstEntirePath.iterator();
		
		while( iter.hasNext() ) {
			RouteVertex vertex = iter.next();
			
			lstRet.add(vertex);
			
			if( vertex.getFloorID().compareTo("") == 0 )
				break;
		}
			
		return lstRet;
	}
	
	public static boolean AreDepartureAndDestinationOnTheSameBuilding() {
		if( m_lstEntirePath == null ) 
			return false;
		
		boolean bRet = false;
		
		for(int i=0; i<m_lstEntirePath.size(); i++) {
			RouteVertex vertex  = m_lstEntirePath.get(i);
			
			if( vertex.getFloorID().compareTo("") == 0 ) {
				bRet = true;
				break;
			}	
		}
		
		return bRet;
	}
	
	public static ArrayList<RouteVertex> getRoutesOnFloor(String strFloorID) {
		if( m_lstFloorPath == null || strFloorID == null )
			return null;
		
		for(int i=0; i<m_lstFloorPath.size(); i++) {
			FloorPath floorPath = m_lstFloorPath.get(i);
			
			if( floorPath.getFloorID().compareTo(strFloorID) == 0 )
				return floorPath.getPath();			
		}
		
		return null;
	}
	
	public static ArrayList<FloorPath> getPathGroupedByFloorID() {
		return m_lstFloorPath;
	}
	
	public static double distanceToDestination(EstimatedLocation currentLocation) {
		float fDistance[] = new float[1];
		
		Location.distanceBetween(m_poiDestination.getLatitude(), m_poiDestination.getLongitude(), currentLocation.getLatitude(), currentLocation.getLongitude(), fDistance);
		
		return fDistance[0];		
	}

	
	private static class ModifiedCoordinate {
		public int x;
		public int y;
		
		public static double distance(ModifiedCoordinate x, ModifiedCoordinate y) {
			return Math.sqrt(Math.pow(x.x - y.x, 2.0f) + Math.pow(x.y - y.y, 2.0f));
		}
		
	}

}
