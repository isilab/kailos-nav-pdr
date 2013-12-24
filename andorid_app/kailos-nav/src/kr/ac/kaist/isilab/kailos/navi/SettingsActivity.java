package kr.ac.kaist.isilab.kailos.navi;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

public class SettingsActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Adding a back button at the action bar...
		getActionBar().setHomeButtonEnabled(true);
		
		// Load settings fragment..
		setContentView(R.layout.settings_activity);
		getFragmentManager().beginTransaction().replace(R.id.setting_contents, new SettingsFragment()).commit();
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
}
