package kr.ac.kaist.isilab.kailos.util;

import java.util.ArrayList;

import android.graphics.Color;

public class Util {
	private static int m_nIndex;
	private static ArrayList<Integer> m_lstColors;	
	
	// Probably, never, ever gonna use it...
	public static void InitColorManager() {
		m_lstColors = new ArrayList<Integer>();
		
		m_lstColors.add(Color.rgb(237,28,36));		// Red...
		m_lstColors.add(Color.rgb(34,177,76));		// Green...
		m_lstColors.add(Color.rgb(0,162,232)); 		// Blue...
		m_lstColors.add(Color.rgb(63,72,204)); 		// Dark Blue...
		m_lstColors.add(Color.rgb(163,73,164)); 	// Purple..
		
		m_nIndex = 0;
	}
	
	public static int getNextColor() {
		return m_lstColors.get((m_nIndex++)%m_lstColors.size());		
	}
}
