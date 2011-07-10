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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

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

	private ListView incomingListView;
	private BookmarkStore bookmarkStore;
	private Button upgradeButton;

	@Override
	/**
	 * called when the activity is first created
	 */
	public void onCreate(final Bundle savedInstanceState) {
		setTheme(android.R.style.Theme_Light_NoTitleBar);
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		LinearLayout notpaidLayout = (LinearLayout)findViewById(R.id.content_notpaid);
		upgradeButton = (Button)findViewById(R.id.but_upgrade);
		upgradeButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				buyPremium();
			}
		});

		initIncomingBookmarks();

		if (Licensing.verify(this)) {
			notpaidLayout.setVisibility(View.GONE);
		} else {
			incomingListView.setVisibility(View.GONE);
		}
	}

	private void initIncomingBookmarks() {
		bookmarkStore = BookmarkStore.get(this);

		incomingListView = (ListView)findViewById(R.id.content_lv);
		incomingListView.setAdapter(new IncomingBookmarkAdapter(this, bookmarkStore.getLatestIncomingBookmarks()));

		incomingListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				IncomingBookmark clickedBm = (IncomingBookmark)arg0.getItemAtPosition(arg2);
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(clickedBm.url));
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(i);

			}
		});

	}

	@Override
	/**
	 * called when the activity is moved to the foreground
	 */
	protected void onResume() {
		init();
		super.onResume();

		if (Global.hasUserCredentials(this) && Global.isPaid(this)) {
			C2DM.registerToC2DM(this, true);
		}

		if (Global.hasUserCredentials(this) && !Global.hasSeenFreemiumMessage(this)) {
			showFreemiumMessage();
		}
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

		button = (Button)findViewById(R.id.but_configurerss);
		button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, ConfigureRssActivity.class));
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

	private void showFreemiumMessage() {
		String message = "Bookmark to Desktop uses the freemium model. This means that basic functionality is available in the free version and extended functionality is available if you upgrade your license.\n\n";
		message += "By buying the premium license from the Android market you are supporting further development and maintenance of this application. The generated income will be used to buy coffee that keeps me awake after my day job so I can work on these projects.\n\n";
		message += "Features in the free version:\n\n";
		message += "* Send links from your Android device to the desktop extension\n";
		message += "* If the browser is open, sent links will be opened immediately (PUSH)\n";
		message += "* Sent links will be saved to the bookmarktodesktop folder in your bookmarks bar\n";
		message += "* Request your previously sent links via your own personalized RSS feed\n\n";
		message += "Extra features for premium users:\n\n";
		message += "* Send links from the desktop extension to your Android phone\n";
		message += "* That fuzzy warm feeling you get inside when you realize you supported a developer for the many late night hours he spent working on the application you love";

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Important changes, please read");
		builder.setMessage(message);
		builder.setPositiveButton("Buy premium license", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				buyPremium();
				Global.markFreemiumPopupSeen(MainActivity.this);
			}
		});
		builder.setNegativeButton("Use free version", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Global.markFreemiumPopupSeen(MainActivity.this);
			}
		});
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				Global.markFreemiumPopupSeen(MainActivity.this);
			}
		});
		builder.create().show();
	}

	private void buyPremium() {
		String url = "http://market.android.com/details?id=be.vbsteven.bmtodesklicense";
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);
	}

}