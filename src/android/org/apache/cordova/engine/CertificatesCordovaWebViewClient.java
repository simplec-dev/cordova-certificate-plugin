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
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.cordova.CordovaResourceApi;
import org.apache.cordova.CordovaResourceApi.OpenForReadResult;
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

	@Override
	public WebResourceResponse shouldInterceptRequest(WebView arg0, String url) {
		WebResourceResponse ret = super.shouldInterceptRequest(arg0, url);
		/*if (ret == null && url!=null && url.contains("cloudfront.net") && url.startsWith("https://")) {
			try {
		        Log.d(TAG, "is a cloudfront url");
				CordovaResourceApi resourceApi = getResourceApi();
				Uri origUri = Uri.parse(url);
		        Uri remappedUri = resourceApi.remapUri(origUri);
		        
                HttpURLConnection conn = (HttpURLConnection)new URL(remappedUri.toString()).openConnection();
                conn.setDoInput(true);
                String mimeType = conn.getHeaderField("Content-Type");
                if (mimeType != null) {
                    mimeType = mimeType.split(";")[0];
                }
                int length = conn.getContentLength();
                InputStream inputStream = conn.getInputStream();
                CordovaResourceApi.OpenForReadResult result = new OpenForReadResult(uri, inputStream, mimeType, length, null);
		        
		        
	            CordovaResourceApi.OpenForReadResult result = resourceApi.openForRead(remappedUri, true);
	            Log.d(TAG, "shouldInterceptRequest.  " + url + "  ret="+result);
	            return new WebResourceResponse(result.mimeType, "UTF-8", result.inputStream);
		    } catch (IOException e) {
		        if (!(e instanceof FileNotFoundException)) {
		            LOG.e(TAG, "Error occurred while loading a file (returning a 404).", e);
		        }
		        // Results in a 404.
		        return new WebResourceResponse("text/plain", "UTF-8", null);
		    }
		}*/
        Log.d(TAG, "shouldInterceptRequest.  " + url + "  ret="+ret);
		return ret;
	}

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		boolean ret = super.shouldOverrideUrlLoading(view, url);
        Log.d(TAG, "shouldOverrideUrlLoading.  " + url + "  ret="+ret);
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
