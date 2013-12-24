package kr.ac.kaist.isilab.kailos.route;

import java.util.ArrayList;

public class FloorPath {
	ArrayList<RouteVertex> m_lstPath;
	String m_strFloorID;
	
	public FloorPath() {
		m_strFloorID = "";			
		m_lstPath = new ArrayList<RouteVertex>();			
	}
	
	public String getFloorID() {
		return m_strFloorID;
	}
	
	public void setFloorID(String newID) {
		m_strFloorID = newID;
	}
	
	public ArrayList<RouteVertex> getPath() {
		return m_lstPath;
	}
	
	public void setPath(ArrayList<RouteVertex> newPath) {
		m_lstPath = newPath;
	}
}