package com.adi.exam;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.adi.exam.callbacks.IItemHandler;
import com.adi.exam.common.AppPreferences;
import com.adi.exam.common.AppSettings;
import com.adi.exam.common.NetworkInfoAPI;
import com.adi.exam.controls.CustomEditText;
import com.adi.exam.controls.CustomTextView;
import com.adi.exam.database.PhoneComponent;
import com.adi.exam.dialogfragments.MessageDialog;
import com.adi.exam.fragments.WifiFragment;
import com.adi.exam.tasks.HTTPPostTask;
import com.adi.exam.utils.TraceUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity implements IItemHandler {
    private final List blockedKeys = new ArrayList(Arrays.asList(KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_UP));
    private NetworkInfoAPI network = null;
    CustomTextView sname, sclass, batch, stuid;
    CustomEditText user;

    //private PhoneComponent phncomp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_login);

        getSupportActionBar().hide();

        ((EditText) findViewById(R.id.et_password))
                .setTransformationMethod(PasswordTransformationMethod
                        .getInstance());

        findViewById(R.id.tv_frgtpwd).setOnClickListener(onClick);
        findViewById(R.id.connect_wife).setOnClickListener(onClick);

        findViewById(R.id.tv_register).setOnClickListener(onClick);

        findViewById(R.id.tv_login).setOnClickListener(onClick);

        findViewById(R.id.iv_settings).setOnClickListener(onClick);

        sname = findViewById(R.id.sname);
        stuid = findViewById(R.id.stuid);
        sclass = findViewById(R.id.sclass);
        batch = findViewById(R.id.batch);
        user = findViewById(R.id.et_username);

        network = new NetworkInfoAPI();

        network.initialize(this);
        sendDeviceId();
        //phncomp = new PhoneComponent(this, this, 2);

    }

    OnClickListener onClick = new OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {

                case R.id.iv_settings:
                    //launchSettingsActivity();
                    break;

                case R.id.tv_login:
                    checkUserCredentials();
                    break;

                case R.id.tv_register:
                    launchRegistrationActivity();
                    break;

                case R.id.tv_frgtpwd:
                    launchForgotActivity();
                    break;
                case R.id.connect_wife:
                    Intent ns = new Intent(LoginActivity.this, CheckWifi.class);
                    startActivity(ns);
                    break;

                default:
                    break;
            }

        }
    };

    public void launchActivity() {
        Intent intent = new Intent(this, SriVishwa.class);
        LoginActivity.this.finish();
        startActivity(intent);
    }

    public void launchProfileActivity(String studentDetails) {
        Intent intent = new Intent(this, SriVishwa.class);
        intent.putExtra("studentDetails", studentDetails);
        startActivity(intent);
        LoginActivity.this.finish();
    }

    public void launchRegistrationActivity() {

        if (network.execute("getConnectionInfo").equalsIgnoreCase("none")) {
            showokPopUp(getString(R.string.errorTxt),
                    getString(R.string.network));
        }

        /*Intent intent = new Intent(this, RegistrationActivity.class);
        startActivityForResult(intent, 100);*/
    }

    public void launchForgotActivity() {

        if (network.execute("getConnectionInfo").equalsIgnoreCase("none")) {
            showokPopUp(getString(R.string.errorTxt),
                    getString(R.string.network));
        }

        /*Intent intent = new Intent(this, ForgotActivity.class);
        startActivityForResult(intent, 100);*/
    }

    private void validateLoginRequest() {

        try {

            if (network.execute("getConnectionInfo").equalsIgnoreCase("none")) {
                showokPopUp(getString(R.string.errorTxt),
                        getString(R.string.network));
                return;
            }

            String username = ((EditText) findViewById(R.id.et_username)).getText()
                    .toString().trim();

            if (username.length() == 0) {

                showokPopUp(getString(R.string.errorTxt),
                        getString(R.string.peui));

                return;
            }

            String password = ((EditText) findViewById(R.id.et_password)).getText()
                    .toString().trim();

            if (password.length() == 0) {
                showokPopUp(getString(R.string.errorTxt),
                        getString(R.string.pePswd));
                return;
            }

            sendLoginRequest(username, password);

        } catch (Exception e) {
            TraceUtils.logException(e);
        }

    }

    private void checkUserCredentials() {

        try {

            String username = ((EditText) findViewById(R.id.et_username)).getText()
                    .toString().trim();

            if (username.length() == 0) {

                showokPopUp(getString(R.string.errorTxt),
                        getString(R.string.peui));

                return;
            }

            String password = ((EditText) findViewById(R.id.et_password)).getText()
                    .toString().trim();

            if (password.length() == 0) {
                showokPopUp(getString(R.string.errorTxt),
                        getString(R.string.pePswd));
                return;
            }

            sendLoginRequest(username, password);

            /*phncomp.defineWhereClause("application_no = '"+username+"' AND roll_no = '"+password+"'");

            phncomp.executeLocalDBInBackground("STUDENTS");*/

        } catch (Exception e) {
            TraceUtils.logException(e);
        }

    }

    private void sendLoginRequest(String userid, String password) {

        try {

            JSONObject jsonObject = new JSONObject();

            jsonObject.put("username", userid);

            jsonObject.put("password", password);

            HTTPPostTask post = new HTTPPostTask(this, this);

            post.userRequest(getString(R.string.plwait), 1, "studentlogin", jsonObject.toString());

        } catch (Exception e) {

            TraceUtils.logException(e);

        }

    }

    //device id check

    private void sendDeviceId() {

        try {

            JSONObject jsonObject = new JSONObject();

            jsonObject.put("device_id", getDevid());

            //jsonObject.put("imei", getIMEI(this));
            //Toast.makeText(this, getIMEI(this), Toast.LENGTH_SHORT).show();


            HTTPPostTask post = new HTTPPostTask(this, this);

            post.userRequest(getString(R.string.plwait), 3, "get_student_details", jsonObject.toString());

        } catch (Exception e) {

            TraceUtils.logException(e);

        }

    }

    public String getIMEI(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                //return TODO;
            }
        }
        return String.valueOf(telephonyManager.getDeviceId());
    }

    public void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    public void showToast(int text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFinish(Object results, int requestType) {

        try {

            switch (requestType) {
                case 1:

                    parseLoginResponse((String) results);

                    break;


                case 2:

                    if (results != null) {

                        JSONArray jsonArray = (JSONArray) results;

                        if (jsonArray.length() > 0) {

                            JSONObject studentDetails = jsonArray.getJSONObject(0);

                            AppPreferences.getInstance(this).addToStore("studentDetails", studentDetails.toString(), false);

                            launchProfileActivity(studentDetails.toString());

                            return;
                        }

                    }

                    break;

                case 3:

                    JSONObject object = new JSONObject(results.toString());
                    if (object.optString("statuscode").equalsIgnoreCase("200")) {
                        JSONObject student = object.getJSONObject("student_details");

                        sname.setText(student.optString("student_name"));

                        sclass.setText(student.optString("course"));

                        batch.setText(student.optString("year"));

                        stuid.setText(student.optString("username"));

                        user.setText(student.optString("username"));

                        AppPreferences.getInstance(this).addToStore("studentDetails", student.toString(), false);
                    } else {

                    }
                    break;

                default:
                    break;
            }

        } catch (Exception e) {
            TraceUtils.logException(e);
        }

    }

    @Override
    public void onError(String errorCode, int requestType) {

        switch (requestType) {
            case 1:
                showToast(errorCode);
                break;

            default:
                break;
        }
    }

    @Override
    public void onProgressChange(int requestId, Long... values) {

    }

    public void showokPopUp(String title, String message) {

        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("message", message);
        MessageDialog.newInstance(bundle).show(this.getSupportFragmentManager(), "dialog");

    }

    private void parseLoginResponse(Object object) throws Exception {

        JSONObject jsonObject = new JSONObject(object.toString());

        if (jsonObject.optString("statuscode").equalsIgnoreCase("200")) {


            if (jsonObject.has("student_details")) {

                JSONObject jsonObject1 = jsonObject.getJSONObject("student_details");

                AppPreferences.getInstance(this).addToStore("studentDetails", jsonObject1.toString(), false);

                launchProfileActivity(jsonObject1.toString());

                return;

            }

        }

        showokPopUp(getString(R.string.errorTxt), getString(R.string.ic));

    }

    public String getDevid() {
        String android_id = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return android_id;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) {
            // Close every kind of system dialog
            Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            sendBroadcast(closeDialog);
        }
    }

    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        if (this.blockedKeys.contains(Integer.valueOf(keyEvent.getKeyCode()))) {
            return true;
        }
        return super.dispatchKeyEvent(keyEvent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
