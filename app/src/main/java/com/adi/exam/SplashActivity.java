
package com.adi.exam;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

import com.adi.exam.callbacks.IDialogCallbacks;
import com.adi.exam.common.AppPreferences;
import com.adi.exam.common.NetworkInfoAPI;
import com.adi.exam.database.App_Table;
import com.adi.exam.dialogfragments.MessageDialog;
import com.adi.exam.utils.TraceUtils;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.Iterator;

/*import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;*/

public class SplashActivity extends AppCompatActivity implements
        IDialogCallbacks {

    private NetworkInfoAPI networkInfoAPI = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splashscreen);

        String networkType = getNetWorkObject().execute("getConnectionInfo");

        if (networkType.equalsIgnoreCase("none")) {

            showokPopUp(R.drawable.pop_ic_info, getString(R.string.alert), getString(R.string.nipcyns), 1, this, false);

            return;

        }

        if (AppPreferences.getInstance(this).getFromStore("User-Agent").length() == 0) {

            WebView webview = new WebView(this);

            AppPreferences.getInstance(this).addToStore("User-Agent", webview.getSettings().getUserAgentString() + "", false);

            webview = null;

        }

        if (!AppPreferences.getInstance(this).getBooleanFromStore("exceldone")) {

            loadData();

        } else {

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {

                    lauchLoginActivity(LoginActivity.class);

                }

            }, 5000);

        }

    }

    public NetworkInfoAPI getNetWorkObject() {
        if (networkInfoAPI == null) {
            networkInfoAPI = new NetworkInfoAPI();
            networkInfoAPI.initialize(this);
        }
        return networkInfoAPI;
    }

    @Override
    public void onOK(int requestId) {

        switch (requestId) {

            case 1:

                SplashActivity.this.finish();

                break;

            case 2:

                break;

        }

    }

    @Override
    public void onCancel(int requestId) {
        switch (requestId) {

            case 1:

                break;

            case 2:

                break;
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

    public void lauchLoginActivity(Class<?> cls) {

        Intent intent = new Intent(this, cls);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(intent);

        SplashActivity.this.finish();

    }

    public void readExcelFileFromAssets(String[] columnNames, int sheetNameIndex, String tableName, App_Table table) {

        try {

            InputStream myInput;

            // initialize asset manager
            AssetManager assetManager = getAssets();

            //  open excel sheet
            myInput = assetManager.open("data.xls");

            // Create a POI File System object
            POIFSFileSystem myFileSystem = new POIFSFileSystem(myInput);

           /* // Finds the workbook instance for XLSX file
            XSSFWorkbook myWorkBook = new XSSFWorkbook(myInput);

            // Return first sheet from the XLSX workbook
            XSSFSheet mySheet = myWorkBook.getSheetAt(0);*/

            // Create a workbook using the File System
            HSSFWorkbook myWorkBook = new HSSFWorkbook(myFileSystem);

            // Get the first sheet from workbook
            HSSFSheet mySheet = myWorkBook.getSheetAt(sheetNameIndex);

            // We now need something to iterate through the cells.
            Iterator<Row> rowIter = mySheet.rowIterator();

            int rowno = 0;

            //textView.append("\n");

            JSONArray jsonArray = new JSONArray();

            while (rowIter.hasNext()) {

                TraceUtils.logE("row no", rowno + "");

                HSSFRow myRow = (HSSFRow) rowIter.next();

                Iterator<Cell> cellIter = myRow.cellIterator();

                JSONObject jsonObject = new JSONObject();

                int cellIndex = 0;

                while (cellIter.hasNext()) {

                    HSSFCell myCell = (HSSFCell) cellIter.next();

                    String data = myCell.toString();

                    TraceUtils.logE("Cell value", data);

                    jsonObject.put(columnNames[cellIndex], data);

                    ++cellIndex;

                }

                rowno++;

                jsonArray.put(jsonObject);

            }

            if (jsonArray.length() > 0) {

                table.insertMultipleRecords(jsonArray, tableName);

                AppPreferences.getInstance(this).addBooleanToStore("exceldone", true, false);

            }

        } catch (Exception e) {

            TraceUtils.logException(e);

        }

    }

    private void loadData() {

        try {

            new Thread(new Runnable() {

                @Override
                public void run() {

                    try {

                        App_Table table = new App_Table(SplashActivity.this);

                        String[] cloumnsNames = table.getColumnNames("STUDENTS");
                        readExcelFileFromAssets(cloumnsNames, 0/*"student"*/, "STUDENTS", table);


                        cloumnsNames = table.getColumnNames("TOPICS");
                        readExcelFileFromAssets(cloumnsNames, 1/*"TOPICS"*/, "TOPICS", table);


                        cloumnsNames = table.getColumnNames("LESSONS");
                        readExcelFileFromAssets(cloumnsNames, 2/*"student"*/, "LESSONS", table);


                        cloumnsNames = table.getColumnNames("QUESTIONS");
                        readExcelFileFromAssets(cloumnsNames, 3/*"student"*/, "QUESTIONS", table);


                        cloumnsNames = table.getColumnNames("COURSES");
                        readExcelFileFromAssets(cloumnsNames, 4/*"student"*/, "COURSES", table);


                    } catch (Exception ex) {

                        TraceUtils.logException(ex);

                    } finally {

                        runOnUiThread(new Runnable() {
                            public void run() {
                                lauchLoginActivity(LoginActivity.class);
                            }
                        });

                        /*new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                lauchLoginActivity(LoginActivity.class);
                            }
                        }, 100);*/

                    }
                }
            }).start();

        } catch (Exception e) {

            TraceUtils.logException(e);

        }

    }

}

