package kr.ac.kaist.isilab.kailos.poi;

import java.util.ArrayList;
import kr.ac.kaist.isilab.kailos.navi.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class POIItemAdapter extends ArrayAdapter<POI> {
	private ArrayList<POI> m_lstItems;
	
	public POIItemAdapter(Context context, int resource, ArrayList<POI> objects) {
		super(context, resource, objects);
		
		m_lstItems = objects;		
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		
		if( view == null ) {
			// Gotta inflate!
			LayoutInflater inflater = LayoutInflater.from(getContext());
			view = inflater.inflate(R.layout.query_result_list_item, null);
		}
		
		POI item = m_lstItems.get(position);
		
		if( item != null ) {
			TextView textViewPoiName = (TextView)view.findViewById(R.id.text_poi_name);
			TextView textViewCategory = (TextView)view.findViewById(R.id.text_category);
			TextView textViewBuildingName = (TextView)view.findViewById(R.id.text_building_name);
			TextView textViewRoomNumber = (TextView)view.findViewById(R.id.text_room_number);
			
			textViewCategory.setText(item.getCategory() + "  ");
			textViewBuildingName.setText("NOT AVAILABLE" + "  ");
			textViewRoomNumber.setText(item.getRoomNumber() + " ");
			textViewPoiName.setText(item.getPlaceNameEng());
			
		}
				
		return view;
	}
}
