package com.adi.exam.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adi.exam.R;
import com.adi.exam.SriVishwa;
import com.adi.exam.adapters.AssignmentHistoryAdapter;
import com.adi.exam.adapters.AssignmentListingAdapter;
import com.adi.exam.adapters.MaterialAdapter;
import com.adi.exam.database.App_Table;
import com.adi.exam.utils.TraceUtils;

import org.json.JSONArray;
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

        layout = inflater.inflate(R.layout.fragment_examlist, container, false);
        table = new App_Table(getActivity());
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

}
