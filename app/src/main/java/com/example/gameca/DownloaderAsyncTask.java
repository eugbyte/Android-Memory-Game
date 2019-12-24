package com.example.gameca;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DownloaderAsyncTask extends AsyncTask < String, Object, Void > {

    //member attributes

    //Nested interface for activity to interact with AsyncTask
    public interface ICallback {
        void setProgressBar(int progress);
        void updateBitmapLists (Bitmap bitmap);
        void removeProgressBar();
        void addSuccessfulFileUrls(String fileUrl);
        void setProgressText(int fileNum);
        void removeProgressText();
    }

    //Dependency injection
    private ICallback callback;

    //Activity is injected during the construction of new MyAsyncTask()
    public DownloaderAsyncTask(ICallback callback) {
        this.callback = callback;
    }

    @Override
    protected Void doInBackground (String...params) {

        int imageNum = 0;
        int maxImageNum = 20;

        try {
            String html = getHTML(params[0]);
            List<String> imageSrcs = getImageSrcsFromHTML(html);

            for (String imageSrc : imageSrcs) {

                //Cancel async task if requested so
                if (isCancelled())
                    break;

                //stop downloading when number of images is satisfied
                if (imageNum >= maxImageNum)
                    break;

                //String imgSrc = image.absUrl("src");
                String fileLocation = params[1] + "/" + imageNum + ".jpg";
                Bitmap bitmap = downloadImageFromImageSrc(imageSrc, fileLocation);

                //if the download is corrupt, try another image in the Elements list.
                // Do not publish index to onProgressUpdate
                if (bitmap == null){
                    continue;
                }

                //if successful...
                imageNum += 1;

                //to fill the gridview and progress bar as download progresses
                publishProgress(bitmap, (imageNum * 100) / maxImageNum, fileLocation, imageNum);
            }

        } catch (Exception error) {
            Log.v("in outer catch block:", String.valueOf(imageNum));
            error.printStackTrace();
        } finally {
            Log.v("total images",String.valueOf(imageNum));
        }
        return null;
    }

    public Bitmap downloadImageFromImageSrc(String imageSrc, String fileLocation) {

        try {
            URL url = new URL(imageSrc);
            InputStream inputStream = url.openStream();

            //download the image into the folder, and overwrite existing files
            OutputStream outputStream = new FileOutputStream(fileLocation, false);

            //download the file
            for (int b; (b = inputStream.read()) != -1;) {
                outputStream.write(b);
            }

            //form the folder, decode the file input stream into a bitmap
            File file = new File(fileLocation);
            Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file));

            //if file is corrupt, delete the corrupted file
            if (bitmap == null) {
                Log.v("bitmap is corrupted", imageSrc);
                boolean deleted = file.delete();

                inputStream.close();
                outputStream.close();
                return null;
            }

            inputStream.close();
            outputStream.close();
            return bitmap;
        } catch (Exception error) {
            error.printStackTrace();
            return null;
        }
    }

    public String getHTML (String websiteUrl) {
        try {
            URL url = new URL(websiteUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();

            InputStream inputStream = url.openStream();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            for (int b; (b = inputStream.read()) != -1;) {
                outputStream.write(b);
            }

            String html = new String(outputStream.toByteArray());
            return html;


        } catch (Exception error) {
            error.printStackTrace();
            return null;
        }
    }

    public List<String> getImageSrcsFromHTML (String html) {

        List<String> srcs = new ArrayList<String>();

        String keyword = "<img src=" + "\"";
        int startIndex = html.indexOf(keyword);
        int endIndex = html.indexOf("\"", startIndex + keyword.length() );

        while (startIndex >= 0) {
            String src = html.substring(startIndex + keyword.length(), endIndex);
            if (src.contains(".jpg") || src.contains(".png")) {
                srcs.add(src);
            }

            startIndex = html.indexOf(keyword, startIndex + keyword.length() );
            endIndex = html.indexOf("\"", startIndex + keyword.length() );
        }

        return srcs;
    }

    @Override
    public void onProgressUpdate(Object... objects) {
        Bitmap bitmap = (Bitmap)objects[0];
        int progressPercent = (int)objects[1];
        String fileLocation = (String)objects[2];
        int imageNum = (int)objects[3];

        callback.updateBitmapLists(bitmap);
        callback.addSuccessfulFileUrls(fileLocation);
        callback.setProgressBar(progressPercent);
        callback.setProgressText(imageNum);
    }

    @Override
    public void onPostExecute(Void args) {
        callback.removeProgressBar();
        callback.removeProgressText();
    }

}
