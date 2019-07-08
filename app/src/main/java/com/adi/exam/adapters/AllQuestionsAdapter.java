package com.adi.exam.adapters;

import android.content.Context;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.adi.exam.R;
import com.adi.exam.utils.TraceUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONObject;

public class AllQuestionsAdapter extends RecyclerView.Adapter<AllQuestionsAdapter.ContactViewHolder> {

    private JSONArray array = new JSONArray();

    private ImageLoader imageLoader;

    private Context mContext;

    public AllQuestionsAdapter(Context context) {

        mContext = context;

        imageLoader = ImageLoader.getInstance();

    }

    @Override
    public int getItemCount() {
        return array.length();
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder contactViewHolder, int position) {

        try {

            JSONObject jsonObject = array.getJSONObject(position);

            contactViewHolder.tv_qnumber.setText(mContext.getString(R.string.qnumber, position+""));

            imageLoader.displayImage("file://" + Environment.getExternalStorageDirectory() + "/allimages/" + jsonObject.optString("question_name"), contactViewHolder.iv_qimage);

        } catch (Exception e) {

            TraceUtils.logException(e);

        }

    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_allquestions_item, viewGroup,
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

        TextView tv_qnumber;

        ImageView iv_qimage;

        ContactViewHolder(View v) {
            super(v);

            tv_qnumber = v.findViewById(R.id.tv_qnumber);

            iv_qimage = v.findViewById(R.id.iv_qimage);

        }

    }

}