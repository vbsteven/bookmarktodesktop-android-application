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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Browser;
import android.provider.Browser.BookmarkColumns;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * activity that sends all current bookmarks in the android browser to the server
 * 
 * FIXME: this part was quickly hacked together because of high user demand
 *        it could use a big cleanup + documentation
 * 
 * @author steven
 */
public class SendAllCurrentBookmarksActivity extends Activity {
	
	private ArrayList<Bookmark> queue = new ArrayList<SendAllCurrentBookmarksActivity.Bookmark>();
	private int totalqueuesize = 0;
	private ProgressDialog progress;
	private String responseMessage = "";
	private Handler handler;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		setTheme(android.R.style.Theme_Light_NoTitleBar);
		super.onCreate(savedInstanceState);
		handler = new Handler();
		
		setContentView(R.layout.sendallcurrent);
		
		final Button button = (Button)findViewById(R.id.but_sendall);
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(final View v) {
				sendAllBookmarks();
			}
		});
	}

	protected void sendAllBookmarks() {
		// first update queue
		queue = getAllBookmarks();
		totalqueuesize = queue.size();
		
		if (totalqueuesize <= 0) {
			Utils.showMessage(this, "Invalid bookmarks", "No bookmarks were found in the browser");
			return;
		}
		
		showProgress();
		processNextBookmark();
	}
	
	protected void processNextBookmark() {
		if (totalqueuesize <= 0) {
			// started without bookmarks, should not happen
			hideProgress();
			return;
		}
		
		if (queue.size() <= 0) {
			// queue is empty, end of run
			hideProgress();
			Intent intent = new Intent(this, AfterSuccessfulSendActivity.class);
			intent.putExtra(Global.EXTRA_COUNT, totalqueuesize);
			startActivity(intent);
			finish();
			return;
		}
		
		// pick first from the queue, remove and process it
		final Bookmark b = queue.get(0);
		queue.remove(0);
		
		int progresscount = totalqueuesize-queue.size(); 
		progress.setMessage("Sending bookmarks to server (" +  progresscount + " of " + totalqueuesize + ")");
		
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				doPost(Global.getUsername(SendAllCurrentBookmarksActivity.this), Global.getPassword(SendAllCurrentBookmarksActivity.this), b.title, b.url);
			}
		});
		thread.start();
	}
	
	public void doPost(String username, String password, String title, String url) {
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost post = new HttpPost(URI.create("shttp://bookmarktodesktop.appspot.com/addbookmark"));
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
			nameValuePairs.add(new BasicNameValuePair("username", username));
			nameValuePairs.add(new BasicNameValuePair("password", password));
			nameValuePairs.add(new BasicNameValuePair("title", title));
			nameValuePairs.add(new BasicNameValuePair("url", url));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response;
			response = httpclient.execute(post);

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent(), "UTF-8"));
			responseMessage = reader.readLine();
			handler.post(afterRequestRunnable);
		} catch (Exception e) {
			responseMessage = "REQUESTFAILED";
			handler.post(afterRequestRunnable);
		}

	}
	
	private Runnable afterRequestRunnable = new Runnable() {
		
		@Override
		public void run() {
			onRegistrationResult(responseMessage);
		}
	};
	
	private void onRegistrationResult(String message) {
		Log.d(Global.TAG, "message: " + message);
		if (message == null) {
			hideProgress();
			Utils.showMessage(this, "Sending bookmark failed", "Please try again later or contact me on twitter: @vbsteven");
			return;
		}
		
		if (message.startsWith("INVALIDENTRY")) {
			hideProgress();
			Utils.showMessage(this, "Invalid input", "Please provide a title and a url");
			return;
		} else if (message.startsWith("INVALIDLOGIN")) {
			hideProgress();
			Utils.showMessage(this, "Authentication failed",
					"Please check your account info in preferences");
		} else if (message.startsWith("SUCCESSFUL")) {
			processNextBookmark();
			
		} else if (message.startsWith("REQUESTFAILED")) {
			hideProgress();
			Utils.showMessage(this, "Sending bookmark failed", "Please try again later or contact me on twitter @vbsteven");
		} else {
			hideProgress();
			Utils.showMessage(this, "Sending bookmark failed", "Please try again later or contact me on twitter @vbsteven");
		}
	}
	
	
	private void showProgress() {
		if (progress != null) {
			progress.dismiss();
		}
		
		progress = ProgressDialog.show(this, "Bookmark to Desktop", "Sending " + queue.size() + " bookmarks to server");
	}
	
	private void hideProgress() {
		if (progress != null) {
			progress.dismiss();
		}
		
		progress = null;
	}

	protected ArrayList<Bookmark> getAllBookmarks() {
		ArrayList<Bookmark> result = new ArrayList<SendAllCurrentBookmarksActivity.Bookmark>();
		String[] projection = new String[]{ BookmarkColumns.TITLE, BookmarkColumns.URL};
		Cursor c = managedQuery(Browser.BOOKMARKS_URI, projection, "bookmark = ?", new String[] {"1"}, null);
		
		if (c.moveToFirst()) {
			int title = c.getColumnIndex(BookmarkColumns.TITLE);
			int url = c.getColumnIndex(BookmarkColumns.URL);
			while (!c.isAfterLast()) {
				Bookmark b = new Bookmark();
				b.title = c.getString(title);
				b.url = c.getString(url);
				result.add(b);
				
				c.moveToNext();
			}
		}
		
		return result;
	}
	
	protected class Bookmark {
		public String title;
		public String url;
	}
}
