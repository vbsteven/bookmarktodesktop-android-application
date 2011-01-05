package be.vbsteven.bmtodesk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;


/**
 * activity that shows post-sharing information
 * 
 * @author steven
 */
public class AfterSuccessfulSendActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(android.R.style.Theme_Light_NoTitleBar);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aftersend);

		// setup back button
		Button button = (Button) findViewById(R.id.but_takemeback);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish(); // finish the activity so we go back to the previous one
			}
		});

		// setup share button
		// this sends an intent that allows the user to share the given text with his favorite app (twitter,facebook,mail,...)
		button = (Button) findViewById(R.id.but_shareapp);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent share = new Intent(Intent.ACTION_SEND);
				share.setType("text/plain");
				share.putExtra(
						Intent.EXTRA_TEXT,
						"I shared a bookmark from my Android phone to my desktop browser with #BookmarkToDesktop. http://bit.ly/a5CtSP");
				startActivity(share);
			}
		});

		// change the displayed text when this activity is started after a batch upload 
		if (getIntent().hasExtra(Global.EXTRA_COUNT)) {
			int count = getIntent().getIntExtra(Global.EXTRA_COUNT, 0);
			TextView tv = (TextView) findViewById(R.id.tv1);
			tv.setText(count
					+ " bookmarks sent!\n\nIf you have installed the correct extension in your browser then these bookmarks will shortly show up in the bookmarktodesktop folder on your bookmarks bar.");
		}
	}
}
