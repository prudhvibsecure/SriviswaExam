package com.adi.exam;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import com.adi.exam.callbacks.IItemHandler;
import com.adi.exam.common.AppPreferences;
import com.adi.exam.tasks.ImageProcesser;
import com.adi.exam.utils.TraceUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;


public class DownloadFilesToMemory extends AsyncTask {
    HttpURLConnection connection = null;
    Context context;
    String down_url[];
    URL useUrl;

    public DownloadFilesToMemory(String[] urls, Context context) {
        this.down_url = urls;
        this.context = context;

    }

    @Override
    protected Object doInBackground(Object... objects) {
        try {
            for (int i = 0; i < down_url.length; i++) {
                useUrl = new URL(down_url[i]);

                try {

                    File dir = new File(Environment.getExternalStorageDirectory() + "/"
                            + "allimages/");
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    File file = new File(dir, useUrl.getFile());

                    URLConnection ucon = useUrl.openConnection();
                    InputStream is = ucon.getInputStream();
                    BufferedInputStream bis = new BufferedInputStream(is);
                    FileOutputStream fos = new FileOutputStream(file);
                    int current = 0;
                    while ((current = bis.read()) != -1) {
                        fos.write(current);
                    }

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            processImages();
                        }
                    });

                    fos.close();

                } catch (Exception e) {
                    TraceUtils.logException(e);
                }
            }
        } catch (Exception e) {
            TraceUtils.logException(e);
        }
        return null;
    }

    private void processImages() {

        try {
            final String PATH = Environment.getExternalStorageDirectory() + "/allimages/";
            ImageProcesser imageProcesser = new ImageProcesser(context, new IItemHandler() {

                @Override
                public void onFinish(Object results, int requestId) {

                    Toast.makeText(context, "Completed", Toast.LENGTH_LONG).show();
                    File ff=new File(PATH);
                    ff.delete();

                }

                @Override
                public void onError(String errorCode, int requestId) {
                    Toast.makeText(context, errorCode, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onProgressChange(int requestId, Long... values) {

                }

            });



            imageProcesser.startProcess(1, PATH);

        } catch (Exception e) {

            TraceUtils.logException(e);

        }

    }

}
