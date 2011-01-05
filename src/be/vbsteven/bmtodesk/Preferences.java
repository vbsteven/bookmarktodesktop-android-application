/* 
 * Copyright (C) 2010-2011 Steven Van Bael <steven.v.bael@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA 
 */
package be.vbsteven.bmtodesk;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

/**
 * main preferences window
 * 
 * all preferences are loaded from prefs.xml and most of them are good by default
 * the two action preferences for "wacht intro video" and "create account" need extra initialization
 * 
 * @author steven
 */
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
