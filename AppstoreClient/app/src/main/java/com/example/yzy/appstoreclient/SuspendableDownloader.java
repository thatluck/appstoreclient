package com.example.yzy.appstoreclient;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by yangzhongyu on 15-3-4.
 */
public class SuspendableDownloader {

    private  static  final String SDPATH = "//sdcard//";
    private  static  final String TAG  = "SuspendableDownloader";
    public boolean isStopDownload = false;

    public void startDownload() {
        isStopDownload = false;
    }

    public void stopDownload(){
        isStopDownload = true;
    }


    public interface CallBack{
        public boolean notfiyProgress(int percent);
        public void onDownLoadCancel();
        public void onDownLoadDone();
    }
    private   CallBack mCallBack = null;

    public  void setCallBack(CallBack callback){
        this.mCallBack = callback;

    }


    public  String downLoadFile(String httpUrl) throws IOException {
        File tmpFile = new File(SDPATH);
        if (!tmpFile.exists()) {
            tmpFile.mkdir();
        }
        String fileName = httpUrl.split("/")[httpUrl.split("/").length-1];

        final RandomAccessFile file = new RandomAccessFile(SDPATH + fileName,"rwd");

        //第一次下载
        if (file.length() == 0) {

            URL url = new URL(httpUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            int  length = conn.getContentLength();

            InputStream is = conn.getInputStream();
            //FileOutputStream fos = new FileOutputStream(file);
            byte[] buf = new byte[256*2];
            conn.connect();
            double count = 0;
            if (conn.getResponseCode() >= 400) {
                 Log.i(TAG,"time exceed");
            } else {
                while (count <= 100 && !isStopDownload) {
                    if (is != null) {
                        int numRead = is.read(buf);


                        if (numRead <= 0) {
                            break;
                        } else {
                            file.write(buf, 0, numRead);
                            mCallBack.notfiyProgress((int)(file.length()*100/length));
                            Log.d(TAG,"notifyprogress="+(int)(file.length()*100/length));
                        }
                    } else {
                        break;
                    }
                }
                if (isStopDownload) {
                    mCallBack.onDownLoadCancel();
                } else {
                    mCallBack.onDownLoadDone();
                }

            }
            conn.disconnect();
            file.close();
            is.close();
        }else {
            //不是第一次下载，之前有过下载
            Log.d(TAG,"continue file.length() ="+file.length()  );
        //    file.seek(file.length());

            URL url = new URL(httpUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            int length = conn.getContentLength();


            HttpURLConnection conn2 = (HttpURLConnection) url.openConnection();

            conn2.setRequestProperty("Range", "bytes="+file.length()+"-"+(length-1)); //如果文件已经下载完成，即从 1234-1234 会报告FileNotFound Exception

            //java.lang.IllegalStateException: Cannot set request property after connection is made

          //  int length = conn.getContentLength();http://www.eoeandroid.com/thread-154241-1-1.html

            if (file.length() == length) {
                 return SDPATH + fileName;
            }

            // file.setLength(length);

            InputStream is = conn.getInputStream();
            //FileOutputStream fos = new FileOutputStream(file);
            byte[] buf = new byte[256*2];
            conn.connect();
            double count = 0;

            if (conn.getResponseCode() >= 400) {
                 Log.i(TAG,"time exceed");
            } else {
                while (count <= 100 && !isStopDownload) {
                    if (is != null) {
                        int numRead = is.read(buf);
                        if (numRead <= 0) {

                            break;
                        } else {
                            file.write(buf, 0, numRead);
                            mCallBack.notfiyProgress((int)(file.length()*100/length));
                            Log.d(TAG,"notifyprogress="+(int)(file.length()*100/length));
                        }
                    } else {
                        break;
                    }
                }
                if (isStopDownload) {
                    mCallBack.onDownLoadCancel();
                } else {
                    mCallBack.onDownLoadDone();
                }

            }
            conn.disconnect();
            file.close();
            is.close();


        }

        return SDPATH + fileName;
    }

}
