package com.adi.exam;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;

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
    public DownloadFilesToMemory(String [] urls, Context context) {
        this.down_url=urls;
        this.context=context;

    }

    @Override
    protected Object doInBackground(Object... objects) {
        try {
            for (int i = 0; i < down_url.length; i++) {
                useUrl = new URL(down_url[i]);

                try {

                    File dir = new File(Environment.getExternalStorageDirectory() + "/"
                            + "allimages");
                    if (dir.exists() == false) {
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

                    fos.close();

                } catch (Exception e) {
                    TraceUtils.logException(e);
                }
            }
        }catch (Exception e){
            TraceUtils.logException(e);
        }
        return null;
    }
}
