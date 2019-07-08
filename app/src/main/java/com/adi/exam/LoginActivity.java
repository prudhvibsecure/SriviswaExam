package com.adi.exam;

import android.content.Intent;
import android.os.Bundle;

import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.adi.exam.callbacks.IItemHandler;
import com.adi.exam.common.AppPreferences;
import com.adi.exam.common.AppSettings;
import com.adi.exam.common.NetworkInfoAPI;
import com.adi.exam.database.PhoneComponent;
import com.adi.exam.dialogfragments.MessageDialog;
import com.adi.exam.tasks.HTTPPostTask;
import com.adi.exam.utils.TraceUtils;

import org.json.JSONArray;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity implements IItemHandler {

    private NetworkInfoAPI network = null;

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

        findViewById(R.id.tv_register).setOnClickListener(onClick);

        findViewById(R.id.tv_login).setOnClickListener(onClick);

        findViewById(R.id.iv_settings).setOnClickListener(onClick);

        network = new NetworkInfoAPI();

        network.initialize(this);

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
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("studentDetails", studentDetails);
        LoginActivity.this.finish();
        startActivity(intent);
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

            if(jsonObject.has("student_details")) {

                JSONObject jsonObject1 = jsonObject.getJSONObject("student_details");

                AppPreferences.getInstance(this).addToStore("studentDetails", jsonObject1.toString(), false);

                launchProfileActivity(jsonObject1.toString());

                return;

            }

        }

        showokPopUp(getString(R.string.errorTxt), getString(R.string.ic));

    }

}
