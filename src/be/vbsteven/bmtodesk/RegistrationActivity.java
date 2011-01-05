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
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;

/**
 * registration activity is presented when the user opens the app for the first time
 * 
 * given credentials can be used to create a new account or to login with an existing one
 * 
 * @author steven
 */
public class RegistrationActivity extends Activity {

	private Handler handler;

	private Button button;
	private Dialog progress;

	private String responseMessage = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(android.R.style.Theme_Light_NoTitleBar);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.registration);

		handler = new Handler();

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

	/**
	 * checks if the given account is valid on the server
	 */
	private void checkLogin() {
		final String username = ((TextView) findViewById(R.id.et_username)).getText().toString();
		final String password = ((TextView) findViewById(R.id.et_password)).getText().toString();
		
		if (!checkInput(username, password)) {
			return;
		}

		showLoginProgress();

		// run in a separate thread to not block the UI thread with network I/O
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				doLoginPost(username, password);
			}

		});
		thread.start();
	}

	/**
	 * registers the given account on the server
	 */
	private void register() {
		final String username = ((TextView) findViewById(R.id.et_username)).getText().toString();
		final String password = ((TextView) findViewById(R.id.et_password)).getText().toString();

		if (!checkInput(username, password)) {
			return;
		}

		showRegisterProgress();

		// run in a separate thread to not block the UI thread with network I/O
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				doRegisterPost(username, password);
			}

		});
		thread.start();
	}

	/**
	 * validates if the given username and password are according to policy
	 * 
	 * @param username
	 * @param password
	 * @return true if the given credentials are valid for use
	 */
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

	/**
	 * sends a POST request to the server for registration
	 * 
	 * the result will be put in responseMessage
	 * 
	 * @param username
	 * @param password
	 */
	public void doRegisterPost(String username, String password) {
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost post = new HttpPost(URI.create("http://bookmarktodesktop.appspot.com/createuser"));
			
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("username", username));
			nameValuePairs.add(new BasicNameValuePair("password", password));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			
			HttpResponse response = httpclient.execute(post);

			BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
			responseMessage = reader.readLine();
			handler.post(afterRegisterRequestRunnable);
		} catch (Exception e) {
			responseMessage = "REQUESTFAILED";
			handler.post(afterRegisterRequestRunnable);
		}
	}

	/**
	 * sends a POST request to the server for login
	 * 
	 * the result will be put in responseMessage
	 * 
	 * @param username
	 * @param password
	 */
	public void doLoginPost(String username, String password) {
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost post = new HttpPost(URI.create("http://bookmarktodesktop.appspot.com/checklogin"));

			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("username", username));
			nameValuePairs.add(new BasicNameValuePair("password", password));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			HttpResponse response = httpclient.execute(post);

			BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
			responseMessage = reader.readLine();
			handler.post(afterLoginRequestRunnable);
		} catch (Exception e) {
			responseMessage = "REQUESTFAILED";
			handler.post(afterRegisterRequestRunnable);
		}
	}

	/**
	 * runnable to execute on the UI thread once the registration POST has finished
	 */
	private Runnable afterRegisterRequestRunnable = new Runnable() {

		@Override
		public void run() {
			hideProgress();
			onRegistrationResult(responseMessage);
		}

	};

	/**
	 * runnable to execute on the UI thread once the login POST has finished
	 */
	private Runnable afterLoginRequestRunnable = new Runnable() {

		@Override
		public void run() {
			hideProgress();
			onLoginResult(responseMessage);
		}

	};

	/**
	 * handles the result form the login POST request
	 * 
	 * @param message
	 */
	private void onLoginResult(String message) {

		if (message == null) {
			Utils.showMessage(this, "Login failed", "Please try again later or contact me on twitter @vbsteven");
			return;
		}

		if (message.startsWith("INVALIDLOGIN")) {
			Utils.showMessage(this, "Username and password are invalid", "Please verify if they are correct or create a new account");
			return;
		}

		if (message.startsWith("SUCCESS")) {
			String username = ((TextView) findViewById(R.id.et_username)).getText().toString();
			String password = ((TextView) findViewById(R.id.et_password)).getText().toString();

			Global.saveUsernameAndPassword(this, username, password);

			showSuccessfulMessage("Login successful!", null);
			return;
		}
	}

	/**
	 * handles the result from the registration POST request
	 * 
	 * @param message
	 */
	private void onRegistrationResult(String message) {

		if (message == null) {
			Utils.showMessage(this, "Account creation failed", "Please try again later or contact me on twitter @vbsteven");
			return;
		}

		if (message.startsWith("INVALIDINPUT")) {
			Utils.showMessage(this, "Invalid input", "Please fill in all fields");
			return;
		}

		if (message.startsWith("USERNAMEUNAVAILABLE")) {
			Utils.showMessage(this, "Username not available","Please choose a different username");
			return;
		}

		if (message.startsWith("SUCCESSFUL")) {
			String username = ((TextView) findViewById(R.id.et_username)).getText().toString();
			String password = ((TextView) findViewById(R.id.et_password)).getText().toString();

			Global.saveUsernameAndPassword(this, username, password);

			showSuccessfulMessage("Account creation successful!", null);
			return;
		}

		Utils.showMessage(this, "Account creation failed", "Please try again later or contact me on twitter @vbsteven");
	}

	/**
	 * shows a dialog with the given title, message and opens the next step of the wizard
	 * afther the dialog is dismissed
	 * 
	 * @param title
	 * @param message
	 */
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

	/**
	 * shows progress dialog for login
	 */
	private void showLoginProgress() {
		if (progress != null) {
			progress.dismiss();
		}
		
		progress = ProgressDialog.show(this, "Bookmark to Desktop", "Attempting login...");
	}

	/**
	 * shows progress dialog for registration
	 */
	private void showRegisterProgress() {
		if (progress != null) {
			progress.dismiss();
		}
		
		progress = ProgressDialog.show(this, "Bookmark to Desktop", "Creating account...");
	}

	/**
	 * hides the shown dialog (if any)
	 */
	private void hideProgress() {
		if (progress != null) {
			progress.dismiss();
		}
	}
}
