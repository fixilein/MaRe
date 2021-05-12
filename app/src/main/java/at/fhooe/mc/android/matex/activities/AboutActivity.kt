package at.fhooe.mc.android.matex.activities;

import android.os.Bundle;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

import at.fhooe.mc.android.matex.R;


public class AboutActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        WebView wv = findViewById(R.id.activity_about_web_view);
        wv.loadUrl("file:///android_asset/licenses.html");
    }

}
