package be.vbsteven.bmtodesk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ConfigureAddonActivity extends Activity {
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(android.R.style.Theme_Light_NoTitleBar);
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.configaddons);
		
		Button button = (Button)findViewById(R.id.but_finish);
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(ConfigureAddonActivity.this, MainActivity.class));
				finish();
			}
		});
	}
}
