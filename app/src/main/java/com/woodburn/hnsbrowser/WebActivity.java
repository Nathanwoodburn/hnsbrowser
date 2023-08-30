package com.woodburn.hnsbrowser;

import androidx.appcompat.app.AppCompatActivity;
import androidx.webkit.ProxyConfig;
import androidx.webkit.ProxyController;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Bundle;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.util.List;
import java.util.concurrent.Executor;
import android.net.DnsResolver;
import org.xbill.DNS.*;



public class WebActivity extends AppCompatActivity {

    boolean sslError = false;
    String url = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        Bundle bundle = getIntent().getExtras();
        url = bundle.getString("url");
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
                handler.proceed(); // Ignore SSL certificate errors
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean validSSL = false;
                        try {
                            Certificate certificate = error.getCertificate().getX509Certificate();
                            byte[] publicKeyBytes = certificate.getPublicKey().getEncoded();
                            MessageDigest sha256Digest = MessageDigest.getInstance("SHA-256");
                            byte[] sha256Hash = sha256Digest.digest(publicKeyBytes);
                            // Print the hash in hexadecimal format
                            StringBuilder hexString = new StringBuilder();
                            for (byte b : sha256Hash) {
                                hexString.append(String.format("%02X", b));
                            }
                            // Get TLSA hash via DIG
                            String siteHash = hexString.toString();
                            String domain = url.replace("https://", "");
                            if (domain.contains("/")) {
                                domain = domain.substring(0, domain.indexOf("/"));
                            }
                            domain = "_443._tcp." + domain;

                            SimpleResolver resolver = new SimpleResolver("152.69.186.119");
                            Lookup lookup = new Lookup(domain, Type.TLSA);
                            lookup.setResolver(resolver);

                            Record[] records = lookup.run();
                            if (lookup.getResult() == Lookup.SUCCESSFUL) {
                                for (Record record : records) {
                                    if (record instanceof TLSARecord) {
                                        // Verify TLSA hash
                                        TLSARecord tlsaRecord = (TLSARecord) record;
                                        String tlsaHash = tlsaRecord.getCertificateAssociationData().toString().replace(" ", "");
                                        runOnUiThread(() -> {
                                            Toast.makeText(WebActivity.this, "TLSA: " + tlsaHash, Toast.LENGTH_SHORT).show();
                                            Toast.makeText(WebActivity.this, "SITE: " + siteHash, Toast.LENGTH_SHORT).show();
                                        });
                                        if (tlsaHash.equals(siteHash)) {
                                            validSSL = true;
                                        }

                                    }
                                }
                            } else
                            {
                                runOnUiThread(() -> {
                                    Toast.makeText(WebActivity.this, "TLSA Lookup Failed!", Toast.LENGTH_SHORT).show();
                                    Toast.makeText(WebActivity.this, lookup.getErrorString(), Toast.LENGTH_SHORT).show();
                                });
                            }


                        } catch (Exception e) {
                            runOnUiThread(() -> {
                                Toast.makeText(WebActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                            });
                        }
                        if (!validSSL) {
                            sslError = true;
                            runOnUiThread(() -> {

                                Toast.makeText(WebActivity.this, "SSL NOT VALID!", Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            runOnUiThread(() -> {
                                Toast.makeText(WebActivity.this, "SSL VALID!", Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                }).start();
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