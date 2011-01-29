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

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

/**
 * main activity the user sees when opening the app
 *
 * @author steven
 */
public class MainActivity extends Activity {

	// constants for the menu
	private static final int MENU_SETTINGS = 0;
	private static final int MENU_CREATE_ACCOUNT = 1;
	private static final int MENU_EXPORT_ALL = 2;

	@Override
	/**
	 * called when the activity is first created
	 */
	public void onCreate(final Bundle savedInstanceState) {
		setTheme(android.R.style.Theme_Light_NoTitleBar);
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);
	}

	@Override
	/**
	 * called when the activity is moved to the foreground
	 */
	protected void onResume() {
		init();
		super.onResume();
	}

	/**
	 * checks if the registration wizard or the intro video popup needs to be shown
	 */
	private void init() {
		if (!Global.hasUserCredentials(this)) {
			// no credentials yet
			// show setup wizard
			startActivity(new Intent(this, RegistrationActivity.class));
			finish();
		}

		Button button = (Button)findViewById(R.id.but_downloadaddons);
		button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, ConfigureAddonActivity.class));
			}
		});


		button = (Button)findViewById(R.id.but_showvideo);
		button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showYoutubeVideo();
			}
		});


	}

	/**
	 * shows the youtube introduction video
	 */
	private void showYoutubeVideo() {
		final Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse("http://www.youtube.com/watch?v=RH4O019v2iA"));
		startActivity(intent);
	}

	@Override
	/**
	 * creates the options menu the first time the menu button is clicked
	 */
	public boolean onCreateOptionsMenu(final Menu menu) {
		menu.add(0, MENU_SETTINGS, 0, "Settings").setIcon(R.drawable.ic_menu_preferences);
		if (!Global.hasUserCredentials(this)) {
			menu.add(0, MENU_CREATE_ACCOUNT, 0, "Create account").setIcon(android.R.drawable.ic_menu_add);
		} else {
			menu.add(0, MENU_EXPORT_ALL, 0, "Export all bookmarks").setIcon(android.R.drawable.ic_menu_upload);
		}
		return true;
	}

	@Override
	/**
	 * handles a click in the menu
	 */
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case MENU_SETTINGS:
			startActivity(new Intent(this, Preferences.class));
			break;
		case MENU_CREATE_ACCOUNT:
			startActivity(new Intent(this, RegistrationActivity.class));
			break;
		case MENU_EXPORT_ALL:
			startActivity(new Intent(this, SendAllCurrentBookmarksActivity.class));
			break;
		}
		return true;
	}
}