package be.vbsteven.bmtodesk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class AfterSuccessfulSendActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(android.R.style.Theme_Light_NoTitleBar);
		super.onCreate(savedInstanceState);

		setContentView(R.layout.aftersend);

		Button button = (Button) findViewById(R.id.but_takemeback);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

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

		if (getIntent().hasExtra(Global.EXTRA_COUNT)) {
			// we are called after a batch upload
			int count = getIntent().getIntExtra(Global.EXTRA_COUNT, 0);
			TextView tv = (TextView) findViewById(R.id.tv1);
			tv.setText(count
					+ " bookmarks sent!\n\nIf you have installed the correct extension in your browser then these bookmarks will shortly show up in the bookmarktodesktop folder on your bookmarks bar.");
		}
	}
}
