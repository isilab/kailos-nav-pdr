package kr.ac.kaist.isilab.kailos.drawmenu;

/**
 * 
 * Model per item menu
 * 
 * @author gabriele
 *
 */
public class NsMenuItemModel {

	public int title;
	public int iconRes;
	public int counter;
	public boolean isHeader;

	public NsMenuItemModel(int title, int iconRes,boolean header,int counter) {
		this.title = title;
		this.iconRes = iconRes;
		this.isHeader=header;
		this.counter=counter;
	}
	
	public NsMenuItemModel(int title, int iconRes,boolean header){
		this(title,iconRes,header,0);
	}
	
	public NsMenuItemModel(int title, int iconRes) {
		this(title,iconRes,false);
	}
	
	
	
}
