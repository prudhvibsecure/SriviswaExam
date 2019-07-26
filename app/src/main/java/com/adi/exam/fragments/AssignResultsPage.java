package com.adi.exam.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.adi.exam.R;
import com.adi.exam.SriVishwa;
import com.adi.exam.callbacks.IDialogCallbacks;
import com.adi.exam.callbacks.IItemHandler;
import com.adi.exam.controls.CustomTextView;
import com.adi.exam.database.PhoneComponent;
import com.adi.exam.utils.TraceUtils;

import org.json.JSONArray;
import org.json.JSONObject;

public class AssignResultsPage extends ParentFragment implements IItemHandler, View.OnClickListener, IDialogCallbacks {


    private int student_exam_result_id = 0;

    private OnFragmentInteractionListener mFragListener;

    private JSONObject examDetails = new JSONObject();

    private SriVishwa activity;

    private TableLayout tl_results;

    private TextView tv_scoore, tv_tqval, tv_taval, tv_caval, tv_icaval;

    private CustomTextView tv_submit;

    private JSONArray allQuestions = new JSONArray();

    private int type = 0;

    public AssignResultsPage() {
        // Required empty public constructor
    }

    public static AssignResultsPage newInstance(String examDetails) {

        AssignResultsPage frag = new AssignResultsPage();

        Bundle args = new Bundle();

        args.putString("examDetails", examDetails);

        frag.setArguments(args);

        return frag;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {

            if (getArguments() != null) {

                examDetails = new JSONObject(getArguments().getString("examDetails"));

            }

        } catch (Exception e) {

            TraceUtils.logException(e);

        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View layout = inflater.inflate(R.layout.fragment_exam_resultspage, container, false);

        tl_results = layout.findViewById(R.id.tl_results);

        tv_scoore = layout.findViewById(R.id.tv_scoore);

        tv_tqval = layout.findViewById(R.id.tv_tqval);

        tv_taval = layout.findViewById(R.id.tv_taval);

        tv_caval = layout.findViewById(R.id.tv_caval);

        tv_icaval = layout.findViewById(R.id.tv_icaval);

        View view = View.inflate(activity, R.layout.row_fragment_results, null);

        tl_results.addView(view);

        allQuestions = activity.getAllQuestions();

        tv_tqval.setText(allQuestions.length() + "");

        try {

                PhoneComponent phncomp = new PhoneComponent(this, activity, 1);

                phncomp.defineWhereClause("assignment_result_id = '" + examDetails.optString("assignment_result_id") + "' AND student_id = '" + activity.getStudentDetails().optInt("student_id") + "' AND assignment_id = '" + examDetails.optString("assignment_id") + "'");

                phncomp.executeLocalDBInBackground("ASSIGNMENTRESULTS");



        } catch (Exception e) {

            TraceUtils.logException(e);

        }

        tv_submit = layout.findViewById(R.id.tv_submit);

        tv_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(activity, SriVishwa.class));
            }
        });


        return layout;

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        mFragListener = (SriVishwa) context;

        activity = (SriVishwa) context;

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        mFragListener.onFragmentInteraction(activity.getString(R.string.result), false);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mFragListener.onFragmentInteraction(activity.getString(R.string.result), false);

    }

    @Override
    public void onDetach() {
        super.onDetach();

        mFragListener = null;

    }

    @Override
    public void onProgressChange(int requestId, Long... values) {

    }

    @Override
    public void onFinish(Object results, int requestId) {

        try {

            if (results != null) {

                JSONArray jsonArray = (JSONArray) results;

                JSONObject jsonObject = jsonArray.getJSONObject(jsonArray.length() - 1);

                showExamSummary(jsonObject);

            }

        } catch (Exception e) {

            TraceUtils.logException(e);

        }

    }

    @Override
    public void onError(String errorCode, int requestId) {

    }

    private void showExamSummary(JSONObject jsonObject) {

        try {

            tv_scoore.setText(activity.getString(R.string.score, jsonObject.optString("score")));

            tv_caval.setText(jsonObject.optString("no_of_correct_answers", "0"));

            int no_of_correct_answersInt = Integer.parseInt(jsonObject.optString("no_of_correct_answers", "0"));

            int total_questions_attempted = Integer.parseInt(jsonObject.optString("total_questions_attempted", "0"));

            tv_taval.setText(jsonObject.optString("total_questions_attempted"));

            tv_icaval.setText((total_questions_attempted - no_of_correct_answersInt) + "");

            for (int i = 0; i < allQuestions.length(); i++) {

                JSONObject jsonObject1 = allQuestions.getJSONObject(i);

                View view = View.inflate(activity, R.layout.row_fragment_results, null);

                ((TextView) view.findViewById(R.id.tv_questionno)).setText(activity.getString(R.string.questionno, (i + 1) + ""));

                ((TextView) view.findViewById(R.id.tv_selectedoption)).setText(jsonObject1.optString("qanswer"));

                String qanswer = jsonObject1.optString("qanswer").trim();

                String answer = jsonObject1.optString("answer");

                if (qanswer.length() == 0) {

                    ((TextView) view.findViewById(R.id.tv_selectedoption)).setText(activity.getString(R.string.lines));

                }

                ((TextView) view.findViewById(R.id.tv_status)).setText(activity.getString(R.string.notapplicable));

                if (qanswer.trim().length() > 0) {

                    if (qanswer.toLowerCase().equalsIgnoreCase(answer.toLowerCase())) {

                        ((TextView) view.findViewById(R.id.tv_status)).setText("Correct");

                        ((TextView) view.findViewById(R.id.tv_status)).setBackground(ContextCompat.getDrawable(activity, R.drawable.template_pop_but_sel_green));

                    } else {


                        ((TextView) view.findViewById(R.id.tv_status)).setText("Wrong");

                        ((TextView) view.findViewById(R.id.tv_status)).setBackground(ContextCompat.getDrawable(activity, R.drawable.template_pop_but_sel_red));

                    }

                }

                ((TextView) view.findViewById(R.id.tv_currectopion)).setText(answer);

                tl_results.addView(view);

            }

        } catch (Exception e) {

            TraceUtils.logException(e);

        }

    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.tv_yes) {

            activity.showokPopUp(R.drawable.pop_ic_confirmation, activity.getString(R.string.confirmation), activity.getString(R.string.tyyrwbsffmcotcfs), 1, this, true);

        } else if (view.getId() == R.id.tv_no) {

            activity.onKeyDown(4, null);

        }

    }

    @Override
    public void onOK(int requestId) {

        if (requestId == 1) {

            activity.showokPopUp(R.drawable.pop_ic_success, activity.getString(R.string.message), activity.getString(R.string.tyss), 2, activity.getString(R.string.viewresults), this, false);

        } else if (requestId == 2) {


        }

    }

    @Override
    public void onCancel(int requestId) {

    }
}
