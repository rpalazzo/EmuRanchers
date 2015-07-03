/* Copyright Robert Palazzo 2015
 * Copying this propritary source code without express
 * written permission from the author is prohibited.
 */

package com.rpalazzo.emuranchers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN, 
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void onPlayClick(View view) {	    
	    Intent myIntent = new Intent(this, PlayActivity.class);
	    //myIntent.putExtra("key", value); //Optional parameters
	    startActivity(myIntent);
	}
	
	public void onSettingsClick(View view) {	    
	    Intent myIntent = new Intent(this, SettingsActivity.class);
	    //myIntent.putExtra("key", value); //Optional parameters
	    startActivity(myIntent);
	}

	public void onStatsClick(View view) {	    
	    //Intent myIntent = new Intent(this, StatsActivity.class);
	    //myIntent.putExtra("key", value); //Optional parameters
	    //startActivity(myIntent);
		
		Toast toast;	
		toast = Toast.makeText(this, "Stats coming soon.", Toast.LENGTH_SHORT);  
		toast.show();
		
	}
	
	public void onRulesClick(View view) {	    
	    Intent myIntent = new Intent(this, RulesActivity.class);
	    //myIntent.putExtra("key", value); //Optional parameters
	    startActivity(myIntent);
	}	

	public void onAboutClick(View view) {	    
	    Intent myIntent = new Intent(this, AboutActivity.class);
	    //myIntent.putExtra("key", value); //Optional parameters
	    startActivity(myIntent);
	}
}
