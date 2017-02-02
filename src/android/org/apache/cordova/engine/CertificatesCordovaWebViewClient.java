/*

 The MIT License (MIT)

 Copyright (c) 2015 Martin Reinhardt

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
package org.apache.cordova.engine;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.cordova.CordovaResourceApi;
import org.apache.cordova.LOG;

import android.net.Uri;
import android.net.http.SslError;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

/**
 *
 * Certificates Cordova WebView Client
 *
 * author, Martin Reinhardt on 23.06.14.
 *
 * Copyright Martin Reinhardt 2014. All rights reserved.
 *
 */
public class CertificatesCordovaWebViewClient extends SystemWebViewClient {
	/**
     * Logging Tag
     */
    public static final String TAG = "CertificatesCordovaWebViewClient";

    private boolean allowUntrusted = false;
    
    private static void trustAllHosts() {
        X509TrustManager easyTrustManager = new X509TrustManager() {

            public void checkClientTrusted(
                    X509Certificate[] chain,
                    String authType) throws CertificateException {
                // Oh, I am easy!
            }

            public void checkServerTrusted(
                    X509Certificate[] chain,
                    String authType) throws CertificateException {
                // Oh, I am easy!
            }

            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

        };

        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] {easyTrustManager};

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");

            sc.init(null, trustAllCerts, new java.security.SecureRandom());

            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        } catch (Exception e) {
                e.printStackTrace();
        }
    }
    
	@Override
	public WebResourceResponse shouldInterceptRequest(WebView arg0, String url) {
		WebResourceResponse ret = super.shouldInterceptRequest(arg0, url);
		if (ret == null && url!=null && url.contains("cloudfront.net") && url.startsWith("https://")) {
			try {
			    trustAllHosts();
				CordovaResourceApi resourceApi = getResourceApi();
				Uri origUri = Uri.parse(url);
		        Uri remappedUri = resourceApi.remapUri(origUri);
		        
               /* HttpURLConnection conn = (HttpURLConnection)new URL(remappedUri.toString()).openConnection();
		        Log.d(TAG, "is a cloudfront ZZZ 1");
                conn.setDoInput(true);
		        Log.d(TAG, "is a cloudfront ZZZ 2");
                String mimeType = conn.getHeaderField("Content-Type");
		        Log.d(TAG, "is a cloudfront ZZZ 3");
                if (mimeType != null) {
                    mimeType = mimeType.split(";")[0];
                }
		        Log.d(TAG, "is a cloudfront ZZZ 4");
                int length = conn.getContentLength();
		        Log.d(TAG, "is a cloudfront ZZZ 5");
                InputStream inputStream = conn.getInputStream();
		        Log.d(TAG, "is a cloudfront ZZZ 6");
                CordovaResourceApi.OpenForReadResult result = new OpenForReadResult(remappedUri, inputStream, mimeType, length, null);

		        Log.d(TAG, "is a cloudfront ZZZ 7");*/
		        
	            CordovaResourceApi.OpenForReadResult result = resourceApi.openForRead(remappedUri, true);
	            Log.d(TAG, "shouldInterceptRequest.  " + url + "  ret="+result);
	            return new WebResourceResponse(result.mimeType, "UTF-8", result.inputStream);
		    } catch (IOException e) {
		    	e.printStackTrace();
		        if (!(e instanceof FileNotFoundException)) {
		            LOG.e(TAG, "Error occurred while loading a file (returning a 404).", e);
		        }
		        // Results in a 404.
		        return new WebResourceResponse("text/plain", "UTF-8", null);
		    }
		}
        Log.d(TAG, "shouldInterceptRequest.  " + url + "  ret="+ret);
		return ret;
	}
	
	protected CordovaResourceApi getResourceApi() {
		return parentEngine.resourceApi;
	}

    /**
     *
     * @param cordova
     */
    public CertificatesCordovaWebViewClient(SystemWebViewEngine parentEngine) {
       super(parentEngine);       
    }

    /**
     * @return true of usage of untrusted (self-signed) certificates is allowed,
     *         otherwise false
     */
    public boolean isAllowUntrusted() {
        return allowUntrusted;
    }

    /**
     *
     *
     * @param pAllowUntrusted
     *            the allowUntrusted to set
     */
    public void setAllowUntrusted(final boolean pAllowUntrusted) {
        this.allowUntrusted = pAllowUntrusted;
    }

    /**
     * @see org.apache.cordova.SystemWebViewClient#onReceivedSslError(WebView,
     *      SslErrorHandler, SslError)
     */
    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler,
            SslError error) {
        Log.d(TAG, "onReceivedSslError. Proceed? " + isAllowUntrusted());
        if (isAllowUntrusted()) {
            handler.proceed();
        } else {
            super.onReceivedSslError(view, handler, error);
        }
    }
}
