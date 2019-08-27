package com.adi.exam.fragments;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.adi.exam.database.Database;
import com.adi.exam.database.PhoneComponent;
import com.adi.exam.tasks.HTTPPostTask;
import com.adi.exam.utils.TraceUtils;
import com.adi.exam.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ExamList extends ParentFragment implements View.OnClickListener, IItemHandler {

    //TODO: error handling -> make use of tv_content_txt

    private OnFragmentInteractionListener mFragListener;

    private ProgressBar progressBar;

    private TextView tv_content_txt;

    private SriVishwa activity;

    private ExamContentListingAdapter adapterContent;

    private int mCount = 0;

    private int iCounter = 0;

    private long time = 0;

    private long left_over_time = 0;

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

        if (isNetworkAvailable()) {

            JSONArray jsonArray = getExams();

            if (jsonArray.length() > 0) {

                adapterContent.setItems(jsonArray);

                adapterContent.notifyDataSetChanged();

                progressBar.setVisibility(View.GONE);

                //updateOtherDetails(adapterContent.getItems());

            }

            checkQuestionPaper();


        } else {
            progressBar.setVisibility(View.GONE);
           /* PhoneComponent phncomp = new PhoneComponent(this, activity, 3);
            phncomp.executeLocalDBInBackground("EXAM");*/
            JSONArray jsonArray = getExams();

            if (jsonArray.length() > 0) {

                adapterContent.setItems(jsonArray);

                adapterContent.notifyDataSetChanged();

                progressBar.setVisibility(View.GONE);

                //updateOtherDetails(adapterContent.getItems());

            } else {
                Toast.makeText(activity, "No Data Found", Toast.LENGTH_SHORT).show();
            }

        }

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
                    App_Table table = new App_Table(activity);

                    String iwhereClause = "exam_id = '" + jsonObject1.optString("exam_id") + "'";
                    boolean isRecordExits = table.isRecordExits(iwhereClause, "STUDENTEXAMRESULT");

                    if (isRecordExits) {

                        activity.showokPopUp(R.drawable.pop_ic_failed, "", activity.getString(R.string.yhadwte));

                        return;

                    }
                    JSONObject question_details = jsonObject1.getJSONObject("question_details");
                    String timestamp = new SimpleDateFormat("dd-MM-yyyy ")
                            .format(new Date()) // get the current date as String
                            .concat(question_details.optString("from_time").trim()
                            );
                    DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm a");
                    Date date1 = (Date) formatter.parse(timestamp);

                    long duration_secs = jsonObject1.optLong("duration_sec");
                    long current_time = System.currentTimeMillis();//current time
                    long from_time = date1.getTime();// from time


                    Date date2 = formatter.parse(timestamp);
                    if (from_time < current_time) {
                        long left_time = current_time - from_time;

//                        DateFormat dateFormat2 = new SimpleDateFormat("hh:mm");
//                        String dateString2 = dateFormat2.format();
//                        Log.e("current time",dateString2);
//                        String []time_array1=dateString2.split(":");
//                        long tt2=Integer.parseInt(time_array1[0])*3600+Integer.parseInt(time_array1[1])*60;

                        left_over_time = duration_secs - (left_time / 1000);


                    }
                    jsonObject1.put("duration_sec", left_over_time);
                   /* JSONObject question_details = jsonObject1.getJSONObject("question_details");

                    String dateTime = question_details.optString("exam_date").trim() + " " + question_details.optString("from_time").trim();

                    Date examDateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH).parse(dateTime);

                    Calendar c = Calendar.getInstance();

                    SimpleDateFormat df1 = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH);

                    String formattedDate1 = df1.format(c.getTime());

                    Date currentDateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH).parse(formattedDate1);

                    long[] diff = getDifference(currentDateTime, examDateTime);

                    if (diff[0] > 0) {

                        activity.showokPopUp(R.drawable.pop_ic_info, "", activity.getString(R.string.indays, diff[0] + ""));

                        return;

                    }

                    if (diff[1] > 0) {

                        activity.showokPopUp(R.drawable.pop_ic_info, "", activity.getString(R.string.inhours, diff[1] + "", diff[2] + "", diff[3] + ""));

                        return;

                    }

                    if (diff[2] > 0) {

                        activity.showokPopUp(R.drawable.pop_ic_info, "", activity.getString(R.string.inmins, diff[2] + "", diff[3] + ""));

                        return;

                    }

                    if (diff[3] > 0) {

                        activity.showokPopUp(R.drawable.pop_ic_info, "", activity.getString(R.string.insecs, diff[3] + ""));

                        return;

                    }

                    if (diff[0] < 0) {

                        activity.showokPopUp(R.drawable.pop_ic_info, "", activity.getString(R.string.ethbf));

                        return;

                    }

                    if (diff[1]<0){
                        long hoursInSecs = diff[1] * 60 * 60;

                        long minsInSecs = diff[2] * 60;

                        long secInSecs = diff[3];

                        long totalDelay = Math.abs(hoursInSecs) + Math.abs(minsInSecs) + Math.abs(secInSecs);

                        long duration_secs = jsonObject1.optLong("duration_sec");

                        long leftOverSeconds = duration_secs - totalDelay;

                        if (leftOverSeconds < 0) {

                            activity.showokPopUp(R.drawable.pop_ic_info, "", activity.getString(R.string.ethbf));

                            return;

                        } else {

                            jsonObject1.put("duration_sec", leftOverSeconds);
                            activity.showInstructionsScreen(jsonObject1, true);
                            return;
                        }

                    }

                    String dateTime1 = question_details.optString("exam_date").trim() + " " + question_details.optString("to_time").trim();

                    Date examDateTime1 = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH).parse(dateTime1);

                    Calendar c1 = Calendar.getInstance();

                    SimpleDateFormat df11 = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH);

                    String formattedDate11 = df11.format(c1.getTime());

                    Date currentDateTime1 = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH).parse(formattedDate11);

                    long[] diff1 = getDifference(currentDateTime1,examDateTime1 );

                    if (diff1[1] < 0) {

                        long hoursInSecs = diff1[1] * 60 * 60;

                        long minsInSecs = diff1[2] * 60;

                        long secInSecs = diff1[3];

                        long totalDelay = Math.abs(hoursInSecs) + Math.abs(minsInSecs) + Math.abs(secInSecs);

                        long duration_secs = jsonObject1.optLong("duration_sec");

                        long leftOverSeconds = duration_secs - totalDelay;

                        if (leftOverSeconds < 0) {

                            activity.showokPopUp(R.drawable.pop_ic_info, "", activity.getString(R.string.ethbf));

                            return;

                        } else {

                            jsonObject1.put("duration_sec", leftOverSeconds);

                        }

                    } else if (diff1[2] < 0) {

                        long minsInSecs = diff1[2] * 60;

                        long secInSecs = diff1[3];

                        long totalDelay = Math.abs(minsInSecs) + Math.abs(secInSecs);

                        long duration_secs = jsonObject1.optLong("duration_sec");

                        long leftOverSeconds = duration_secs - totalDelay;

                        if (leftOverSeconds < 0) {

                            activity.showokPopUp(R.drawable.pop_ic_info, "", activity.getString(R.string.ethbf));

                            return;

                        } else {

                            jsonObject1.put("duration_sec", leftOverSeconds);

                        }

                    } else if (diff1[3] < 0) {

                        long secInSecs = diff1[3];

                        long totalDelay = Math.abs(secInSecs);

                        long duration_secs = jsonObject1.optLong("duration_sec");

                        long leftOverSeconds = duration_secs - totalDelay;

                        if (leftOverSeconds < 0) {

                            activity.showokPopUp(R.drawable.pop_ic_info, "", activity.getString(R.string.ethbf));

                            return;

                        } else {

                            jsonObject1.put("duration_sec", leftOverSeconds);

                        }

                    }*/

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
                                if (jsonObject1.optString("jee_adv").equalsIgnoreCase("0")) {

                                    reqObject.put("student_id", activity.getStudentDetails().optString("student_id"));

                                    reqObject.put("question_paper_id", jsonObject1.optString("question_paper_id"));

                                    HTTPPostTask post = new HTTPPostTask(activity, this);

                                    post.disableProgress();

                                    post.userRequest(getString(R.string.plwait), 2, "getquestionpaper", reqObject.toString());
                                } else {
                                    reqObject.put("student_id", activity.getStudentDetails().optString("student_id"));

                                    reqObject.put("question_paper_id", jsonObject1.optString("question_paper_id"));

                                    HTTPPostTask post = new HTTPPostTask(activity, this);

                                    post.disableProgress();

                                    post.userRequest(getString(R.string.plwait), 2, "getquestionpaper_jadvance", reqObject.toString());
                                }

                            }

                            return;

                        } else {
                            Toast.makeText(activity, "No Data Found", Toast.LENGTH_SHORT).show();
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

                       /* PhoneComponent phncomp = new PhoneComponent(this, activity, 3);
                       // phncomp.defineWhereClause("");
                        phncomp.executeLocalDBInBackground("EXAM");*/


                        JSONArray jsonArray = getExams();

                        if (jsonArray.length() > 0) {

                            adapterContent.setItems(jsonArray);

                            adapterContent.notifyDataSetChanged();

                            progressBar.setVisibility(View.GONE);

                            //updateOtherDetails(adapterContent.getItems());

                        } else {
                            Toast.makeText(activity, "No Data Found", Toast.LENGTH_SHORT).show();
                        }

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
    private long[] getDifference(Date startDate, Date endDate) {
        //milliseconds

        long[] vals = new long[4];

        long different = endDate.getTime() - startDate.getTime();

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = different / daysInMilli;

        vals[0] = elapsedDays;

        different = different % daysInMilli;

        long elapsedHours = different / hoursInMilli;

        vals[1] = elapsedHours;

        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;

        vals[2] = elapsedMinutes;

        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;

        vals[3] = elapsedSeconds;

       /* System.out.printf(
                "%d days, %d hours, %d minutes, %d seconds%n",
                elapsedDays, elapsedHours, elapsedMinutes, elapsedSeconds);*/

        return vals;

    }

    public boolean isNetworkAvailable() {

        ConnectivityManager manager = (ConnectivityManager) getActivity()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            return false;
        }

        NetworkInfo net = manager.getActiveNetworkInfo();
        if (net != null) {
            if (net.isConnected()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }

    }

    public JSONArray getExams() {

        Database database = new Database(getActivity());

        SQLiteDatabase db;

        JSONArray array = new JSONArray();

        try {
            if (database != null) {

                //String cursor_q = "select * from EXAM";
                String cursor_q = "SELECT exam.exam_id,exam.exam_name,exam.course,exam.no_of_questions,exam.subjects,exam.marks_per_question,exam.negative_marks,exam.duration,exam.duration_sec,questionpaper.from_time,questionpaper.to_time,questionpaper.exam_date,questionpaper.question_paper_id,questionpaper.exam_id,questionpaper.subjects,questionpaper.topicids,questionpaper.year,questionpaper.paper,questionpaper.paper" +
                        " from exam inner join questionpaper on exam.exam_id = questionpaper.exam_id order by questionpaper.exam_date,questionpaper.from_time";
                db = database.getWritableDatabase();
                Cursor cursor = db
                        .rawQuery(cursor_q,
                                null);
                try {
                    if (null != cursor)
                        if (cursor.getCount() > 0) {
                            if (cursor.moveToFirst()) {
                                do {
                                    JSONObject obj = new JSONObject();

                                    obj.put("exam_id", cursor.getString(cursor.getColumnIndex("exam_id")));
                                    obj.put("exam_name", cursor.getString(cursor.getColumnIndex("exam_name")));
                                    obj.put("course", cursor.getString(cursor.getColumnIndex("course")));
                                    obj.put("no_of_questions", cursor.getString(cursor.getColumnIndex("no_of_questions")));
                                    obj.put("subjects", cursor.getString(cursor.getColumnIndex("subjects")));
                                    obj.put("marks_per_question", cursor.getString(cursor.getColumnIndex("marks_per_question")));
                                    obj.put("negative_marks", cursor.getString(cursor.getColumnIndex("negative_marks")));
                                    obj.put("duration", cursor.getString(cursor.getColumnIndex("duration")));
                                    obj.put("duration_sec", cursor.getString(cursor.getColumnIndex("duration_sec")));
                                    JSONObject obj1 = new JSONObject();
                                    obj1.put("from_time", cursor.getString(cursor.getColumnIndex("from_time")));
                                    obj1.put("to_time", cursor.getString(cursor.getColumnIndex("to_time")));
                                    obj1.put("exam_date", cursor.getString(cursor.getColumnIndex("exam_date")));
                                    obj1.put("question_paper_id", cursor.getString(cursor.getColumnIndex("question_paper_id")));
                                    obj1.put("exam_id", cursor.getString(cursor.getColumnIndex("exam_id")));
                                    obj1.put("subjects", cursor.getString(cursor.getColumnIndex("subjects")));
                                    obj1.put("topicids", cursor.getString(cursor.getColumnIndex("topicids")));
                                    obj1.put("year", cursor.getString(cursor.getColumnIndex("year")));
                                    obj1.put("paper", cursor.getString(cursor.getColumnIndex("paper")));
                                    obj.put("question_details", obj1);

                                    String dateTime = cursor.getString(cursor.getColumnIndex("exam_date")).trim() + " " + cursor.getString(cursor.getColumnIndex("to_time")).trim();

                                    Date examDateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH).parse(dateTime);

                                    Calendar c = Calendar.getInstance();

                                    SimpleDateFormat df1 = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH);

                                    String formattedDate1 = df1.format(c.getTime());

                                    Date currentDateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH).parse(formattedDate1);

                                    long[] diff = getDifference(currentDateTime, examDateTime);

                                    String edate = cursor.getString(cursor.getColumnIndex("exam_date")).trim();
                                    String cdate = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(c.getTime());
                                    Date exdate = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).parse(edate);
                                    Date cudate = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).parse(cdate);

                                    //array.put(obj);

                                    if (exdate.equals(cudate) || examDateTime.compareTo(currentDateTime) > 0) {

                                      /* if(diff[1] > 0 || diff[2] > 0 || diff[3] > 0)
                                       {*/
                                        array.put(obj);
                                        //}
                                    }

                                } while (cursor.moveToNext());
                            }
                        }
                    cursor.close();
                    db.close();
                } finally {
                    db.close();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return array;
    }


    public void refresh() throws Exception {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("student_id", activity.getStudentDetails().optString("student_id"));

        HTTPPostTask post = new HTTPPostTask(activity, this);

        post.disableProgress();

        post.userRequest(getString(R.string.plwait), 1, "checkqustionpaper", jsonObject.toString());
    }
}
