package com.adi.exam.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.adi.exam.callbacks.DownloadAPIs;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;

public class ApkFileDownloader extends IntentService {
    private static final String DOWNLOAD_PATH = "my_Download_path";
    private static final String DESTINATION_PATH = "my_Destination_path";
    private static final String FILE_NAME = "my_file_name";

    public ApkFileDownloader() {
        super("ApkFileDownloader");
    }

    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager notificationManager;
    private int totalFileSize;
    private String file_name_nv;


    public static Intent getDownloadService(final Context callingClassContext, final String downloadPath, final String destinationPath, String f_name) {
        return new Intent(callingClassContext, DownloadService.class)
                .putExtra(DOWNLOAD_PATH, downloadPath)
                .putExtra(DESTINATION_PATH, destinationPath)
                .putExtra(FILE_NAME, f_name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String downloadPath = intent.getStringExtra(DOWNLOAD_PATH);
        String destinationPath = intent.getStringExtra(DESTINATION_PATH);
        String f_name = intent.getStringExtra(FILE_NAME);
//        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//        notificationBuilder = new NotificationCompat.Builder(this, getString(R.string.notification_channel_id))
//                .setSmallIcon(R.mipmap.ic_download_file)
//                .setContentTitle(f_name)
//                .setContentText("Downloading File...")
//                .setAutoCancel(true);
//        notificationManager.notify(0, notificationBuilder.build());

        initDownload(downloadPath, destinationPath, f_name);

    }

    private void initDownload(String downloadPath, String destinationPath, String f_name) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://bsecuresoftechsolutions.com/viswa/assets/upload/version/")
                .build();
      //  https://bsecuresoftechsolutions.com/viswa/assets/upload/version/viswa_1.2.apk
        DownloadAPIs retrofitInterface = retrofit.create(DownloadAPIs.class);
        Call<ResponseBody> request = retrofitInterface.downloadFile("https://bsecuresoftechsolutions.com/viswa/assets/upload/version/" + f_name);
        try {
            downloadFile(request.execute().body(), f_name, downloadPath);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();

        }
    }

    private void downloadFile(ResponseBody body, String f_name, String downloadPath) throws IOException {

        int count;
        byte data[] = new byte[1024 * 4];
        long fileSize = body.contentLength();
        InputStream bis = new BufferedInputStream(body.byteStream(), 1024 * 8);
        File outputFile = new File(Environment.getExternalStorageDirectory()
                .toString(), f_name);
        OutputStream output = new FileOutputStream(outputFile);
        long total = 0;
        long startTime = System.currentTimeMillis();
        int timeCount = 1;
        while ((count = bis.read(data)) != -1) {

            total += count;
            totalFileSize = (int) (fileSize / (Math.pow(1024, 2)));
            double current = Math.round(total / (Math.pow(1024, 2)));

            int progress = (int) ((total * 100) / fileSize);

            long currentTime = System.currentTimeMillis() - startTime;

            Download download = new Download();
            download.setTotalFileSize(totalFileSize);

            if (currentTime > 1000 * timeCount) {

                download.setCurrentFileSize((int) current);
                download.setProgress(progress);
                // sendNotification(download);
                timeCount++;
            }

            output.write(data, 0, count);
        }
        onDownloadComplete(f_name, downloadPath);
        output.flush();
        output.close();
        bis.close();

    }

    private void sendNotification(Download download) {

//        sendIntent(download);
//        notificationBuilder.setProgress(100, download.getProgress(), false);
//        notificationBuilder.setContentText("Downloading file " + download.getCurrentFileSize() + "/" + totalFileSize + " MB");
//        notificationManager.notify(0, notificationBuilder.build());
    }

    private void sendIntent(Download download) {
////
//        Intent intent = new Intent(ChatSingle.MESSAGE_PROGRESS);
//        intent.putExtra("download", download);
//        LocalBroadcastManager.getInstance(DownloadService.this).sendBroadcast(intent);
    }

    private void onDownloadComplete(String file_name, String downloadPath) {

//        Download download = new Download();
//        download.setProgress(100);
//        sendIntent(download);
//
//        notificationManager.cancel(0);
//        notificationBuilder.setProgress(0, 0, false);
//        notificationBuilder.setContentText("File Downloaded");
//        notificationManager.notify(0, notificationBuilder.build());

        file_name_nv=file_name;
        getstartshow(file_name_nv);
    }

    private void getstartshow(String file_name_nv) {
        try {

            Intent task=new Intent("com.attach.apk");
            task.putExtra("attachname",file_name_nv);
            sendBroadcast(task);
//            String type = Utils.getMimeType(file_name_nv);
//            Intent intent = new Intent(Intent.ACTION_VIEW);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.setData(Uri.parse(ContentValues.Images));
//            intent.setDataAndType(Uri.parse(ContentValues.Images), type);
//            getApplicationContext().startActivity(Intent.createChooser(intent, "Open with"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        notificationManager.cancel(0);
    }

}
