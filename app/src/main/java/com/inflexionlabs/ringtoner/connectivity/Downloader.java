package com.inflexionlabs.ringtoner.connectivity;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Downloader {

    private final ExecutorService downloader = Executors.newSingleThreadExecutor();

    final File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
    boolean fileExists;
//    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)+File.separator+"MyAppFolder");

    public void downloadFromURl(String url, String songName, DownloadListener downloadListener){

        downloadListener.onDownloaded(false,null);
        downloadListener.onDownloadFailed(false);
        downloadListener.uiChange(false, null);

        File f = new File(file,songName+System.currentTimeMillis()+".mp3");

        Handler handler = new Handler(Looper.getMainLooper());

        downloader.execute(() -> {

            if (!file.exists()){
                fileExists = file.mkdir();
            }else{
                fileExists = true;
            }

            if (fileExists){
                try {

                    URL url1 = new URL(url);

                    Log.d("TAG", "from URl");
                    InputStream input = new BufferedInputStream(url1.openStream());
                    OutputStream output = new FileOutputStream(f);

                    byte[] data = new byte[1024];
                    int count;
                    while ((count = input.read(data)) != -1) {

                        output.write(data, 0, count);
                    }

                    output.flush();
                    output.close();
                    input.close();

                    Log.d("TAG", "from URl@");

                    handler.post(()-> downloadListener.uiChange(true, f));
                    downloadListener.onDownloaded(true, f);
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.post(()-> downloadListener.uiChange(true, null));
                    Log.d("TAG", "from Catch "+e);
                    downloadListener.onDownloadFailed(true);
                }
            }

        });
    }

    public interface DownloadListener{
        void onDownloaded(boolean downloaded, File file);
        void onDownloadFailed(boolean failed);
        void uiChange(boolean done, File file);
    }
}
