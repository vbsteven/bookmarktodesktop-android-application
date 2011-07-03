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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * service that sends bookmarks to the server in the background
 * and keeps the user up to date through notifications
 *
 * @author steven
 */
public class BackgroundSharingService extends Service {

	private static final String URL = Global.getDomain() + "/addbookmark";

	private NotificationManager nManager;

	private String title = "";
	private String url = "";

	@Override
	/**
	 * not really used here
	 */
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	/**
	 * starts the service and gets ready to send bookmark
	 *
	 * this service is supposed to only get started once for every bookmark
	 * so if onStart is called it means a new bookmark needs to be sent
	 */
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);

		// init notification manager
		nManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

		if (intent.hasExtra(Global.EXTRA_TITLE) && intent.hasExtra(Global.EXTRA_URL)) {
			title = intent.getStringExtra(Global.EXTRA_TITLE);
			url = intent.getStringExtra(Global.EXTRA_URL);
			Log.d(Global.TAG, "started BackgroundSharingService with values " + title + " " + url);

			sendToServer(title, url);
		} else {
			// if we're not called with a title and url, stop the service
			stopSelf();
			return;
		}
	}

	/**
	 * sends the bookmark to the server in a POST request
	 *
	 * @param title
	 * @param url
	 */
	private void sendToServer(final String title, final String url) {
		showProgress();

		String username = Global.getUsername(BackgroundSharingService.this);
		String password = Global.getPassword(BackgroundSharingService.this);
		String responseMessage;

		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost post = new HttpPost(URI.create(URL));

			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
			nameValuePairs.add(new BasicNameValuePair("username", username));
			nameValuePairs.add(new BasicNameValuePair("password", password));
			nameValuePairs.add(new BasicNameValuePair("title", title));
			nameValuePairs.add(new BasicNameValuePair("url", url));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			HttpResponse response = httpclient.execute(post);

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent(), "UTF-8"));
			responseMessage = reader.readLine();
		} catch (Exception e) {
			responseMessage = "REQUESTFAILED";
		}

		hideProgress();
		onResult(responseMessage);
	};

	/**
	 * handles the result for the POST request
	 *
	 * @param message the message returned from the POST request
	 */
	private void onResult(String message) {

		if (message == null) {
			showFailedSend("Please try again later or contact me on twitter: @vbsteven");
			return;
		}

		if (message.startsWith("INVALIDENTRY")) {
			showFailedSend("Please provide a title and a url");
			return;
		} else if (message.startsWith("INVALIDLOGIN")) {
			showFailedSend("Authentication failed");
		} else if (message.startsWith("SUCCESSFUL")) {
			showSuccessfulSend();

		} else if (message.startsWith("REQUESTFAILED")) {
			showFailedSend("Please try again later or contact me on twitter @vbsteven");
		} else {
			showFailedSend("Please try again later or contact me on twitter @vbsteven");
		}

		// no more use for this service, stop it
		stopSelf();
	}

	/**
	 * shows the progress notification
	 */
	private void showProgress() {
		Notification n = new Notification(R.drawable.icon, "Sending bookmark to server...", System.currentTimeMillis());
		Intent i = new Intent(this, MainActivity.class); // contentintent is required. so redirect to mainpage
		PendingIntent contentIntent = PendingIntent.getActivity(this, 1, i, 0);
		n.setLatestEventInfo(this, "Bookmark to Desktop", "Sending bookmark to server...", contentIntent);
		nManager.notify(2, n);
	}

	/**
	 * hides the progress notification(s)
	 */
	private void hideProgress() {
		nManager.cancel(2);
		nManager.cancel(3);
	}

	/**
	 * shows notification when the sending failed
	 *
	 * @param message
	 */
	private void showFailedSend(String message) {
		Notification n = new Notification(R.drawable.icon, "Sending bookmark failed", System.currentTimeMillis());
		n.flags = Notification.FLAG_AUTO_CANCEL;
		Intent i = new Intent(this, ShareActivity.class);
		i.putExtra(Global.EXTRA_TITLE, title);
		i.putExtra(Global.EXTRA_URL, url);
		PendingIntent p = PendingIntent.getActivity(this, 4, i, 0);
		n.setLatestEventInfo(this, "Sending bookmark failed", message, p);
		nManager.notify(3, n);
	}

	/**
	 * shows notification when the sending succeeded
	 */
	private void showSuccessfulSend() {
		if (Global.isHideConfirmation(this)) {
			nManager.cancelAll();
		} else {
			Notification n = new Notification(R.drawable.icon, "Sending bookmark successful", System.currentTimeMillis());
			n.flags = Notification.FLAG_AUTO_CANCEL;
			Intent i = new Intent(this, AfterSuccessfulSendActivity.class);
			i.putExtra(Global.EXTRA_TITLE, title);
			i.putExtra(Global.EXTRA_URL, url);
			PendingIntent p = PendingIntent.getActivity(this, 4, i, 0);
			n.setLatestEventInfo(this, "Bookmark to Desktop", "Sending bookmark successful", p);
			nManager.notify(3, n);
		}
	}
}
