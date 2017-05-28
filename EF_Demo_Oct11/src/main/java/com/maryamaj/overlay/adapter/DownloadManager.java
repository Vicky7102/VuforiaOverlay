package com.maryamaj.overlay.adapter;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by android on 24/4/17.
 */

public class DownloadManager {

    private static DownloadManager manager;

    private static Context mContext;

    public interface DownloadListener {
        public void onDownloaded();
    }

    private DownloadManager() {
    }

    public static DownloadManager init(Context context) {
        if (manager == null) {
            synchronized (DownloadManager.class) {
                manager = new DownloadManager();
            }
        }

        mContext = context;

        return manager;
    }

    public void download(final String i_url, final DownloadListener listener) {

        new AsyncTask<Void, Void, Void>() {

            int count;

            @Override
            protected Void doInBackground(Void... voids) {

                try {
                    System.out.println("DownloadManager.doInBackground");
                    URL url = new URL(i_url);
                    URLConnection conection = url.openConnection();
                    conection.connect();
                    // getting file length
                    int lenghtOfFile = conection.getContentLength();

                    // input stream to read file - with 8k buffer
                    InputStream input = new BufferedInputStream(url.openStream(), 8192);

                    File dir = new File(mContext.getFilesDir().getAbsolutePath() + "/pdf");

                    if (!dir.exists()) {
                        dir.mkdirs();
                    }

                    String fileName = i_url.substring(i_url.lastIndexOf('/') + 1);
                    // Output stream to write file
                    OutputStream output = new FileOutputStream(dir.getAbsolutePath() + "/" + fileName);

                    byte data[] = new byte[1024];

                    long total = 0;

                    while ((count = input.read(data)) != -1) {
                        total += count;

                        // writing data to file
                        output.write(data, 0, count);
                    }

                    // flushing output
                    output.flush();

                    // closing streams
                    output.close();
                    input.close();


                } catch (Exception e) {
                    Log.e("Error: ", e.getMessage());
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                System.out.println("DownloadManager.onPostExecute");
                listener.onDownloaded();
            }
        }.execute();
    }
}
