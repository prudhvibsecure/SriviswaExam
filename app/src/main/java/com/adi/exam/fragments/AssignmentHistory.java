package com.adi.exam.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adi.exam.R;
import com.adi.exam.SriVishwa;
import com.adi.exam.adapters.AssignmentHistoryAdapter;
import com.adi.exam.adapters.AssignmentListingAdapter;
import com.adi.exam.database.App_Table;
import com.adi.exam.utils.TraceUtils;

import org.json.JSONArray;
import org.json.JSONObject;

public class AssignmentHistory extends ParentFragment implements View.OnClickListener {
    private OnFragmentInteractionListener mFragListener;
    private ProgressBar progressBar;
    private AssignmentHistoryAdapter adapterContent;
    private TextView tv_content_txt;
    private App_Table table;
    private SriVishwa activity;

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View layout = inflater.inflate(R.layout.fragment_examlist, container, false);
        table = new App_Table(getActivity());
        progressBar = layout.findViewById(R.id.pb_content_bar);

        tv_content_txt = layout.findViewById(R.id.tv_content_txt);

        tv_content_txt.setText(R.string.cydhaen);

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

        return layout;
    }

    private void checkAssignment() {
        JSONObject jsonObject = table.getAssignmentHistoryList();
        try {
            JSONArray array = new JSONArray(jsonObject);
            adapterContent.setItems(array);
            adapterContent.notifyDataSetChanged();

        } catch (Exception e) {
            TraceUtils.logException(e);
        }

    }

    @Override
    public void onClick(View view) {
        try {

            switch (view.getId()) {

                case R.id.tv_examtitle:

                    JSONObject jsonObject1 = adapterContent.getItems().getJSONObject((int) view.getTag());

                    activity.showAssignmentResult(jsonObject1.toString());

                    break;

            }

        } catch (Exception e) {

            TraceUtils.logException(e);

        }
    }
}
