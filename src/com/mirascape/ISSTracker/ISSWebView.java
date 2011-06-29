package com.mirascape.ISSTracker;

import com.Rob.googlemapsC.R;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class ISSWebView extends Activity
{		
	private WebView mWebView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.web_layout);
		mWebView = (WebView) findViewById(R.id.iss_webview);
		
		WebSettings webSettings = mWebView.getSettings();
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        
        WebChromeClient client = new WebChromeClient();
        mWebView.setWebChromeClient(client);
                
		mWebView.loadUrl("https://www.mirascape.com/earthmarks/155449");
	}

}
