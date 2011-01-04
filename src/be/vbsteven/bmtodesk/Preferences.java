package be.vbsteven.bmtodesk;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(android.R.style.Theme_Light_NoTitleBar);
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
		
		Preference pref = findPreference("WATCHINTROVID");
		pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=RH4O019v2iA")));
				
				return true;
			}
		});
		
		pref = findPreference("CREATEACC");
		pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				startActivity(new Intent(getApplicationContext(), RegistrationActivity.class));
				
				return true;
			}
		});
		
	}
}
