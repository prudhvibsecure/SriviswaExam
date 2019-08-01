package com.adi.exam;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.adi.exam.common.AppPreferences;

import org.json.JSONException;
import org.json.JSONObject;

public class ExamHistory extends AppCompatActivity {

    private String student_id;

    private WebView wv_content = null;

    private WebSettings webSettings = null;

    private JSONObject studentdetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_history);

        if (getSupportActionBar() != null) {

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            getSupportActionBar().setDisplayShowHomeEnabled(true);

        }

        try {

            studentdetails = new JSONObject(AppPreferences.getInstance(this).getFromStore("studentDetails"));

            student_id = studentdetails.optString("student_id");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        wv_content = (WebView) findViewById(R.id.webview);
        wv_content.loadUrl("https://bsecuresoftechsolutions.com/viswa/analysis?student_id="+student_id);
        wv_content.getSettings().setAllowFileAccess(true);
        wv_content.getSettings().setSupportZoom(true);
        wv_content.setVerticalScrollBarEnabled(true);
        wv_content.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        wv_content.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        wv_content.getSettings().setLoadWithOverviewMode(true);
        wv_content.getSettings().setUseWideViewPort(true);
        wv_content.getSettings().setJavaScriptEnabled(true);
        wv_content.getSettings().setPluginState(WebSettings.PluginState.ON);

        wv_content.getSettings().setSaveFormData(false);
        wv_content.getSettings().setSavePassword(false);

        wv_content.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        wv_content.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        wv_content.setWebViewClient(new MyWebViewClient());
        wv_content.setWebChromeClient(new MyWebChromeClient());

        wv_content.getSettings().setJavaScriptEnabled(true);
        wv_content.getSettings().setLoadWithOverviewMode(true);
        wv_content.getSettings().setUseWideViewPort(true);

        webSettings = wv_content.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);

    }

    private class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {

            // Log.e("-=-=-=-=-=-", newProgress + "");

            if (newProgress == 5)
                findViewById(R.id.pb_allpg).setVisibility(View.VISIBLE);

            if (newProgress >= 95) {
                findViewById(R.id.pb_allpg).setVisibility(View.GONE);
            }
            super.onProgressChanged(view, newProgress);
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            return super.onJsAlert(view, url, message, result);
        }
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            findViewById(R.id.pb_allpg).setVisibility(View.GONE);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:

                finish();

                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
