package com.adi.exam.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.provider.Settings.Secure;
import android.util.DisplayMetrics;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Utils {

    private static Dialog dialog = null;
    private Context context;


    public static String urlEncode(String sUrl) {
        int i = 0;
        String urlOK = "";
        while (i < sUrl.length()) {
            if (sUrl.charAt(i) == ' ') {
                urlOK = urlOK + "%20";
            } else {
                urlOK = urlOK + sUrl.charAt(i);
            }
            i++;
        }
        return (urlOK);
    }
    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }
    public static String getDeviceDateTime(String dtFormat) {

        Calendar c = Calendar.getInstance();

        c.setTimeInMillis(System.currentTimeMillis());

        java.util.Date date = new java.util.Date();

        SimpleDateFormat format = new SimpleDateFormat(dtFormat);

        // Log.e("-=-=-=-=-=-=-=-=-=-=", format.format(date)+"");

        return format.format(date);

    }
    public static String getDate(long timeStamp) {

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            java.util.Date netDate = (new java.util.Date(timeStamp));
            return sdf.format(netDate);
        } catch (Exception ex) {
            return "date";
        }
    }
    public static Bitmap decodeBitmapFromFile(String filePath, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }


    public static void dismissProgress() {
        if (dialog != null)
            dialog.dismiss();
        dialog = null;
    }



    public static String getDeviceId(Context context) {
        String deviceId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
        return deviceId;
    }





    public static DisplayMetrics getDisplayMetrics(Context context) {
        Resources resources = context.getResources();
        return resources.getDisplayMetrics();
    }


    public boolean isNetworkAvailable() {

        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            return false;
        }

        NetworkInfo net = manager.getActiveNetworkInfo();
        if (net != null) {
            if (net.isConnected()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }

    }
    public static void encrypt(String image) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        // Here you read the cleartext.

//        FileInputStream fis = new FileInputStream(new File(image));
//        new File(image).delete();
//        FileOutputStream fos = new FileOutputStream(Environment.getExternalStorageDirectory().toString() + "/imag/" + "enc_"+ enc);
//        Cipher encipher = Cipher.getInstance("AES");
//        encipher.init(Cipher.ENCRYPT_MODE, skey);
//        CipherInputStream cis = new CipherInputStream(fis, encipher);
//        int read;
//        while ((read = cis.read()) != -1) {
//            fos.write((char) read);
//            fos.flush();
//        }
//        fos.close();
    }

}