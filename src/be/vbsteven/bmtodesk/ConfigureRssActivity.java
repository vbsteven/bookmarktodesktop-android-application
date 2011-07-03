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
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * setup screen for the personal RSS feed
 *
 * @author steven
 */
public class ConfigureRssActivity extends Activity {


	private String rsslink;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(android.R.style.Theme_Light_NoTitleBar);
		super.onCreate(savedInstanceState);

		setContentView(R.layout.configrss);

		rsslink = calculateLink();

		TextView tvlink = (TextView)findViewById(R.id.tv_rsslink);
		tvlink.setText(rsslink);

		Button button = (Button) findViewById(R.id.but_sendrss);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_SEND);
				i.setType("message/rfc822");
				i.putExtra(Intent.EXTRA_SUBJECT,"Personal RSS feed for Bookmark to Desktop");
				i.putExtra(Intent.EXTRA_TEXT,"Dear user,\n\nThanks for using Bookmark to Desktop for Android.\n\nYou can find your personal RSS feed on " + rsslink + "\n\nSteven\nhttp://bookmarktodesktop.appspot.com");
				startActivity(Intent.createChooser(i, "Select email application."));
			}
		});
	}

	private String calculateLink() {
		String link;

		String username = Global.getUsername(this);
		String password = Global.getPassword(this);

	    String key = MD5.md5(username + ":" + password);

	    link = Global.getDomain() + "/rss/" + username + "/"+ key;
		return link;
	}
}
