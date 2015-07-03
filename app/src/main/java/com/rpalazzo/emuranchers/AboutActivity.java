/* Copyright Robert Palazzo 2015
 * Copying this propritary source code without express
 * written permission from the author is prohibited.
 */

package com.rpalazzo.emuranchers;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class AboutActivity extends Activity {

	 WebView webView;	 
	 String mimeType = "text/html";
	 String encoding = "utf-8";

	 //TODO: attribution should be like the line below
	 //This work, "90fied", is a derivative of "Creative Commons 10th Birthday Celebration San Francisco" by tvol, used under CC BY. "90fied" is licensed under CC BY by [Your name here].
	 
	 /*
	  * unmodified: Decktet design, Emu Ranchers rules, card graphics and font 
	  * Modified: Decktet card design to have suits along the top edge, Emu Ranchers logo image modified to remove text.
	  */
	 String htmlText = "<html> <strong>About Emu Ranchers</strong> " +
	 "<p>Decktet design, decktet card images, decktet card fonts, and Emu Ranchers rules and logo are copyright P.D. Mangus." +
	 "Licensed under under  <a href=\"http://creativecommons.org/licenses/by-nc-sa/3.0/\">Creative Commons Attribution-NonCommercial-ShareAlike 3.0 License</a>.</p>" +
	 "<p>Background image courtesy of Luigi Diamanti at <a href=\"http://www.freedigitalphotos.net/\">FreeDigitalPhotos.net</a></p>" +
	 "<p>Software Copyright R. Palazzo, 2014. All rights reserved.</p>";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		
		webView = (WebView)findViewById(R.id.webview);
		webView.loadData(htmlText, mimeType, encoding);
		webView.getSettings().setBuiltInZoomControls(true);
		//webView.getSettings().setDisplayZoomControls(false);
		
		webView.setWebViewClient(new WebViewClient(){
			    public boolean shouldOverrideUrlLoading(WebView view, String url) {
			        if (url != null && url.startsWith("http://")) {
		            view.getContext().startActivity(
		                new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
		            return true;
		        } else {
		            return false;
		        }
		    }
		});
	}
}
