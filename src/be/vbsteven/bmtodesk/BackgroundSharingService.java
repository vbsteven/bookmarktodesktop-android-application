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
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class BackgroundSharingService extends Service {
	private static final String URL = "http://bookmarktodesktop.appspot.com/addbookmark";
	private String title = "";
	private String url = "";
	private String responseMessage = "";
	private Handler handler;
	private NotificationManager nManager;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		
		nManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		
		handler = new Handler();
		
		if (intent.hasExtra(Global.EXTRA_TITLE) && intent.hasExtra(Global.EXTRA_URL)) {
			title = intent.getStringExtra(Global.EXTRA_TITLE);
			url = intent.getStringExtra(Global.EXTRA_URL);
			Log.d(Global.TAG, "started BackgroundSharingService with values " + title + " " + url);
		} else {
			stopSelf();
			return;
		}
		
		sendToServer(title, url);
		
	}
	
	private void sendToServer(final String title, final String url) {
		showProgress();
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				String username = Global.getUsername(BackgroundSharingService.this);
				String password = Global.getPassword(BackgroundSharingService.this);
				doPost(username, password, title, url);
			}
		});
		thread.start();
	}

	
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
			hideProgress();
			onRegistrationResult(responseMessage);
		}
	};
	
	private void onRegistrationResult(String message) {
		Log.d(Global.TAG, "message: " + message);
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
		
		stopSelf();
	}
	
	private void showProgress() {
		Notification n = new Notification(R.drawable.icon, "Sending bookmark to server...", System.currentTimeMillis());
		Intent i = new Intent(this, MainActivity.class); // contentintent is required. so redirect to mainpage
		PendingIntent contentIntent = PendingIntent.getActivity(this, 1, i, 0);
		n.setLatestEventInfo(this, "Bookmark to Desktop", "Sending bookmark to server...", contentIntent);
		nManager.notify(2, n);
	}
	
	private void hideProgress() {
		nManager.cancel(2);
		nManager.cancel(3);
	}
	
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
	
	private void showSuccessfulSend() {
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
