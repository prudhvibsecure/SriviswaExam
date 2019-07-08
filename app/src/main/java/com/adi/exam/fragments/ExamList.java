package com.adi.exam.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adi.exam.R;
import com.adi.exam.SriVishwa;
import com.adi.exam.adapters.ExamContentListingAdapter;
import com.adi.exam.callbacks.IItemHandler;
import com.adi.exam.database.App_Table;
import com.adi.exam.database.PhoneComponent;
import com.adi.exam.tasks.HTTPPostTask;
import com.adi.exam.utils.TraceUtils;

import org.json.JSONArray;
import org.json.JSONObject;

public class ExamList extends ParentFragment implements View.OnClickListener, IItemHandler {

    //TODO: error handling -> make use of tv_content_txt

    private OnFragmentInteractionListener mFragListener;

    private ProgressBar progressBar;

    private TextView tv_content_txt;

    private SriVishwa activity;

    private ExamContentListingAdapter adapterContent;

    private int mCount = 0;

    private int iCounter = 0;

    public ExamList() {
        // Required empty public constructor
    }

    public static ExamList newInstance() {
        return new ExamList();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View layout = inflater.inflate(R.layout.fragment_examlist, container, false);

        progressBar = layout.findViewById(R.id.pb_content_bar);

        tv_content_txt = layout.findViewById(R.id.tv_content_txt);

        tv_content_txt.setText(R.string.cydhaen);

        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);

        RecyclerView rv_content_list = layout.findViewById(R.id.rv_content_list);

        rv_content_list.setLayoutManager(layoutManager);

        rv_content_list.setItemAnimator(new DefaultItemAnimator());

        DividerItemDecoration did = new DividerItemDecoration(rv_content_list.getContext(), layoutManager.getOrientation());

        rv_content_list.addItemDecoration(did);

        adapterContent = new ExamContentListingAdapter(activity);

        adapterContent.setOnClickListener(this);

        rv_content_list.setAdapter(adapterContent);

        checkQuestionPaper();

        return layout;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        mFragListener = (OnFragmentInteractionListener) context;

        activity = (SriVishwa) context;

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        mFragListener.onFragmentInteraction(R.string.exam, false);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mFragListener.onFragmentInteraction(R.string.exam, false);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mFragListener = null;
    }

    @Override
    public void onClick(View view) {

        try {

            switch (view.getId()) {

                case R.id.tv_startexam:

                    JSONObject jsonObject1 = adapterContent.getItems().getJSONObject((int) view.getTag());

                    activity.showInstructionsScreen(jsonObject1, true);

                    break;

            }

        } catch (Exception e) {

            TraceUtils.logException(e);

        }

    }

    private void checkQuestionPaper() {

        try {

            progressBar.setVisibility(View.VISIBLE);

            tv_content_txt.setVisibility(View.GONE);

            JSONObject jsonObject = new JSONObject();

            jsonObject.put("student_id", activity.getStudentDetails().optString("student_id"));

            HTTPPostTask post = new HTTPPostTask(activity, this);

            post.disableProgress();

            post.userRequest(getString(R.string.plwait), 1, "checkqustionpaper", jsonObject.toString());


        } catch (Exception e) {

            TraceUtils.logException(e);

        }

    }

    @Override
    public void onProgressChange(int requestId, Long... values) {

    }

    @Override
    public void onFinish(Object results, int requestId) {

        try {

            if (requestId == 1) {

                JSONObject jsonObject = new JSONObject(results.toString());

                if (jsonObject.optString("statuscode").equalsIgnoreCase("200")) {

                    if (jsonObject.has("question_paper_details")) {

                        JSONArray jsonArray = jsonObject.getJSONArray("question_paper_details");

                        if (jsonArray.length() > 0) {

                            mCount = jsonArray.length();

                            for (int i = 0; i < jsonArray.length(); i++) {

                                JSONObject jsonObject1 = jsonArray.getJSONObject(i);

                                JSONObject reqObject = new JSONObject();

                                reqObject.put("student_id", activity.getStudentDetails().optString("student_id"));

                                reqObject.put("question_paper_id", jsonObject1.optString("question_paper_id"));

                                HTTPPostTask post = new HTTPPostTask(activity, this);

                                post.disableProgress();

                                post.userRequest(getString(R.string.plwait), 2, "getquestionpaper", reqObject.toString());

                            }

                            return;

                        }

                    }

                }

                progressBar.setVisibility(View.GONE);

                tv_content_txt.setVisibility(View.VISIBLE);

            } else if (requestId == 2) {

                JSONObject jsonObject = new JSONObject(results.toString());

                if (jsonObject.optString("statuscode").equalsIgnoreCase("200")) {

                    App_Table table = new App_Table(activity);

                    if (jsonObject.has("exam_details")) {

                        JSONArray exam_details = jsonObject.getJSONArray("exam_details");

                        for (int i = 0; i < exam_details.length(); i++) {

                            JSONObject jsonObject1 = exam_details.getJSONObject(i);

                            String iwhereClause = "exam_id = '" + jsonObject1.optString("exam_id") + "'";

                            table.checkNInsertARecord(jsonObject1, "EXAM", iwhereClause);

                        }

                    }

                    if (jsonObject.has("question_details")) {

                        JSONArray question_details = jsonObject.getJSONArray("question_details");

                        for (int i = 0; i < question_details.length(); i++) {

                            JSONObject jsonObject1 = question_details.getJSONObject(i);

                            String iwhereClause = "exam_id = '" + jsonObject1.optString("exam_id") + "' AND question_paper_id = '" + jsonObject1.optString("question_paper_id") + "'";

                            table.checkNInsertARecord(jsonObject1, "QUESTIONPAPER", iwhereClause);

                        }

                    }

                    if (jsonObject.has("student_question_paper_details")) {

                        JSONArray student_question_paper_details = jsonObject.getJSONArray("student_question_paper_details");

                        for (int i = 0; i < student_question_paper_details.length(); i++) {

                            JSONObject jsonObject1 = student_question_paper_details.getJSONObject(i);

                            String iwhereClause = "student_question_paper_id = '" + jsonObject1.optString("student_question_paper_id") + "' AND question_paper_id = '" + jsonObject1.optString("question_paper_id") + "'";

                            table.checkNInsertARecord(jsonObject1, "STUDENTQUESTIONPAPER", iwhereClause);

                        }

                    }

                    --mCount;

                    if (mCount == 0) {

                        progressBar.setVisibility(View.GONE);

                        PhoneComponent phncomp = new PhoneComponent(this, activity, 3);

                        phncomp.executeLocalDBInBackground("EXAM");

                    }

                    return;

                }

                progressBar.setVisibility(View.GONE);

                tv_content_txt.setVisibility(View.VISIBLE);

            } else if (requestId == 3) {

                JSONArray jsonArray = (JSONArray) results;

                if (jsonArray.length() > 0) {

                    adapterContent.setItems(jsonArray);

                    adapterContent.notifyDataSetChanged();

                    progressBar.setVisibility(View.GONE);

                    updateOtherDetails(adapterContent.getItems());

                }

            }

        } catch (Exception e) {

            TraceUtils.logException(e);

        }

    }

    @Override
    public void onError(String errorCode, int requestId) {

    }

    private void updateOtherDetails(final JSONArray jsonArray) throws Exception {

        for (iCounter = 0; iCounter < jsonArray.length(); iCounter++) {

            final JSONObject jsonObject = jsonArray.getJSONObject(iCounter);

            String exam_id = jsonObject.optString("exam_id").trim();

            if (exam_id.length() > 0) {

                PhoneComponent phncomp = new PhoneComponent(new IItemHandler() {
                    @Override
                    public void onFinish(Object results, int requestId) {

                        try {

                            if (results != null) {

                                JSONArray jsonArray1 = (JSONArray) results;

                                adapterContent.getItems().getJSONObject(requestId).put("question_details", jsonArray1.getJSONObject(0));

                                adapterContent.notifyDataSetChanged();

                            }

                        } catch (Exception e) {

                            TraceUtils.logException(e);

                        }

                    }

                    @Override
                    public void onError(String errorCode, int requestId) {

                    }

                    @Override
                    public void onProgressChange(int requestId, Long... values) {

                    }

                }, activity, iCounter);

                phncomp.defineWhereClause("exam_id = '" + exam_id + "'");

                phncomp.executeLocalDBInBackground("QUESTIONPAPER");

            }

        }

    }

   /* private void showSubCategories(JSONObject jsonObject) {

        try {

            PhoneComponent phncomp = new PhoneComponent(this, activity, 4);

            phncomp.defineWhereClause("exam_id = '" + jsonObject.optString("exam_id") + "' ");

            phncomp.executeLocalDBInBackground("QUESTIONPAPER");


        } catch (Exception e) {

            TraceUtils.logException(e);

        }

    }*/

}
