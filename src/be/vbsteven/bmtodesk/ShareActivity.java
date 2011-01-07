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
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * this activity will be called when the user decides to share a bookmark with this application
 * 
 * it shows two textboxes so the user can validate the given title/url and then sends them to the server
 * 
 * @author steven
 */
public class ShareActivity extends Activity {

	private static final String URL = "https://bookmarktodesktop.appspot.com/addbookmark"; 

	private String responseMessage = "";
	private Handler handler;
	private Dialog progress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(android.R.style.Theme_Light_NoTitleBar);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.share);

		handler = new Handler();

		// if the user has not configured his credentials yet, show registration wizard
		if (!Global.hasUserCredentials(this)) {
			startActivity(new Intent(this, RegistrationActivity.class));
			finish();
		}

		// check if we are called with a share intent
		if (getIntent().getAction() != null && getIntent().getAction().equals("android.intent.action.SEND")) {
			if (getIntent().hasExtra(Intent.EXTRA_TEXT)) {
				String value = getIntent().getStringExtra(Intent.EXTRA_TEXT);
				value = sanitizeValue(value);

				EditText titleText = (EditText) findViewById(R.id.et_title);
				EditText urlText = (EditText) findViewById(R.id.et_url);

				titleText.setText(value);
				urlText.setText(value);
			}
		}

		Button button = (Button) findViewById(R.id.but_send);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String title = ((EditText)findViewById(R.id.et_title)).getText().toString();
				String url = ((EditText)findViewById(R.id.et_url)).getText().toString();
				if (validateInput(title, url)) {
					sendToServer(title, url);
				}
			}

		});

		// if fastsharing is on don't bother asking the user for a title
		if (Global.isFastSharing(this)) {
			String url = ((EditText)findViewById(R.id.et_url)).getText().toString();
			sendToServer(url, url);
		}
	}

	/*
	 * sanitizes the input so it does not contain newlines
	 */
	private String sanitizeValue(String value) {
		// for the moment only skyfire has this issue so we can always
		// just return the first part
		String[] parts = value.split("\n");
		return parts[0];
	}

	/**
	 * starts sending the bookmark to the server
	 * 
	 * @param title
	 * @param url
	 */
	private void sendToServer(final String title, final String url) {

		// if the user has backgroundsharing enabled start the backgroundservice
		if (Global.isBackgroundSharing(this)) {
			Log.d(Global.TAG, "backgroundSharing is enabled, starting service");
			Intent i = new Intent(this, BackgroundSharingService.class);
			i.putExtra(Global.EXTRA_TITLE, title);
			i.putExtra(Global.EXTRA_URL, url);
			startService(i);
			finish();
			return;
		}

		showProgress();

		// run in new thread to not block UI thread with network I/O
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				String username = Global.getUsername(ShareActivity.this);
				String password = Global.getPassword(ShareActivity.this);
				doPost(username, password, title, url);
			}

		});
		thread.start();
	}

	/**
	 * validates if the input is correct
	 * 
	 * @param title
	 * @param url
	 * @return true if input is valid
	 */
	private boolean validateInput(String title, String url) {
		if (title == null || url == null) {
			Utils.showMessage(this, "Input invalid", "Please provide a title and a url");
			return false;
		} 

		if (title.equals("") || url.equals("")) {
			Utils.showMessage(this, "input invalid", "Please provide a title and a url");
			return false;
		}

		// TODO: maybe validate url on 'http://' or 'https://'

		return true;
	}

	/**
	 * sends the actual request to the server in the form of an HTTP POST
	 * 
	 * the result will be put in responseMessage
	 * 
	 * @param username
	 * @param password
	 * @param title
	 * @param url
	 */
	public void doPost(String username, String password, String title, String url) {
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost post = new HttpPost(URI.create(URL));

			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
			nameValuePairs.add(new BasicNameValuePair("username", username));
			nameValuePairs.add(new BasicNameValuePair("password", password));
			nameValuePairs.add(new BasicNameValuePair("title", title));
			nameValuePairs.add(new BasicNameValuePair("url", url));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			HttpResponse response;
			response = httpclient.execute(post);

			BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
			responseMessage = reader.readLine();
		} catch (Exception e) {
			responseMessage = "REQUESTFAILED";
		}

		handler.post(afterRequestRunnable);
	}

	/**
	 * runnable that makes sure the handling of the response is done back on the UI thread
	 */
	private Runnable afterRequestRunnable = new Runnable() {
		
		@Override
		public void run() {
			hideProgress();
			onResult(responseMessage);
		}

	};

	/**
	 * handles the result of the POST request
	 * 
	 * @param message
	 */
	private void onResult(String message) {

		if (message == null) {
			Utils.showMessage(this, "Sending bookmark failed", "Please try again later or contact me on twitter: @vbsteven");
			return;
		}

		if (message.startsWith("INVALIDENTRY")) {
			Utils.showMessage(this, "Invalid input", "Please provide a title and a url");
			return;
		}

		if (message.startsWith("INVALIDLOGIN")) {
			Utils.showMessage(this, "Authentication failed","Please check your account info in preferences");
			return;
		}

		if (message.startsWith("SUCCESSFUL")) {
			if (Global.isHideConfirmation(this)) {
				Toast t = Toast.makeText(this, "Bookmark sent to server!", Toast.LENGTH_SHORT);
				t.show();
			} else {
				String title = ((EditText)findViewById(R.id.et_title)).getText().toString();
				String url = ((EditText)findViewById(R.id.et_url)).getText().toString();

				Intent intent = new Intent(this, AfterSuccessfulSendActivity.class);
				intent.putExtra(Global.EXTRA_TITLE, title);
				intent.putExtra(Global.EXTRA_URL, url);

				startActivity(intent);
			}
			finish();
			return;
		}

		Utils.showMessage(this, "Sending bookmark failed", "Please try again later or contact me on twitter @vbsteven");
	}

	/**
	 * shows the progress dialog
	 */
	private void showProgress() {
		if (progress != null) {
			progress.dismiss();
		}

		progress = ProgressDialog.show(this, "Bookmark to Desktop", "Sending bookmark to server...");
	}
	
	/**
	 * hides the progress dialog
	 */
	private void hideProgress() {
		if (progress != null) {
			progress.dismiss();
		}
	}
}
