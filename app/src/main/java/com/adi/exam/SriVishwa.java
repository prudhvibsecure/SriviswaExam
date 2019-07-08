package com.adi.exam;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.adi.exam.callbacks.IDialogCallbacks;
import com.adi.exam.callbacks.IItemHandler;
import com.adi.exam.common.AppPreferences;
import com.adi.exam.dialogfragments.AppUpdateDialog;
import com.adi.exam.dialogfragments.ExitDialog;
import com.adi.exam.dialogfragments.MessageDialog;
import com.adi.exam.fragments.AllQuestions;
import com.adi.exam.fragments.Assignment;
import com.adi.exam.fragments.AssignmentList;
import com.adi.exam.fragments.Dashboard;
import com.adi.exam.fragments.ExamList;
import com.adi.exam.fragments.ExamSubmitConfirmationPage;
import com.adi.exam.fragments.ExamTemplates;
import com.adi.exam.fragments.Instructions;
import com.adi.exam.fragments.Lessons;
import com.adi.exam.fragments.Materials;
import com.adi.exam.fragments.ParentFragment;
import com.adi.exam.fragments.ResultsPage;
import com.adi.exam.fragments.Subjects;
import com.adi.exam.fragments.Topics;
import com.adi.exam.tasks.HTTPPostTask;
import com.adi.exam.utils.TraceUtils;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Stack;

public class SriVishwa extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ParentFragment.OnFragmentInteractionListener, Dashboard.OnListFragmentInteractionListener, View.OnClickListener, IItemHandler, AppUpdateDialog.IUpdateCallback {

    private Toolbar toolbar;

    private ActionBarDrawerToggle toggle;

    private DrawerLayout drawer;

    private FragmentManager manager = null;

    private Stack<ParentFragment> fragStack = null;

    private JSONObject studentDetails;

    private JSONArray allQuestions = new JSONArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_srivishwa);

        toolbar = findViewById(R.id.toolbar);

        toolbar.setTitle(R.string.dashboard);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            getSupportActionBar().setDisplayShowHomeEnabled(true);

        }

        /*FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        drawer = findViewById(R.id.drawer_layout);

        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.addDrawerListener(toggle);

        toggle.syncState();

        fragStack = new Stack<>();

        manager = getSupportFragmentManager();

        if (savedInstanceState != null) {

            removeAllFragments();

        }

        swiftFragments(Dashboard.newInstance(), "home");

        toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onKeyDown(4, null);

            }

        });

        try {

            studentDetails = new JSONObject(AppPreferences.getInstance(this).getFromStore("studentDetails"));

        } catch (Exception e) {

            TraceUtils.logException(e);

        }

        updateToken();

        checkAppUpdate();

        NavigationView navigationView = findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener(this);

        ((TextView) navigationView.findViewById(R.id.tv_appversion)).setText(getString(R.string.app_version, applicationVName()));

        View header = navigationView.getHeaderView(0);

        ((TextView) header.findViewById(R.id.tv_name)).setText(studentDetails.optString("student_name"));

        ((TextView) header.findViewById(R.id.tv_classbatch)).setText(studentDetails.optString("roll_no"));

    }

    public JSONObject getStudentDetails() {

        return studentDetails;

    }

    @Override
    public void onBackPressed() {

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.sri_vishwa, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_materials) {

            showSubjects();

        } else if (id == R.id.nav_logout) {

            SriVishwa.this.finish();

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        drawer.closeDrawer(GravityCompat.START);

        return true;

    }

    @Override
    public void onFragmentInteraction(int stringid, boolean blockMenu) {

        toolbar.setTitle(stringid);

        lockUnLockMenu(blockMenu);

    }

    @Override
    public void onFragmentInteraction(String title, boolean blockMenu) {

        toolbar.setTitle(title);

        lockUnLockMenu(blockMenu);

    }

    public void lockUnLockMenu(boolean mode) {

        toggle.setDrawerIndicatorEnabled(mode);

        drawer.setDrawerLockMode(mode ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

    }

    @Override
    public void onListFragmentInteraction(JSONObject jsonObject) {

        try {

            int position = jsonObject.getInt("position");

            switch (position) {

                case 1:

                    if (checkPermission("android.permission.READ_EXTERNAL_STORAGE", 100) == 1) {

                        swiftFragments(ExamList.newInstance(), "examlist");

                    }

                    break;

                case 2:
                    break;

                case 3:

                    showAssignmentList();

                    break;

                case 4:
                    break;

            }

        } catch (Exception e) {

            TraceUtils.logException(e);

        }

    }

    @Override
    public void onClick(View v) {

        try {

            switch (v.getId()) {

                case R.id.tv_ok:

                    SriVishwa.this.finish();

                    break;

                case R.id.tv_cancel:
                    break;

            }

        } catch (Exception e) {

            TraceUtils.logException(e);

        }

    }

    public void swiftFragments(ParentFragment frag, String tag) {

        FragmentTransaction trans = manager.beginTransaction();

        ParentFragment tempFrag = (ParentFragment) manager.findFragmentByTag(tag);
        if (tempFrag != null) {

            if (tempFrag.isAdded() && tempFrag.isVisible())
                return;
            else if (tempFrag.isAdded() && tempFrag.isHidden()) {

                trans.hide(fragStack.get(fragStack.size() - 1));
                trans.show(tempFrag);

                fragStack.remove(tempFrag);
                fragStack.add(tempFrag);

            }

        } else if (!frag.isAdded()) {

            if (fragStack.size() > 0) {
                ParentFragment pf = fragStack.get(fragStack.size() - 1);
                trans.hide(pf);
            }

            trans.add(R.id.container, frag, tag);
            trans.show(frag);

            fragStack.push(frag);
        }

        try {

            if (SriVishwaApp.isInterestingActivityVisible()) {
                trans.commit();
            } else {
                trans.commitAllowingStateLoss();
            }

        } catch (IllegalStateException e) {
            TraceUtils.logException(e);
        }

    }

    private void removeAllFragments() {

        FragmentTransaction trans = manager.beginTransaction();

        for (int i = 0; i < manager.getFragments().size(); i++) {

            trans.remove(manager.getFragments().get(i));

        }

        trans.commitAllowingStateLoss();

        trans = null;

    }

    public void showInstructionsScreen(JSONObject jsonObject, boolean showProceed) {

        swiftFragments(Instructions.newInstance(jsonObject.toString(), showProceed), "instructions");

    }

    public void showAllQuestions() {

        swiftFragments(AllQuestions.newInstance(), "allquestions");

    }

    public void showExamSubmitConfirmationPage(JSONObject jsonObject, int student_exam_result_id, int type) {

        swiftFragments(ExamSubmitConfirmationPage.newInstance(jsonObject.toString(), student_exam_result_id, type), "examsubmitconfirmationpage");

    }

    public void showResults(String examDetails, int student_exam_result_id, int type) {

        swiftFragments(ResultsPage.newInstance(examDetails, student_exam_result_id, type), "results");

    }

    public void showSubjects() {

        swiftFragments(Subjects.newInstance(), "subjects");

    }

    public void showLessons(String data) {

        swiftFragments(Lessons.newInstance(data), "lessons");

    }

    public void showTopics(String data) {

        swiftFragments(Topics.newInstance(data), "topics");

    }

    public void showMaterials(String data) {

        swiftFragments(Materials.newInstance(data), "materials");

    }

    public void showExamTemplate(String data) {

        try {

            JSONObject jsonObject = new JSONObject(data);

            if (jsonObject.optString("course").equalsIgnoreCase("1") || jsonObject.optString("course").equalsIgnoreCase("2")) { //JEE

                swiftFragments(ExamTemplates.newInstance(data), "examtemplate");

            } else if (jsonObject.optString("course").equalsIgnoreCase("3")) { //BITSAT

                //localPath = "file:///android_asset/BITSAT/bitsat0.html";

            } else if (jsonObject.optString("course").equalsIgnoreCase("4") || jsonObject.optString("course").equalsIgnoreCase("8")) { //EAMCET

                //localPath = "file:///android_asset/EAMCET/eamcet.html";

            } else if (jsonObject.optString("course").equalsIgnoreCase("5")) { //NEET

                swiftFragments(ExamTemplates.newInstance(data), "examtemplate");

            } else if (jsonObject.optString("course").equalsIgnoreCase("6")) { //AIIMS

                //localPath = "file:///android_asset/AIIMS/aiims.html";

            } else if (jsonObject.optString("course").equalsIgnoreCase("7")) { //JIPMER

                //localPath = "file:///android_asset/JIPMER/jipmer.html";

            } else if (jsonObject.optString("course").equalsIgnoreCase("9")) { //KVPY

                //localPath = "file:///android_asset/KVPY/kvpy.html";

            }

        } catch (Exception e) {

            TraceUtils.logException(e);

        }

    }

    public void showAssignmentList() {

        swiftFragments(AssignmentList.newInstance(), "assignmentlist");

    }

    public void showAssignment(String data) {

        swiftFragments(Assignment.newInstance(data), "assignment");

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        try {
            if (keyCode == 4) {

                if (fragStack.size() > 0) {

                    ParentFragment pf = fragStack.peek();

                    if (pf.back())
                        return true;

                    fragStack.pop();

                    FragmentTransaction trans = manager.beginTransaction();
                    trans.remove(pf);

                    if (fragStack.size() > 0) {
                        ParentFragment pf1 = fragStack.get(fragStack.size() - 1);
                        trans.show(pf1);
                    } /*else {
                        swiftHomeFragments(null, fragHomeStack.peek());
                    }*/

                    trans.commit();

                    return true;
                }

                showExitDialog();
                return false;

            }
        } catch (Exception e) {
            TraceUtils.logException(e);
        }

        return super.onKeyDown(keyCode, event);
    }

    public void showExitDialog() {

        ExitDialog.newInstance().show(getSupportFragmentManager(), "dialog");

    }

    public void showToast(int text) {

        Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);

        try {

            TextView v = toast.getView().findViewById(android.R.id.message);
            v.setTextColor(Color.WHITE);

        } catch (Exception e) {

            TraceUtils.logException(e);

        } finally {

            toast.show();

        }

    }

    public void showokPopUp(int drawableId, String title, String message) {

        try {

            Bundle bundle = new Bundle();

            bundle.putString("title", title);

            bundle.putString("message", message);

            bundle.putInt("drawableId", drawableId);

            MessageDialog.newInstance(bundle).show(getSupportFragmentManager(), "dialog");

        } catch (Exception e) {

            TraceUtils.logException(e);

        }

    }

    public void showokPopUp(int drawableId, String title, String message, int requestId, IDialogCallbacks callbacks, boolean showCancel) {

        try {

            Bundle bundle = new Bundle();

            bundle.putString("title", title);

            bundle.putString("message", message);

            bundle.putInt("requestId", requestId);

            bundle.putInt("drawableId", drawableId);

            bundle.putBoolean("showcancel", showCancel);

            MessageDialog messages = MessageDialog.newInstance(bundle);

            messages.setIDialogListener(callbacks);

            messages.show(this.getSupportFragmentManager(), "dialog");

        } catch (Exception e) {

            TraceUtils.logException(e);

        }

    }

    public void showokPopUp(int drawableId, String title, String message, int requestId, String activateBut, String cancelBut, IDialogCallbacks callbacks) {

        try {

            Bundle bundle = new Bundle();

            bundle.putString("title", title);

            bundle.putString("message", message);

            bundle.putInt("requestId", requestId);

            bundle.putInt("drawableId", drawableId);

            bundle.putString("cancelBut", cancelBut);

            bundle.putString("activateBut", activateBut);

            bundle.putBoolean("showcancel", true);

            MessageDialog messages = MessageDialog.newInstance(bundle);

            messages.setIDialogListener(callbacks);

            messages.show(this.getSupportFragmentManager(), "dialog");

        } catch (Exception e) {

            TraceUtils.logException(e);

        }

    }

    public void showokPopUp(int drawableId, String title, String message, int requestId, String activateBut, IDialogCallbacks callbacks, boolean showCancel) {

        try {

            Bundle bundle = new Bundle();

            bundle.putString("title", title);

            bundle.putString("message", message);

            bundle.putInt("requestId", requestId);

            bundle.putInt("drawableId", drawableId);

            bundle.putBoolean("showcancel", showCancel);

            bundle.putString("activateBut", activateBut);

            MessageDialog messages = MessageDialog.newInstance(bundle);

            messages.setIDialogListener(callbacks);

            messages.show(this.getSupportFragmentManager(), "dialog");

        } catch (Exception e) {

            TraceUtils.logException(e);

        }

    }

    public int checkPermission(String permissions, int code) {

        if (ActivityCompat.checkSelfPermission(this, permissions) == PackageManager.PERMISSION_GRANTED) {
            return 1;
        }

        if (ActivityCompat.checkSelfPermission(this, permissions) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    permissions)) {

                ActivityCompat.requestPermissions(this,
                        new String[]{permissions},
                        code);

                return 0;

            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{permissions},
                        code);

                return 0;
            }
        }
        return -1;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case 100:

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    swiftFragments(ExamList.newInstance(), "examlist");

                } else {

                    showToast(R.string.pd);

                }

                break;

            case 200:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    fragStack.peek().onRequestPermissionsResult(requestCode, true);

                } else {

                    showToast(R.string.pd);

                }

                break;

        }
    }

    public void setAllQuestions(JSONArray aAllQuestions) {

        allQuestions = aAllQuestions;

    }

    public JSONArray getAllQuestions() {

        return allQuestions;

    }

    private void updateToken() {

        try {

            String token = AppPreferences.getInstance(this).getFromStore("token");

            if (token.trim().length() == 0)
                return;

            JSONObject jsonObject = new JSONObject();

            jsonObject.put("student_id", studentDetails.optString("student_id"));

            jsonObject.put("regidand", AppPreferences.getInstance(this).getFromStore("token"));

            HTTPPostTask post = new HTTPPostTask(this, this);

            post.disableProgress();

            post.userRequest(getString(R.string.plwait), 1, "updatedevice", jsonObject.toString());

        } catch (Exception e) {

            TraceUtils.logException(e);

        }

    }

    private void checkAppUpdate() {

        try {

            JSONObject jsonObject = new JSONObject();

            jsonObject.put("version", applicationVName());

            HTTPPostTask post = new HTTPPostTask(this, this);

            post.disableProgress();

            post.userRequest(getString(R.string.plwait), 2, "checkversion", jsonObject.toString());

        } catch (Exception e) {

            TraceUtils.logException(e);

        }

    }

    @Override
    public void onFinish(Object results, int requestId) {

        try {

            if (requestId == 1) {

                JSONObject jsonObject = new JSONObject(results.toString());

                if (jsonObject.optString("statuscode").equalsIgnoreCase("200")) {


                    //Nothing  to do here

                }

            } else if (requestId == 2) {

                JSONObject jsonObject = new JSONObject(results.toString());

                if (jsonObject.optString("statuscode").equalsIgnoreCase("200")) {

                    if (jsonObject.has("new_version")) {

                        String appServerVersion = "1.0.0";

                        String temp = jsonObject.optString("new_version").trim();

                        if (temp.length() > 0)
                            appServerVersion = temp;

                        String appVersion = applicationVName();

                        int diff = compareVersions(appServerVersion, appVersion);

                        if (diff == 1) {

                            showUpdateDialog(jsonObject.optString("mandatory").equalsIgnoreCase("true") ? "N" : "Y");

                        }

                    }

                }

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

    public String applicationVName() {

        String versionName = "";

        PackageManager manager = getPackageManager();
        try {

            PackageInfo info = manager.getPackageInfo(getPackageName(),
                    0);
            versionName = info.versionName;

        } catch (PackageManager.NameNotFoundException e) {

            TraceUtils.logException(e);

        } catch (Exception e) {

            TraceUtils.logException(e);

        }

        return versionName;

    }

    public void showUpdateDialog(String showskip) {

        Bundle bundle = new Bundle();
        bundle.putString("showskip", showskip);

        AppUpdateDialog updateDialog = AppUpdateDialog.newInstance(bundle);
        updateDialog.setIUpdateCallback(this);
        updateDialog.show(getSupportFragmentManager(), "dialog");

    }

    @Override
    public void onUserAction(String showskip, int id) {

        switch (id) {

            case R.id.tv_updatecancel:

                if (showskip.equalsIgnoreCase("y")) {

                    return;

                }

                closeActivity();

                break;

            case R.id.tv_updateok:

                //lauchExternalBrowser("market://details?id=" + getPackageName());

                //closeActivity();

                break;

        }

    }

    public void closeActivity() {

        SriVishwa.this.finish();

    }

    private int compareVersions(String ver1, String ver2) {

        String[] vals1 = ver1.split("\\.");

        String[] vals2 = ver2.split("\\.");

        int i = 0;

        while (i < vals1.length && i < vals2.length
                && vals1[i].equals(vals2[i])) {

            i++;

        }

        if (i < vals1.length && i < vals2.length) {

            int diff = Integer.valueOf(vals1[i]).compareTo(
                    Integer.valueOf(vals2[i]));

            return diff < 0 ? -1 : diff == 0 ? 0 : 1;

        }

        return vals1.length < vals2.length ? -1
                : vals1.length == vals2.length ? 0 : 1;

    }

}
