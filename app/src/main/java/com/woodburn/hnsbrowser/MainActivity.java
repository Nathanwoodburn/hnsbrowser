package com.woodburn.hnsbrowser;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.navigate_button).setOnClickListener(v -> navigate());
        ((EditText)findViewById(R.id.url_textbox)).setOnEditorActionListener((v, actionId, event) -> {
            navigate();
            return true;
        });
    }

    void navigate() {
        // Get inputted text from url text box
        String url = ((EditText)findViewById(R.id.url_textbox)).getText().toString();
        url = url.trim();
        if (url.length() < 2){
            return;
        }
        if (url.contains("http")){
            if (url.contains("http://")){ // Force https
                url = url.replace("http://", "https://");
            }
        } else {
            url = "https://" + url;
        }

        // Send url to WebActivity
        Intent intent = new Intent(this, WebActivity.class);
        intent.putExtra("url", url);
        startActivity(intent);
    }
}