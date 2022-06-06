package com.asquare.booksbear.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toolbar;

import com.asquare.booksbear.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;

import im.delight.android.webview.AdvancedWebView;

public class LoadUrlActivity extends AppCompatActivity implements AdvancedWebView.Listener{
    private AdvancedWebView mWebView;
    ProgressBar load;
    String Url,Title;
    ImageView imageMenu;
    TextView toolbarTitle;
    boolean back = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_url);
        Url = getIntent().getStringExtra("url");
        Title = getIntent().getStringExtra("title");
        mWebView = (AdvancedWebView) findViewById(R.id.webView1);
        load = findViewById(R.id.prgLoading);
        imageMenu = findViewById(R.id.imageMenu);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        imageMenu.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_arrow_back, getTheme()));
        toolbarTitle.setText(Title);
        imageMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                back = true;
                onBackPressed();
                /*Intent intent = new Intent(LoadUrlActivity.this,MainActivity.class);
                startActivity(intent);
                finish();*/
            }
        });
        mWebView.setListener(this, this);
        mWebView.setMixedContentAllowed(false);
        mWebView.loadUrl(Url);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return false;
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        mWebView.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public void onBackPressed() {
        if (back){
            super.onBackPressed();

        }

        if( mWebView.canGoBack()){
            mWebView.goBack();
        }else{
            //Do something else. like trigger pop up. Add rate app or see more app
        }
    }



    @Override
    public void onPageStarted(String url, Bitmap favicon) {
        load.setVisibility(View.VISIBLE);

    }

    @Override
    public void onPageFinished(String url) {
        load.setVisibility(View.GONE);
    }

    @Override
    public void onPageError(int errorCode, String description, String failingUrl) {

    }

    @Override
    public void onDownloadRequested(String url, String suggestedFilename, String mimeType, long contentLength, String contentDisposition, String userAgent) {

    }

    @Override
    public void onExternalPageRequest(String url) {


    }



}