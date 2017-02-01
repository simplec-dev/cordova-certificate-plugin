/*

 The MIT License (MIT)

 Copyright (c) 2014 Martin Reinhardt

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.

 Certificate Plugin for Cordova

 */
package de.martinreinhardt.cordova.plugins;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.engine.CertificatesCordovaWebViewClient;
import org.apache.cordova.engine.SystemWebView;
import org.apache.cordova.engine.SystemWebViewEngine;
import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;

/**
 *
 * Certificate Plugin for Cordova
 *
 * author, Martin Reinhardt on 23.06.14.
 *
 * Copyright Martin Reinhardt 2014. All rights reserved.
 *
 */
public class CertificatesPlugin extends CordovaPlugin {
	/**
     * Logging Tag
     */
    private static final String LOG_TAG = "Certificates";

    /**
     * Untrusted Variable
     */
    private boolean allowUntrusted = false;

	private static void trustEveryone() { 
	    try { 
	            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier(){ 
	                    public boolean verify(String hostname, SSLSession session) { 
	                            return true; 
	                    }}); 
	            SSLContext context = SSLContext.getInstance("TLS"); 
	            context.init(null, new X509TrustManager[]{new X509TrustManager(){ 
	                    public void checkClientTrusted(X509Certificate[] chain, 
	                                    String authType) throws CertificateException {} 
	                    public void checkServerTrusted(X509Certificate[] chain, 
	                                    String authType) throws CertificateException {} 
	                    public X509Certificate[] getAcceptedIssuers() { 
	                            return new X509Certificate[0]; 
	                    }}}, new SecureRandom()); 
	            HttpsURLConnection.setDefaultSSLSocketFactory( 
	                            context.getSocketFactory()); 
	            
	    } catch (Exception e) { // should never happen 
	            e.printStackTrace(); 
	    } 
	} 
	
    @Override
	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);
		
		trustEveryone();

		setUntrusted(true);
	}

    /**
     * Executes the request.
     *
     * This method is called from the WebView thread. To do a non-trivial amount
     * of work, use: cordova.getThreadPool().execute(runnable);
     *
     * To run on the UI thread, use:
     * cordova.getActivity().runOnUiThread(runnable);
     *
     * @param action
     *            The action to execute. (Currently "setUntrusted only")
     * @param args
     *            The exec() arguments.
     * @param callbackContext
     *            The callback context used when calling back into JavaScript.
     * @return Whether the action was valid.
     *
     *
     */
    @Override
    public boolean execute(String action, JSONArray args,
            CallbackContext callbackContext) throws JSONException {

        if (action.equals("setUntrusted")) {
              try {
            	  allowUntrusted = args.getBoolean(0);
                cordova.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                  	  setUntrusted(allowUntrusted);
                    }
                });
                callbackContext.success();
                return true;
              } catch(Exception e){
                Log.e(LOG_TAG, "Got unkown error during passing to UI Thread", e);
              }
        }
        callbackContext.error("Invalid Command");
        return false;
    }
    
    public void setUntrusted(boolean untrusted) {
    	allowUntrusted = untrusted;
        Log.d(LOG_TAG, "Setting allowUntrusted to " + allowUntrusted);
        try {
            SystemWebView view = (SystemWebView)webView.getView();
            CertificatesCordovaWebViewClient cWebClient =
                new CertificatesCordovaWebViewClient((SystemWebViewEngine)webView.getEngine());

            cWebClient.setAllowUntrusted(allowUntrusted);
            webView.clearCache();
            view.setWebViewClient(cWebClient);
          } catch(Exception e){
            Log.e(LOG_TAG, "Got unkown error during setting webview in activity", e);
          }
    }
}
