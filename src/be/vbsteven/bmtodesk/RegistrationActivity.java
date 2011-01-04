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
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;

public class RegistrationActivity extends Activity {

	private Button button;
	private Dialog progress;
	private String responseMessage = "";
	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(android.R.style.Theme_Light_NoTitleBar);
		super.onCreate(savedInstanceState);
		handler = new Handler();
		setContentView(R.layout.registration);

		button = (Button) findViewById(R.id.but_create);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				register();
			}
		});
		
		button = (Button)findViewById(R.id.but_login);
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				checkLogin();
			}
		});
		
		CheckBox cb = (CheckBox)findViewById(R.id.cb_password);
		cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				EditText password = (EditText)findViewById(R.id.et_password);
				if (isChecked) {
					password.setTransformationMethod(null);
				} else {
					password.setTransformationMethod(new PasswordTransformationMethod());
				}
			}
		});
	}
	
	private void checkLogin() {
		final String username = ((TextView) findViewById(R.id.et_username))
				.getText().toString();
		final String password = ((TextView) findViewById(R.id.et_password))
				.getText().toString();
		
		if (!checkInput(username, password)) {
			return;
		}
		
		showLoginProgress();
		
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				doPostForLoginCheck(username, password);
			}
		});
		thread.start();
	}

	private void register() {
		final String username = ((TextView) findViewById(R.id.et_username)).getText()
				.toString();
		final String password = ((TextView) findViewById(R.id.et_password)).getText()
				.toString();

		if (!checkInput(username, password)) {
			return;
		}
		
		showProgress();
		
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				doPost(username, password);
			}
		});
		thread.start();
	}

	private boolean checkInput(final String username, final String password) {
		if (username.equals("") || password.equals("")) {
			Utils.showMessage(this, "Validation error", "Please fill in all fields");
			return false;
		}
		
		if (username.contains(" ") || password.contains(" ")) {
			Utils.showMessage(this, "Validation error", "Username and password cannot contain spaces");
			return false;
		}
		
		if (username.length() < 5 || password.length() < 5) {
			Utils.showMessage(this, "Validation error", "Username and password have to be at least 5 characters long");
			return false;
		}
		
		return true;
	}

	public void doPost(String username, String password) {
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost post = new HttpPost(URI.create("http://bookmarktodesktop.appspot.com/createuser"));
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
					2);
			nameValuePairs.add(new BasicNameValuePair("username", username));
			nameValuePairs.add(new BasicNameValuePair("password", password));
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
	
	public void doPostForLoginCheck(String username, String password) {
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost post = new HttpPost(URI.create("http://bookmarktodesktop.appspot.com/checklogin"));
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
					2);
			nameValuePairs.add(new BasicNameValuePair("username", username));
			nameValuePairs.add(new BasicNameValuePair("password", password));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response;
			response = httpclient.execute(post);

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent(), "UTF-8"));
			responseMessage = reader.readLine();
			handler.post(afterLoginRequestRunnable);
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
	
	private Runnable afterLoginRequestRunnable = new Runnable() {

		@Override
		public void run() {
			hideProgress();
			onLoginResult(responseMessage);
		}
	};
	
	private void onLoginResult(String message) {
		Log.d(Global.TAG, "message: " + message);
		
		if (message == null) {
			Utils.showMessage(this, "Login failed", "Please try again later or contact me on twitter @vbsteven");
			return;
		}
		
		if (message.startsWith("INVALIDLOGIN")) {
			Utils.showMessage(this, "Username and password are invalid", "Please verify if they are correct or create a new account");
			return;
		}
		
		if (message.startsWith("SUCCESS")) {
			String username = ((TextView) findViewById(R.id.et_username))
					.getText().toString();
			String password = ((TextView) findViewById(R.id.et_password))
					.getText().toString();
			Global.saveUsernameAndPassword(this, username, password);
			showSuccessfulMessage(
					"Login successful!", null);
			return;
		}
	}

	private void onRegistrationResult(String message) {
		Log.d(Global.TAG, "message: " + message);
		if (message == null) {
			Utils.showMessage(this, "Account creation failed", "Please try again later or contact me on twitter @vbsteven");
			return;
		}
		
		if (message.startsWith("INVALIDINPUT")) {
			Utils.showMessage(this, "Invalid input", "Please fill in all fields");
			return;
		} else if (message.startsWith("USERNAMEUNAVAILABLE")) {
			Utils.showMessage(this, "Username not available",
					"Please choose a different username");
		} else if (message.startsWith("SUCCESSFUL")) {
			String username = ((TextView) findViewById(R.id.et_username))
					.getText().toString();
			String password = ((TextView) findViewById(R.id.et_password))
					.getText().toString();
			Global.saveUsernameAndPassword(this, username, password);
			showSuccessfulMessage("Account creation successful!", null);
		} else if (message.startsWith("REQUESTFAILED")) {
			Utils.showMessage(this, "Account creation failed", "Please try again later or contact me on twitter @vbsteven");
		} else {
			Utils.showMessage(this, "Account creation failed", "Please try again later or contact me on twitter @vbsteven");
		}
	}
	
	private void showSuccessfulMessage(String title, String message) {
		new AlertDialog.Builder(this).setTitle(title).setMessage(message)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						setResult(Activity.RESULT_OK);
						Intent intent = new Intent(RegistrationActivity.this, ConfigureAddonActivity.class);
						startActivity(intent);
						finish();
					}
				}).create().show();
	}
	
	private void showLoginProgress() {
		if (progress != null) {
			progress.dismiss();
		}
		
		progress = ProgressDialog.show(this, "Bookmark to Desktop", "Attempting login...");
	}
	
	private void showProgress() {
		if (progress != null) {
			progress.dismiss();
		}
		
		progress = ProgressDialog.show(this, "Bookmark to Desktop", "Creating account...");
	}
	
	private void hideProgress() {
		if (progress != null) {
			progress.dismiss();
		}
	}
}
