package com.adi.exam.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adi.exam.ExamHistory;
import com.adi.exam.R;
import com.adi.exam.SriVishwa;
import com.adi.exam.adapters.AssignmentHistoryAdapter;
import com.adi.exam.adapters.AssignmentListingAdapter;
import com.adi.exam.adapters.MaterialAdapter;
import com.adi.exam.common.AppPreferences;
import com.adi.exam.database.App_Table;
import com.adi.exam.utils.TraceUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AssignmentHistory extends ParentFragment implements View.OnClickListener {
    private OnFragmentInteractionListener mListener;
    private ProgressBar progressBar;
    private AssignmentHistoryAdapter adapterContent;
    private TextView tv_content_txt;
    private App_Table table;
    private SriVishwa activity;
    private View layout;
    private RecyclerView mRecyclerView;

    private String student_id;

    private WebView wv_content = null;

    private WebSettings webSettings = null;

    private JSONObject studentdetails;

    public AssignmentHistory() {
        // Required empty public constructor
    }

    public static AssignmentHistory newInstance() {
        return new AssignmentHistory();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        mListener = (OnFragmentInteractionListener) context;

        activity = (SriVishwa) context;

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;

    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mListener.onFragmentInteraction(activity.getString(R.string.asn_history), false);


    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        layout = inflater.inflate(R.layout.fragment_assign_history, container, false);

        /*if (getActivity().getSupportActionBar() != null) {

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            getSupportActionBar().setDisplayShowHomeEnabled(true);

        }*/

        try {

            studentdetails = new JSONObject(AppPreferences.getInstance(getActivity()).getFromStore("studentDetails"));

            student_id = studentdetails.optString("student_id");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        wv_content = (WebView)layout.findViewById(R.id.webview);
        wv_content.loadUrl("https://bsecuresoftechsolutions.com/viswa/analysis/assignment?student_id="+student_id);
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

        wv_content.setWebViewClient(new AssignmentHistory.MyWebViewClient());
        wv_content.setWebChromeClient(new AssignmentHistory.MyWebChromeClient());

        wv_content.getSettings().setJavaScriptEnabled(true);
        wv_content.getSettings().setLoadWithOverviewMode(true);
        wv_content.getSettings().setUseWideViewPort(true);

        webSettings = wv_content.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        /*table = new App_Table(getActivity());
        progressBar = layout.findViewById(R.id.pb_content_bar);

        tv_content_txt = layout.findViewById(R.id.tv_content_txt);

        tv_content_txt.setText(R.string.ntas);

        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);

        RecyclerView rv_content_list = layout.findViewById(R.id.rv_content_list);

        rv_content_list.setLayoutManager(layoutManager);

        rv_content_list.setItemAnimator(new DefaultItemAnimator());

        DividerItemDecoration did = new DividerItemDecoration(rv_content_list.getContext(), layoutManager.getOrientation());

        rv_content_list.addItemDecoration(did);

        adapterContent = new AssignmentHistoryAdapter(activity);

        adapterContent.setOnClickListener(this);

        rv_content_list.setAdapter(adapterContent);

        checkAssignment();
*/
        return layout;
    }

    private void checkAssignment() {
        try {
            Object results = table.getAssignmentHistoryList();

            if (results != null) {
                JSONObject object = new JSONObject(results.toString());
                JSONArray array = object.getJSONArray("assign_body");
                try {
                    tv_content_txt.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);

                    if (array.length() > 0) {
                        adapterContent.setItems(array);
                        adapterContent.notifyDataSetChanged();
                    }

                } catch (Exception e) {
                    TraceUtils.logException(e);
                }
            } else {
                progressBar.setVisibility(View.GONE);
                tv_content_txt.setVisibility(View.VISIBLE);

            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View view) {
        try {

            switch (view.getId()) {

                case R.id.ll_exam:
                    JSONObject jsonObject1 = adapterContent.getItems().getJSONObject((int) view.getTag());
                    activity.showAssignmentResult(jsonObject1.optString("assignment_id"),jsonObject1.optString("subject"));

                    break;

            }

        } catch (Exception e) {

            TraceUtils.logException(e);

        }
    }
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        mListener.onFragmentInteraction(activity.getString(R.string.asn_history), false);

    }

    private class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {

            // Log.e("-=-=-=-=-=-", newProgress + "");

            if (newProgress == 5)
                layout.findViewById(R.id.pb_allpg).setVisibility(View.VISIBLE);

            if (newProgress >= 95) {
                layout.findViewById(R.id.pb_allpg).setVisibility(View.GONE);
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
            layout.findViewById(R.id.pb_allpg).setVisibility(View.GONE);
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

                getActivity().finish();

                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

}
