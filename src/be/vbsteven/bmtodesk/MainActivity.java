package be.vbsteven.bmtodesk;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {

	private static final int MENU_SETTINGS = 0;
	private static final int MENU_CREATE_ACCOUNT = 1;
	private static final int MENU_EXPORT_ALL = 2;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		setTheme(android.R.style.Theme_Light_NoTitleBar);
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);
	}

	@Override
	protected void onResume() {
		init();
		super.onResume();
	}

	private void init() {
		if (!Global.hasUserCredentials(this)) {
			// no credentials yet
			// show setup wizard
			startActivity(new Intent(this, RegistrationActivity.class));
			finish();
		}

		if (!Global.hasSeenYoutubeVideo(this)) {
			showYoutubeVideoPopup();
		}
		
	}

	private void showYoutubeVideoPopup() {
		final Dialog dialog = new AlertDialog.Builder(this)
				.setTitle("See introduction video")
				.setMessage(
						"Before you start using this application. Do you want to watch an introduction video first?")
				.setPositiveButton("Play video", new OnClickListener() {

					@Override
					public void onClick(final DialogInterface dialog,
							final int which) {
						// show video
						final Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(Uri
								.parse("http://www.youtube.com/watch?v=RH4O019v2iA"));
						startActivity(intent);
						Global.setSeenYoutubeVideo(MainActivity.this);
					}
				}).setNegativeButton("Later", new OnClickListener() {

					@Override
					public void onClick(final DialogInterface dialog,
							final int which) {
						Global.setSeenYoutubeVideo(MainActivity.this);
					}
				}).setOnCancelListener(new OnCancelListener() {

					@Override
					public void onCancel(final DialogInterface dialog) {
						Global.setSeenYoutubeVideo(MainActivity.this);
					}
				}).create();
		dialog.show();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		menu.add(0, MENU_SETTINGS, 0, "Settings").setIcon(
				R.drawable.ic_menu_preferences);
		if (!Global.hasUserCredentials(this)) {
			menu.add(0, MENU_CREATE_ACCOUNT, 0, "Create account").setIcon(
					android.R.drawable.ic_menu_add);
		} else {
			menu.add(0, MENU_EXPORT_ALL, 0, "Export all bookmarks").setIcon(
					android.R.drawable.ic_menu_upload);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case MENU_SETTINGS:
			startActivity(new Intent(this, Preferences.class));
			break;
		case MENU_CREATE_ACCOUNT:
			startActivity(new Intent(this, RegistrationActivity.class));
			break;
		case MENU_EXPORT_ALL:
			startActivity(new Intent(this,
					SendAllCurrentBookmarksActivity.class));
			break;
		}
		return true;
	}
}