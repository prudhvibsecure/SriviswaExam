package com.adi.exam.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adi.exam.R;
import com.adi.exam.SriVishwa;
import com.adi.exam.adapters.QuestionNumberListingAdapter;
import com.adi.exam.utils.TraceUtils;

import org.json.JSONArray;
import org.json.JSONObject;

public class NEETemplates extends ParentFragment implements View.OnClickListener {

    private String examTypeId;

    private String examName;

    private OnFragmentInteractionListener mFragListener;

    private View layout;

    private SriVishwa activity;

    private RecyclerView rv_ques_nums;

    private QuestionNumberListingAdapter adapter;

    private TextView tv_questionno;

    private TextView tv_notvisitedcnt;

    private TextView tv_notansweredcnt;

    private TextView tv_answeredcnt;

    private TextView tv_mfrcnt;

    private TextView tv_amfrcnt;

    private TextView tv_option1, tv_option2, tv_option3, tv_option4;

    private ImageView iv_question, iv_questionimg;

    private int currentExamId = -1;

    private RadioGroup rg_options;

    public NEETemplates() {
        // Required empty public constructor
    }

    public static NEETemplates newInstance(String examTypeId, String examName) {

        NEETemplates fragment = new NEETemplates();

        Bundle args = new Bundle();

        args.putString("examTypeId", examTypeId);

        args.putString("examName", examName);

        fragment.setArguments(args);

        return fragment;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

            examTypeId = getArguments().getString("examTypeId");

            examName = getArguments().getString("examName");

        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        layout = inflater.inflate(R.layout.fragment_kvpyttemplates, container, false);

        rg_options = layout.findViewById(R.id.rg_options);

        iv_question = layout.findViewById(R.id.iv_question);

        iv_questionimg = layout.findViewById(R.id.iv_questionimg);

        tv_option1 = layout.findViewById(R.id.tv_option1);

        tv_option2 = layout.findViewById(R.id.tv_option2);

        tv_option3 = layout.findViewById(R.id.tv_option3);

        tv_option4 = layout.findViewById(R.id.tv_option4);

        tv_questionno = layout.findViewById(R.id.tv_questionno);

        tv_notvisitedcnt = layout.findViewById(R.id.tv_notvisitedcnt);

        tv_notansweredcnt = layout.findViewById(R.id.tv_notansweredcnt);

        tv_answeredcnt = layout.findViewById(R.id.tv_answeredcnt);

        tv_mfrcnt = layout.findViewById(R.id.tv_mfrcnt);

        tv_amfrcnt = layout.findViewById(R.id.tv_amfrcnt);

        layout.findViewById(R.id.tv_savennext).setOnClickListener(this);

        layout.findViewById(R.id.tv_clearresponse).setOnClickListener(this);

        layout.findViewById(R.id.tv_mfrn).setOnClickListener(this);

        layout.findViewById(R.id.tv_submit).setOnClickListener(this);

        rv_ques_nums = layout.findViewById(R.id.rv_ques_nums);

        rv_ques_nums.setLayoutManager(new GridLayoutManager(activity, 5));

        adapter = new QuestionNumberListingAdapter(activity);

        adapter.setItems(getQuestionNum());

        rv_ques_nums.setAdapter(adapter);

        showNextQuestion(0);

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

        mFragListener.onFragmentInteraction(examName, false);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mFragListener.onFragmentInteraction(examName, false);

    }

    @Override
    public void onClick(View v) {
//qstate = //0 = not visited, 1 = not answered, 2 = answered, 3 = marked for review, 4 = answered and marked for review
        try {

            switch (v.getId()) {

                case R.id.tv_savennext:

                    if(currentExamId != -1) {

                        int selRatioId = rg_options.getCheckedRadioButtonId();

                        if(selRatioId == -1) {

                            activity.showokPopUp(R.drawable.pop_ic_info, activity.getString(R.string.alert), activity.getString(R.string.psao));

                            return;
                        }

                        JSONObject jsonObject = adapter.getItems().getJSONObject(currentExamId);

                        jsonObject.put("qstate", 2);

                        if(selRatioId == R.id.rb_first) {
                            jsonObject.put("qanswer", 1);
                        } else if(selRatioId == R.id.rb_second) {
                            jsonObject.put("qanswer", 2);
                        } else if(selRatioId == R.id.rb_third) {
                            jsonObject.put("qanswer", 3);
                        } else if(selRatioId == R.id.rb_fourth) {
                            jsonObject.put("qanswer", 4);
                        }

                        adapter.notifyItemChanged(currentExamId);

                        rg_options.clearCheck();

                        showNextQuestion(currentExamId+1);

                    }

                    break;

                case R.id.tv_clearresponse:

                    rg_options.clearCheck();

                    break;

                case R.id.tv_mfrn:

                    if(currentExamId != -1) {

                        int selRatioId = rg_options.getCheckedRadioButtonId();

                        JSONObject jsonObject = adapter.getItems().getJSONObject(currentExamId);

                        jsonObject.put("qstate", 3);

                        if(selRatioId == R.id.rb_first) {
                            jsonObject.put("qanswer", 1);
                        } else if(selRatioId == R.id.rb_second) {
                            jsonObject.put("qanswer", 2);
                        } else if(selRatioId == R.id.rb_third) {
                            jsonObject.put("qanswer", 3);
                        } else if(selRatioId == R.id.rb_fourth) {
                            jsonObject.put("qanswer", 4);
                        }

                        adapter.notifyItemChanged(currentExamId);

                        rg_options.clearCheck();

                        showNextQuestion(currentExamId+1);

                    }

                    break;

                case R.id.tv_submit:
                    break;

            }

        } catch (Exception e) {

            TraceUtils.logException(e);

        }

    }

    private JSONArray getQuestionNum() {

        JSONArray jsonArray = new JSONArray();

        try {

            for (int i = 0; i < 60; i++) {

                JSONObject jsonObject = new JSONObject();

                jsonObject.put("qstate", 0); //0 = not visited, 1 = not answered, 2 = answered, 3 = marked review, 4 = answered and marked for review

                String resourceName = "q"+(i+1);

                int drawableResourceId = this.getResources().getIdentifier(resourceName, "drawable", activity.getPackageName());

                jsonObject.put("qid", drawableResourceId);

                jsonObject.put("qimages", "");

                int drawableOptionResourceId = this.getResources().getIdentifier(resourceName, "drawable", activity.getPackageName());

                JSONArray jsonArray1 = new JSONArray();
                //jsonArray1.put(R.drawable.opm32a);
                //jsonArray1.put(R.drawable.opm32b);
                //jsonArray1.put(R.drawable.opm32c);
                //jsonArray1.put(R.drawable.opm58a);

                jsonObject.put("qoptions", jsonArray1);

                jsonObject.put("qanswer", "");

                jsonObject.put("sno", i + 1);

                jsonArray.put(jsonObject);

            }

        } catch (Exception e) {

            TraceUtils.logException(e);

        }

        return jsonArray;

    }

    private void showNextQuestion (int position) {

        try {

            currentExamId = position;

            JSONObject jsonObject = adapter.getItems().getJSONObject(position);

            tv_questionno.setText(getString(R.string.questionno, jsonObject.optString("sno")));

            iv_question.setImageResource(jsonObject.optInt("qid"));

            JSONArray jsonArray = jsonObject.optJSONArray("qoptions");

           /* tv_option1.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.opm32a, 0);
            tv_option2.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.opm32b, 0);
            tv_option3.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.opm32c, 0);
            tv_option4.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.opm58a, 0);*/

            if(jsonObject.optInt("qanswer") == 1) {

                ((RadioButton)rg_options.findViewById(R.id.rb_first)).setChecked(true);

            } else  if(jsonObject.optInt("qanswer") == 2) {

                ((RadioButton)rg_options.findViewById(R.id.rb_second)).setChecked(true);

            } else  if(jsonObject.optInt("qanswer") == 3) {

                ((RadioButton)rg_options.findViewById(R.id.rb_third)).setChecked(true);

            } else  if(jsonObject.optInt("qanswer") == 4) {

                ((RadioButton)rg_options.findViewById(R.id.rb_fourth)).setChecked(true);

            }

        } catch (Exception e) {

            TraceUtils.logException(e);

        }

    }

}
