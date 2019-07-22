package com.adi.exam.fragments;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adi.exam.R;
import com.adi.exam.SriVishwa;
import com.adi.exam.adapters.QuestionNumberListingAdapter;
import com.adi.exam.callbacks.IFileUploadCallback;
import com.adi.exam.callbacks.IItemHandler;
import com.adi.exam.common.AppPreferences;
import com.adi.exam.common.AppSettings;
import com.adi.exam.database.App_Table;
import com.adi.exam.database.Database;
import com.adi.exam.database.PhoneComponent;
import com.adi.exam.tasks.FileUploader;
import com.adi.exam.tasks.HTTPPostTask;
import com.adi.exam.utils.TraceUtils;
import com.google.android.material.tabs.TabLayout;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class Assignment extends ParentFragment implements View.OnClickListener, IItemHandler, IFileUploadCallback {

    private OnFragmentInteractionListener mFragListener;

    private View layout;

    private SriVishwa activity;

    private RecyclerView rv_ques_nums;

    private QuestionNumberListingAdapter adapter;

    private TextView tv_questionno;

    private TextView tv_notvisitedcnt;

    private TextView tv_notansweredcnt;

    private TextView tv_answeredcnt;

    private ImageView iv_option1, iv_option2, iv_option3, iv_option4;

    private ImageView iv_question, iv_questionimg;

    private int currentExamId = -1;

    private RadioGroup rg_options;

    private JSONObject data = new JSONObject();
    private JSONArray array=new JSONArray();
    private ImageLoader imageLoader;

    private TextView tv_timer;
    private JSONObject json;

    private FileOutputStream fos = null;

    private static final String FILE_NAME = System.currentTimeMillis()+"_assign_Result.txt";

    public Assignment() {
        // Required empty public constructor
    }

    public static Assignment newInstance(String data) {

        Assignment fragment = new Assignment();

        Bundle args = new Bundle();

        args.putString("data", data);

        fragment.setArguments(args);

        return fragment;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

            try {

                data = new JSONObject(getArguments().getString("data"));

            } catch (Exception e) {

                TraceUtils.logException(e);

            }

        }

        imageLoader = ImageLoader.getInstance();

        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        layout = inflater.inflate(R.layout.fragment_assignment, container, false);

        tv_timer = layout.findViewById(R.id.tv_timer);

        rg_options = layout.findViewById(R.id.rg_options);

        iv_question = layout.findViewById(R.id.iv_question);

        iv_questionimg = layout.findViewById(R.id.iv_questionimg);

        iv_option1 = layout.findViewById(R.id.iv_option1);

        iv_option2 = layout.findViewById(R.id.iv_option2);

        iv_option3 = layout.findViewById(R.id.iv_option3);

        iv_option4 = layout.findViewById(R.id.iv_option4);

        tv_questionno = layout.findViewById(R.id.tv_questionno);

        tv_notvisitedcnt = layout.findViewById(R.id.tv_notvisitedcnt);

        tv_notansweredcnt = layout.findViewById(R.id.tv_notansweredcnt);

        tv_answeredcnt = layout.findViewById(R.id.tv_answeredcnt);

        layout.findViewById(R.id.tv_savennext).setOnClickListener(this);

        layout.findViewById(R.id.tv_clearresponse).setOnClickListener(this);

        layout.findViewById(R.id.tv_back).setOnClickListener(this);

        layout.findViewById(R.id.tv_submit).setOnClickListener(this);

        layout.findViewById(R.id.tv_next).setOnClickListener(this);

        TabLayout tl_subjects = layout.findViewById(R.id.tl_subjects);

        tl_subjects.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                int tabPosition = tab.getPosition();

                if (tabPosition == 0) {

                    showNextQuestion(0);

                    return;

                }

                String noOfQuestions = data.optString("no_of_questions");

                if (noOfQuestions.contains(",")) {

                    String temp1[] = noOfQuestions.split(",");

                    int questionIndex = 0;

                    for (int i = 0; i < tabPosition; i++) {

                        questionIndex = questionIndex + Integer.parseInt(temp1[i]);

                    }

                    //questionIndex = questionIndex+1;

                    showNextQuestion(questionIndex);

                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }

        });

        adapter = new QuestionNumberListingAdapter(activity);

        adapter.setOnClickListener(this);

        rv_ques_nums = layout.findViewById(R.id.rv_ques_nums);

        rv_ques_nums.setLayoutManager(new GridLayoutManager(activity, 5));

        rv_ques_nums.setAdapter(adapter);

        try {

            String questions = data.optString("questions");

            /*PhoneComponent phncomp = new PhoneComponent(this, activity, 1);

            phncomp.defineWhereClause("question_paper_id = '" + questions + "'");

            phncomp.executeLocalDBInBackground("STUDENTQUESTIONPAPER");*/

            getQuestionsFromDBNShow(questions);

        } catch (Exception e) {

            TraceUtils.logException(e);

        }

        String subjectsArray[];

        String subjects = data.optString("subject").trim();

        if (subjects.contains(",")) {

            subjectsArray = subjects.split(",");

        } else {

            subjectsArray = new String[]{subjects};

        }

        for (int i = 0; i < subjectsArray.length; i++) {

            String subject = subjectsArray[i];

            TextView textView = (TextView) View.inflate(activity, R.layout.tab_subjects, null);

            textView.setText(subject);

            tl_subjects.addTab(tl_subjects.newTab().setCustomView(textView));

        }

      //  showTimer((data.optInt("duration") * 60 * 60 * 1000));
        showTimer((data.optInt("duration_sec") * 1000));
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

        mFragListener.onFragmentInteraction(data.optString("exam_name"), false);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mFragListener.onFragmentInteraction(data.optString("exam_name"), false);

    }

    @Override
    public void onClick(View v) {
        //qstate = //0 = not visited, 1 = not answered, 2 = answered, 3 = marked for review, 4 = answered and marked for review
        try {

            switch (v.getId()) {

                case R.id.ll_questionno:

                    int position = rv_ques_nums.getChildAdapterPosition(v);

                    showNextQuestion(position);

                    break;

                case R.id.tv_savennext:

                    if (currentExamId != -1) {

                        int selRatioId = rg_options.getCheckedRadioButtonId();

                        if (selRatioId == -1) {

                            activity.showokPopUp(R.drawable.pop_ic_info, activity.getString(R.string.alert), activity.getString(R.string.psao));

                            return;
                        }

                        JSONObject jsonObject = adapter.getItems().getJSONObject(currentExamId);

                        jsonObject.put("qstate", 2);

                        jsonObject.put("qanswer", layout.findViewById(selRatioId).getTag());

                        /*if (selRatioId == R.id.rb_first) {
                            jsonObject.put("qanswer", "a");
                        } else if (selRatioId == R.id.rb_second) {
                            jsonObject.put("qanswer", "b");
                        } else if (selRatioId == R.id.rb_third) {
                            jsonObject.put("qanswer", "c");
                        } else if (selRatioId == R.id.rb_fourth) {
                            jsonObject.put("qanswer", "d");
                        }*/

                        adapter.notifyItemChanged(currentExamId);

                        rg_options.clearCheck();

                        showNextQuestion(currentExamId + 1);

                    }

                    break;

                case R.id.tv_clearresponse:

                    rg_options.clearCheck();

                    break;

                case R.id.tv_back:

                    /*if (currentExamId == -1)
                        return;*/

                    if (currentExamId == 0)
                        return;

                    showNextQuestion(currentExamId - 1);

                    break;

                case R.id.tv_submit:

                    showResults();

                    break;

                case R.id.tv_next:

                    if (currentExamId == adapter.getCount())
                        return;

                    JSONObject jsonObject = adapter.getItems().getJSONObject(currentExamId);

                    if (jsonObject.optString("qstate").equalsIgnoreCase("0")) {

                        jsonObject.put("qstate", 1);

                    }

                    adapter.notifyItemChanged(currentExamId);

                    showNextQuestion(currentExamId + 1);

                    break;

            }

        } catch (Exception e) {

            TraceUtils.logException(e);

        }

    }

    private void showNextQuestion(int position) {

        try {

            currentExamId = position;

            JSONArray jsonArray = adapter.getItems();

            JSONObject jsonObject = jsonArray.getJSONObject(position);

            adapter.setSelectedPosition(position);

            adapter.notifyDataSetChanged();

            tv_questionno.setText(getString(R.string.questionno, jsonObject.optString("sno")));

            iv_question.setImageResource(jsonObject.optInt("qid"));

            /*Picasso picasso = new Picasso.Builder(getActivity()).listener(new Picasso.Listener() {
                @Override
                public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {

                }
            }).build();
            picasso.load("file:///android_asset/allimages/"+jsonObject.optString("question_name"))
                    .into(iv_question, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {

                        }
                    }); picasso.load("file:///android_asset/allimages/"+jsonObject.optString("option_a"))
                    .into(iv_option1, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {

                        }
                    }); picasso.load("file:///android_asset/allimages/"+jsonObject.optString("option_b"))
                    .into(iv_option2, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {

                        }
                    }); picasso.load("file:///android_asset/allimages/"+jsonObject.optString("option_c"))
                    .into(iv_option3, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {

                        }
                    }); picasso.load("file:///android_asset/allimages/"+jsonObject.optString("option_d"))
                    .into(iv_option4, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {

                        }
                    });
*/
            imageLoader.displayImage("file://" + Environment.getExternalStorageDirectory() + "/allimages/" + jsonObject.optString("question_name"), iv_question);

            imageLoader.displayImage("file://" + Environment.getExternalStorageDirectory() + "/allimages/" + jsonObject.optString("option_a"), iv_option1);

            imageLoader.displayImage("file://" + Environment.getExternalStorageDirectory() + "/allimages/" + jsonObject.optString("option_b"), iv_option2);

            imageLoader.displayImage("file://" + Environment.getExternalStorageDirectory() + "/allimages/" + jsonObject.optString("option_c"), iv_option3);

            imageLoader.displayImage("file://" + Environment.getExternalStorageDirectory() + "/allimages/" + jsonObject.optString("option_d"), iv_option4);

            if (jsonObject.optString("qanswer").equalsIgnoreCase("a")) {

                ((RadioButton) rg_options.findViewById(R.id.rb_first)).setChecked(true);

            } else if (jsonObject.optString("qanswer").equalsIgnoreCase("b")) {

                ((RadioButton) rg_options.findViewById(R.id.rb_second)).setChecked(true);

            } else if (jsonObject.optString("qanswer").equalsIgnoreCase("c")) {

                ((RadioButton) rg_options.findViewById(R.id.rb_third)).setChecked(true);

            } else if (jsonObject.optString("qanswer").equalsIgnoreCase("d")) {

                ((RadioButton) rg_options.findViewById(R.id.rb_fourth)).setChecked(true);

            }

            /*if (jsonObject.optInt("qanswer") == 1) {

                ((RadioButton) rg_options.findViewById(R.id.rb_first)).setChecked(true);

            } else if (jsonObject.optInt("qanswer") == 2) {

                ((RadioButton) rg_options.findViewById(R.id.rb_second)).setChecked(true);

            } else if (jsonObject.optInt("qanswer") == 3) {

                ((RadioButton) rg_options.findViewById(R.id.rb_third)).setChecked(true);

            } else if (jsonObject.optInt("qanswer") == 4) {

                ((RadioButton) rg_options.findViewById(R.id.rb_fourth)).setChecked(true);

            }*/

            int notvisited = 0;

            int notanswered = 0;

            int answered = 0;

            int mfr = 0;

            int amfr = 0;

            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject jsonObject1 = jsonArray.getJSONObject(i);

                if (jsonObject1.optString("qstate").equalsIgnoreCase("0")) {

                    ++notvisited;

                } else if (jsonObject1.optString("qstate").equalsIgnoreCase("1")) {

                    ++notanswered;

                } else if (jsonObject1.optString("qstate").equalsIgnoreCase("2")) {

                    ++answered;

                } else if (jsonObject1.optString("qstate").equalsIgnoreCase("3")) {

                    ++mfr;

                } else if (jsonObject1.optString("qstate").equalsIgnoreCase("4")) {

                    ++amfr;

                }


            }

            //0 = not visited, 1 = not answered, 2 = answered, 3 = marked review, 4 = answered and marked for review,

            tv_notvisitedcnt.setText(notvisited + "");

            tv_notansweredcnt.setText(notanswered + "");

            tv_answeredcnt.setText(answered + "");

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

                JSONArray jsonArray = (JSONArray) results;

                if (jsonArray.length() > 0) {

                    //String subjectName = ((TextView) tl_subjects.getTabAt(0).getCustomView()).getText().toString();

                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject student_question_paper_details = jsonArray.getJSONObject(i);

                        String questions = student_question_paper_details.optString("questions");

                        getQuestionsFromDBNShow(questions);

                        /*if (student_question_paper_details.optString("subject").equalsIgnoreCase(subjectName)) {

                            String questions = student_question_paper_details.optString("questions");

                            getQuestionsFromDBNShow(questions);

                            break;

                        }*/

                    }

                }

            } else if (requestId == 2) {

                JSONArray jsonArray = (JSONArray) results;

                if (jsonArray.length() > 0) {

                    if (adapter.getItems().length() == 0) {

                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONObject jsonObject = jsonArray.getJSONObject(i);

                            jsonObject.put("qstate", 0); //0 = not visited, 1 = not answered, 2 = answered, 3 = marked review, 4 = answered and marked for review, 5 = visited

                            jsonObject.put("qanswer", "");

                            jsonObject.put("sno", i + 1);

                            if (i == 0) {

                                jsonObject.put("qstate", 1);

                            }

                        }

                        adapter.setItems(jsonArray);

                    } else {

                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONObject jsonObject = jsonArray.getJSONObject(i);

                            jsonObject.put("qstate", 0); //0 = not visited, 1 = not answered, 2 = answered, 3 = marked review, 4 = answered and marked for review, 5 = visited

                            jsonObject.put("qanswer", "");

                            jsonObject.put("sno", adapter.getCount() + 1);

                            adapter.getItems().put(jsonArray.getJSONObject(i));

                        }

                    }

                    adapter.notifyDataSetChanged();

                    showNextQuestion(0);

                }

            }

        } catch (Exception e) {

            TraceUtils.logException(e);

        }

    }

    @Override
    public void onError(String errorCode, int requestId) {

    }

    private void getQuestionsFromDBNShow(String questions) {

        try {

            PhoneComponent phncomp = new PhoneComponent(this, activity, 2);

            String whereQuestions = "";

            if (questions.contains(",")) {

                String[] temp = questions.split(",");

                for (int i = 0; i < temp.length; i++) {

                    whereQuestions = whereQuestions + "'" + temp[i] + "',";

                }

                whereQuestions = whereQuestions.trim();

                whereQuestions = whereQuestions.substring(0, whereQuestions.length() - 1);

            }

            phncomp.defineWhereClause("question_id IN (" + whereQuestions + ")");

            phncomp.executeLocalDBInBackground("QUESTIONS");

        } catch (Exception e) {

            TraceUtils.logException(e);

        }

    }

    private void showResults() {

        try {

            int total_questions_attempted = 0;

            int no_of_correct_answers = 0;

            double marks_per_question = data.optDouble("marks_per_question");

            double negative_marks = 0;

            if (data.has("negative_marks"))
                negative_marks = data.optDouble("negative_marks");

            double score = 0;

            int total_not_answered = 0;

            int total_marked_for_review = 0;

            int total_not_visited = 0;

            int total_answered_and_marked_for_review = 0;

            JSONArray jsonArray = adapter.getItems();

            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject jsonObject = jsonArray.getJSONObject(i);

                //qstate = //0 = not visited, 1 = not answered, 2 = answered, 3 = marked for review, 4 = answered and marked for review
                if (jsonObject.optString("qstate").equalsIgnoreCase("3")) {

                    ++total_marked_for_review;

                }

                if (jsonObject.optString("qstate").equalsIgnoreCase("0")) {

                    ++total_not_visited;

                }

                if (jsonObject.optString("qstate").equalsIgnoreCase("4")) {

                    ++total_answered_and_marked_for_review;

                }

                if (jsonObject.optString("qstate").equalsIgnoreCase("1")) {

                    ++total_not_answered;

                }

                String qanswer = jsonObject.optString("qanswer");

                String answer = jsonObject.optString("answer");

                if (qanswer.trim().length() > 0) {

                    ++total_questions_attempted;

                    if (qanswer.toLowerCase().equalsIgnoreCase(answer.toLowerCase())) {

                        ++no_of_correct_answers;

                        score = score + marks_per_question;

                    } else {

                        score = score - negative_marks;

                    }

                }

            }

            data.put("total_not_answered", total_not_answered);

            data.put("total_marked_for_review", total_marked_for_review);

            data.put("total_not_visited", total_not_visited);

            data.put("total_answered_and_marked_for_review", total_answered_and_marked_for_review);

            JSONObject ASSIGNMENTRESULTS = new JSONObject();
            JSONObject backup_result = new JSONObject();
            int assignment_result_id = AppPreferences.getInstance(activity).getIntegerFromStore("assignment_result_id");

            AppPreferences.getInstance(activity).addIntegerToStore("student_exam_result_id", ++assignment_result_id, false);

            ASSIGNMENTRESULTS.put("assignment_result_id", assignment_result_id);
            ASSIGNMENTRESULTS.put("assignment_id", data.optInt("assignment_id"));
            ASSIGNMENTRESULTS.put("student_id", activity.getStudentDetails().optInt("student_id"));
            ASSIGNMENTRESULTS.put("total_questions", adapter.getCount() + "");
            ASSIGNMENTRESULTS.put("total_questions_attempted", total_questions_attempted + "");
            ASSIGNMENTRESULTS.put("no_of_correct_answers", no_of_correct_answers + "");
            ASSIGNMENTRESULTS.put("score", score + "");



            App_Table table = new App_Table(activity);
            json=table.getAssignmentResult(data.optInt("assignment_id"),activity.getStudentDetails().optInt("student_id"));
            json.put("assignment_result_id", assignment_result_id);
            json.put("assignment_id", data.optInt("assignment_id"));
            json.put("student_id", activity.getStudentDetails().optInt("student_id"));
            json.put("total_questions", adapter.getCount() + "");
            json.put("total_questions_attempted", total_questions_attempted + "");
            json.put("no_of_correct_answers", no_of_correct_answers + "");
            json.put("score", score + "");
//            array.put(json);
//            backup_result.put("student_question_time",array);
            try {
                fos = getActivity().openFileOutput(FILE_NAME, MODE_PRIVATE);
                fos.write(backup_result.toString().getBytes());
            }catch (Exception e){
                TraceUtils.logException(e);
            }
            String path = getActivity().getFilesDir().getAbsolutePath() + "/" + FILE_NAME;
            long val = table.insertSingleRecords(ASSIGNMENTRESULTS, "ASSIGNMENTRESULTS");

            if (val > 0) {

                activity.setAllQuestions(jsonArray);

                activity.showExamSubmitConfirmationPage(data, assignment_result_id, 2);
                startUploadBackUp(path,FILE_NAME);
                return;

            }

            activity.showokPopUp(R.drawable.pop_ic_failed, activity.getString(R.string.errorTxt), activity.getString(R.string.isr));

        } catch (Exception e) {

            TraceUtils.logException(e);

        }

    }
    private void startUploadBackUp(String path,String file_name) {
        String url= AppSettings.getInstance().getPropertyValue("uploadfile_admin");
        FileUploader uploader = new FileUploader(getActivity(), this);
        uploader.setFileName(file_name, file_name);
        uploader.userRequest("", 11, url, path);
    }
    private void showTimer(long millisInFuture) {

        new CountDownTimer(millisInFuture, 1000) {

            public void onTick(long millisUntilFinished) {

                //tv_timer.setText(activity.getString(R.string.time, millisUntilFinished / 1000));
                tv_timer.setText(activity.getString(R.string.time, convertSecondsToHMmSs((millisUntilFinished / 1000))));

            }

            public void onFinish() {
                tv_timer.setText("00:00:00");
            }
        }.start();

    }

    private String convertSecondsToHMmSs(long seconds) {
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60)) % 24;
        return String.format(Locale.ENGLISH, "%d:%02d:%02d", h, m, s);
    }


    @Override
    public void onStateChange(int what, int arg1, int arg2, Object obj, int reqID) {
        try {

            switch (what) {

                case -1: // failed

                    Toast.makeText(getActivity()
                            , "Failed To Send", Toast.LENGTH_SHORT).show();
                    break;

                case 1: // progressBar
                    break;

                case 0: // success
                    JSONObject object = new JSONObject(obj.toString());
                    //     {"status":"0","status_description":"File Uploaded Successfully","attachname":"1552318451_Screenshot_20181203-194010_20190311_090349.png"}
                    dataSendServer(object.optString("file_name"));
                    //sendImage();
                    break;

                default:
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
            // TODO: handle exception
        }

    }
    private void dataSendServer(String file_name) {
        try{
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("assignment_id", data.optInt("assignment_id"));
            jsonObject.put("student_id", activity.getStudentDetails().optInt("student_id"));
            jsonObject.put("file_name", file_name);

            HTTPPostTask post = new HTTPPostTask(getActivity(), this);

            post.userRequest(getString(R.string.plwait), 2, "submit_assignment_result", jsonObject.toString());

        }catch (Exception e){
            TraceUtils.logException(e);
        }
    }


}
