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

public class ShareActivity extends Activity {
	
	private static final String URL = "http://bookmarktodesktop.appspot.com/addbookmark"; 

	private String responseMessage = "";
	private Handler handler;
	private Dialog progress;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(android.R.style.Theme_Light_NoTitleBar);
		super.onCreate(savedInstanceState);
		
		if (!Global.hasUserCredentials(this)) {
			// no credentials yet
			// show setup wizard
			startActivity(new Intent(this, RegistrationActivity.class));
			finish();
		}

		handler = new Handler();
		setContentView(R.layout.share);

		// check if we are called with a share intent
		if (getIntent().getAction() != null
				&& getIntent().getAction().equals("android.intent.action.SEND")) {
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
		
		if (Global.isFastSharing(this)) {
			// if fastsharing is on don't bother asking the user for a title
			String url = ((EditText)findViewById(R.id.et_url)).getText().toString();
			sendToServer(url, url);
		}

	}

	/*
	 * sanitize the input so it does not contain newlines
	 */
	private String sanitizeValue(String value) {
		// for the moment only skyfire has this issue so we can always
		// just return the first part
		String[] parts = value.split("\n");
		return parts[0];
	}

	private void sendToServer(final String title, final String url) {
		if (Global.isBackgroundSharing(this)) {
			Log.d(Global.TAG, "backgroundSharing is enabled, starting service");
			Intent i = new Intent(this, BackgroundSharingService.class);
			i.putExtra(Global.EXTRA_TITLE, title);
			i.putExtra(Global.EXTRA_URL, url);
			startService(i);
			finish();
			return;
		}
		Log.d(Global.TAG, "backgroundsharing is not enabled");
		
		
		showProgress();
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
			Utils.showMessage(this, "Sending bookmark failed", "Please try again later or contact me on twitter: @vbsteven");
			return;
		}
		
		if (message.startsWith("INVALIDENTRY")) {
			Utils.showMessage(this, "Invalid input", "Please provide a title and a url");
			return;
		} else if (message.startsWith("INVALIDLOGIN")) {
			Utils.showMessage(this, "Authentication failed",
					"Please check your account info in preferences");
		} else if (message.startsWith("SUCCESSFUL")) {
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
			
		} else if (message.startsWith("REQUESTFAILED")) {
			Utils.showMessage(this, "Sending bookmark failed", "Please try again later or contact me on twitter @vbsteven");
		} else {
			Utils.showMessage(this, "Sending bookmark failed", "Please try again later or contact me on twitter @vbsteven");
		}
	}
	
	private void showProgress() {
		if (progress != null) {
			progress.dismiss();
		}
		
		progress = ProgressDialog.show(this, "Bookmark to Desktop", "Sending bookmark to server...");
	}
	
	private void hideProgress() {
		if (progress != null) {
			progress.dismiss();
		}
	}
}
