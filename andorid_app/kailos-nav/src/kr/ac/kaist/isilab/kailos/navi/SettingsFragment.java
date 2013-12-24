package kr.ac.kaist.isilab.kailos.navi;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Load preference fragment xml...
		addPreferencesFromResource(R.layout.settings_fragment);		
	}
}
