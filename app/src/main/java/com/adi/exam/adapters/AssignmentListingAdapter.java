package com.adi.exam.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.adi.exam.R;
import com.adi.exam.common.ColorGenerator;
import com.adi.exam.controls.TextDrawable;
import com.adi.exam.utils.TraceUtils;

import org.json.JSONArray;
import org.json.JSONObject;

public class AssignmentListingAdapter extends RecyclerView.Adapter<AssignmentListingAdapter.ContactViewHolder> {

    private JSONArray array = new JSONArray();

    private OnClickListener onClickListener;

    private TextDrawable.IBuilder builder;

    private ColorGenerator generator;

    private Context mContext;

    public AssignmentListingAdapter(Context context) {

        generator = ColorGenerator.MATERIAL;

        builder = TextDrawable.builder().beginConfig().toUpperCase().textColor(Color.WHITE).endConfig().round();

        mContext = context;

    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @Override
    public int getItemCount() {
        return array.length();
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder contactViewHolder, int position) {

        try {

            JSONObject jsonObject = array.getJSONObject(position);

            String title = jsonObject.optString("assignment_name").trim();

            contactViewHolder.tv_title.setText(title);

            contactViewHolder.tv_mins.setText(mContext.getString(R.string.hours, jsonObject.optString("duration").trim()));

            String noOfQuestions = jsonObject.optString("no_of_questions");

            int totalQuestions = 0;

            if(noOfQuestions.contains(",")) {

                String temp1[] = noOfQuestions.split(",");

                for (int i = 0; i < temp1.length; i++) {

                    if(temp1[i].trim().length() > 0) {

                        totalQuestions = totalQuestions+Integer.parseInt(temp1[i]);

                    }

                }

                noOfQuestions = totalQuestions+"";

            }

            //jsonObject.put("no_of_questions", noOfQuestions);

            //contactViewHolder.tv_ques.setText(mContext.getString(R.string.ques, jsonObject.optString("no_of_questions").trim()));

            contactViewHolder.tv_ques.setText(mContext.getString(R.string.ques, noOfQuestions+""));

            contactViewHolder.tv_subjects.setText(mContext.getString(R.string.subjects, jsonObject.optString("subject").trim()));

            contactViewHolder.tv_startexam.setOnClickListener(onClickListener);

            contactViewHolder.tv_startexam.setTag(position);

            contactViewHolder.tv_examdate.setText(mContext.getString(R.string.date, jsonObject.optString("exam_date").trim()));

            contactViewHolder.tv_examtime.setText(mContext.getString(R.string.time, jsonObject.optString("from_time").trim() + " - " + jsonObject.optString("to_time").trim()));

            int color = generator.getColor(title);

            String tdText = title;

            if (title.contains(" ")) {

                String[] tempArray = title.split(" ");

                tdText = tempArray[0].charAt(0) + "" + tempArray[1].charAt(0);

            } else if (title.length() > 1) {

                tdText = title.substring(0, 1);

            }

            TextDrawable ic1 = builder.build(tdText, color);

            contactViewHolder.iv_iconexamcontent.setImageDrawable(ic1);

        } catch (Exception e) {

            TraceUtils.logException(e);

        }

    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_examcontent_item, viewGroup,
                false);

        return new ContactViewHolder(itemView);
    }

    public int getCount() {
        return array.length();
    }

    public long getItemId(int position) {
        return position + 1;
    }

    public void setItems(JSONArray aArray) {
        this.array = aArray;
    }

    public JSONArray getItems() {
        return this.array;
    }

    public void clear() {

        array = null;

        array = new JSONArray();

    }

    public void release() {

        array = null;

    }

    class ContactViewHolder extends RecyclerView.ViewHolder {

        ImageView iv_iconexamcontent;

        TextView tv_title;

        TextView tv_mins;

        TextView tv_ques;

        TextView tv_startexam;

        TextView tv_subjects;

        TextView tv_examdate;

        TextView tv_examtime;

        ContactViewHolder(View v) {
            super(v);

            iv_iconexamcontent = v.findViewById(R.id.iv_iconexamcontent);

            tv_title = v.findViewById(R.id.tv_title);

            tv_mins = v.findViewById(R.id.tv_mins);

            tv_ques = v.findViewById(R.id.tv_ques);

            tv_startexam = v.findViewById(R.id.tv_startexam);

            tv_subjects = v.findViewById(R.id.tv_subjects);

            tv_examdate = v.findViewById(R.id.tv_examdate);

            tv_examtime = v.findViewById(R.id.tv_examtime);

        }
    }

}