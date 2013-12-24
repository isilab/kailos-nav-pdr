package kr.ac.kaist.isilab.kailos.poi;

public class POIListItem {
	private String m_strMainTitle;
	private String m_strSubTitle;
	
	public POIListItem(String _mainTitle, String _subTitle) {
		m_strMainTitle = _mainTitle;
		m_strSubTitle = _subTitle;
	}
	
	public String getMainTitle() {
		return m_strMainTitle;
	}
	
	public String getSubTitle() {
		return m_strSubTitle;
	}
}
