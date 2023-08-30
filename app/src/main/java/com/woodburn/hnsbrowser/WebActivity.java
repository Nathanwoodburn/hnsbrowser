package com.woodburn.hnsbrowser;

import androidx.appcompat.app.AppCompatActivity;
import androidx.webkit.ProxyConfig;
import androidx.webkit.ProxyController;

import android.net.http.SslError;
import android.os.Bundle;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.util.concurrent.Executor;

public class WebActivity extends AppCompatActivity {

    boolean sslError = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        Bundle bundle = getIntent().getExtras();
        String url = bundle.getString("url");
        // Validate
        if (url == null || url.isEmpty()) {
            // Display toast
            Toast.makeText(this, "Invalid URL!", Toast.LENGTH_SHORT).show();
            // Close activity
            finish();
        }

        // Open webview with bookmark
        WebView webView = (WebView) findViewById(R.id.webviewer);

        // Set proxy to https://proxy.hnsproxy.au
        WebSettings webSettings = webView.getSettings();
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm){

                handler.proceed("userName", "password");
            }
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                if (!sslError) {
                    Toast.makeText(WebActivity.this, "SSL error", Toast.LENGTH_SHORT).show();
                    Toast.makeText(WebActivity.this, "Make sure you have installed the SSL certificate", Toast.LENGTH_SHORT).show();
                }
                sslError = true;
                handler.proceed(); // Ignore SSL certificate errors
            }
        });
        setProxy();
        webView.loadUrl(url);

    }
    private void setProxy() {
        ProxyConfig proxyConfig = new ProxyConfig.Builder()
                .addProxyRule("https://proxy.hnsproxy.au")
                .addDirect().build();
        ProxyController.getInstance().setProxyOverride(proxyConfig, new Executor() {
            @Override
            public void execute(Runnable command) {
                //do nothing
            }
        }, new Runnable() {
            @Override
            public void run() {

            }
        });
    }
}