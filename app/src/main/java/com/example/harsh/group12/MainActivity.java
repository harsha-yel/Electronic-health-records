package com.example.harsh.group12;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteException;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static com.example.harsh.group12.R.layout.activity_main;

public class MainActivity extends AppCompatActivity {

    private float mLastX, mLastY, mLastZ;
    String[] horlabels = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
    String[] verlabels = new String[]{"10", "9", "8", "7", "6", "5", "4", "3", "2", "1"};
    float[] values1 = new float[10];
    float[] values2 = new float[10];
    float[] values3 = new float[10];
    boolean rad=false;
    String tname="harsha_25_21_male";
    Timestamp currentTime = null;
    int on=0;

    private static final int PERMS_REQUEST_CODE = 123;
    TextView messageText;
    Button uploadButton;
    Button downloadButton;
        int serverResponseCode = 0;
    ProgressDialog dialog = null;
    String upLoadServerUri = null;

    /**********  File Path *************/
    final String uploadFilePath = Environment.getExternalStorageDirectory()+ File.separator+"Mydatabases"+File.separator;
    final String uploadFileName = "accelerometer_data";
    final String downloadFilePath = Environment.getExternalStorageDirectory()+ File.separator+"Mydatabases"+File.separator+"downloads"+File.separator;
    final String downloadFileName = "accelerometer_data";

    List<Float> list_x = new ArrayList<Float>();
    List<Float> list_y = new ArrayList<Float>();
    List<Float> list_z = new ArrayList<Float>();

    float[] values_x;
    float[] values_y;
    float[] values_z;

    LinearLayout layout_graph;
    GraphView graphview;
    private boolean run = false;
    boolean mInitialized;
    final float NOISE = (float) 2.0;
    DBHandler db1 = new DBHandler(this);
    Thread t = new Thread();
    MyReceiver myReceiver;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (hasPermissions()) {
            // our app has permissions.
            //makeFolder();
    }
        else {
            requestPerms();
        }

        setContentView(activity_main);
        graphview = new GraphView(this, values1, values2, values3, "", horlabels, verlabels, true);
        layout_graph = (LinearLayout) findViewById(R.id.graph_app);
        layout_graph.addView(graphview);

        mInitialized = false;
        uploadButton = (Button)findViewById(R.id.uploadButton);
        downloadButton = (Button)findViewById(R.id.downloadButton);
        messageText  = (TextView)findViewById(R.id.messageText);
        /************* Php script path ****************/
        upLoadServerUri = "https://impact.asu.edu/CSE535Spring17Folder/UploadToServer.php";
        Intent intent = new Intent(this, AccelerometerService.class);
        startService(intent);
        myReceiver = new MyReceiver();
        db1.create();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AccelerometerService.MY_ACTION);
        if (on == 0) {
            on = 1;
            registerReceiver(myReceiver, intentFilter);
        }

    }
    private void requestPerms() {
        String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, PERMS_REQUEST_CODE);
        }
    }

    private boolean hasPermissions() {
        int res = 0;
        //string array of permissions,
        String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        for (String perms : permissions) {
            res = checkCallingOrSelfPermission(perms);
            if (!(res == PackageManager.PERMISSION_GRANTED)) {
                return false;
            }
        }
        return true;
    }
    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub
            boolean enab = false;
        //   if (rad) {
            if (currentTime == null) {
                currentTime = new Timestamp(new Date().getTime());
                enab = true;
            } else {
                Timestamp t = new Timestamp(new Date().getTime());
                if (t.getTime() - currentTime.getTime() > 999) {
                    //Only once in a second
                    Log.d("Time", String.valueOf(t.getTime() - currentTime.getTime()));
                    currentTime = t;
                    enab = true;
                }
            }
            if (enab) {
                float x = arg1.getFloatExtra("X", 0.0f);
                float y = arg1.getFloatExtra("Y", 0.0f);
                float z = arg1.getFloatExtra("Z", 0.0f);
                //messageText.setText(x + "," + y + "," + z);
                try {
                    table_setup table = new table_setup();
                    table.setTime_stamp(String.valueOf(currentTime));
                    table.setX_values(x);
                    table.setY_values(y);
                    table.setZ_values(z);
                    db1.addTotable(table,tname);
                } catch (SQLiteException e) {
                    //report problem
                } finally {
                    //db.endTransaction();
                }
            }

        }

    }

    int inc = 0;
    public void onClickRun(View v) throws InterruptedException {

        if (run) {
            for (int i = 0; i < values_x.length - 1; i++) {
                values_x[i] = values_x[i + 1];
                values_y[i] = values_y[i + 1];
                values_z[i] = values_z[i + 1];
            }
            List<table_setup> list123 = db1.getlastdata(tname);
            table_setup entry = list123.get(0);
            values_x[values_x.length - 1] = ((Float)(entry.getX_values())).floatValue();
            values_y[values_y.length - 1] = ((Float)(entry.getY_values())).floatValue();;
            values_z[values_z.length - 1] = ((Float)(entry.getZ_values())).floatValue();;
            return;
        }

        downloadButton.setEnabled(false);

        View v1 = this.getCurrentFocus();
        if (v1 != null) {
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(v1.getWindowToken(), 0);
        }

        EditText age_type = (EditText) findViewById(R.id.age_type);
        EditText name_type = (EditText) findViewById(R.id.name_type);
        EditText pat_id_type = (EditText) findViewById(R.id.pat_id_type);
        RadioButton mal = (RadioButton) findViewById(R.id.mal);
        RadioButton fem = (RadioButton) findViewById(R.id.fem);
        String name = name_type.getText().toString();
        String age = age_type.getText().toString();
        String id = pat_id_type.getText().toString();

        boolean rad1=false;
        //rad = false;

        String sex = null;
        if(mal.isChecked())
            sex="male";
        if (fem.isChecked())
            sex="female";
        tname= name+"_"+id+"_"+age+"_"+sex;

        if (!mal.isChecked() && !fem.isChecked()) {
            rad1 = true;
        }
        if (!(age.matches("") || id.matches("") || name.matches("") || rad1)) {
            rad=true;
            messageText.setText("Running last 10...");


            t = new Thread(new Runnable() {
                public void run() {
                    int i = 0;
                    final int sizemin, sizemax;
                    int ls = list_x.size();
                    if (ls < 10) {
                        sizemin = 0;
                        sizemax = ls;
                    } else {
                        sizemin = ls - 10;
                        sizemax = sizemin + 10;
                    }
                    values_x = new float[10];
                    values_y = new float[10];
                    values_z = new float[10];

                    //int j=sizemin-1;
                    i = 0;

                    int min = list_x.size();
                    if (min > 10)
                        min = 10;
                    for (i = 0; i < min; i++) {
                        values_x[i] = (float) list_x.get(i);
                        values_y[i] = (float) list_y.get(i);
                        values_z[i] = (float) list_z.get(i);
                        values_x[i] = (float) values_x[i];
                        values_y[i] = (float) values_y[i];
                        values_z[i] = (float) values_z[i];
                    }

                    run = true;

                    while (run) {

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                                          @Override
                                          public void run() {

                                              for (int i = 0; i < values_x.length - 1; i++) {
                                                  values_x[i] = values_x[i + 1];
                                                  values_y[i] = values_y[i + 1];
                                                  values_z[i] = values_z[i + 1];
                                              }
                                              List<table_setup> list123 = db1.getlastdata(tname);
                                              table_setup entry = list123.get(0);
                                              values_x[values_x.length - 1] = ((Float)(entry.getX_values())).floatValue();;
                                              values_y[values_y.length - 1] = ((Float)(entry.getY_values())).floatValue();;
                                              values_z[values_z.length - 1] = ((Float)(entry.getZ_values())).floatValue();;
                                              graphview.setValues(values_x, values_y, values_z);
                                              layout_graph.removeView(graphview);
                                              layout_graph.addView(graphview);

                                              try {
                                                  Thread.sleep(10);
                                              } catch (InterruptedException e) {
                                                  e.printStackTrace();
                                              }
                                          }
                                      }
                        );
                    }
                }
            }
            );
            t.start();

        } else {
            Toast.makeText(this, "Please Enter all Details", Toast.LENGTH_SHORT).show();
            return;
        }

    }

    public void onClickStop(View v1) {
        downloadButton.setEnabled(false);
        messageText.setText(" stopped  ");

        Thread stopThread = new Thread();
        {
            //Disbale Stop button if its already running and active
            if (run == false)
                return;
            View v2 = this.getCurrentFocus();
            if (v2 != null) {
                InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(v2.getWindowToken(), 0);
            }
            //If stop button is pressed
            run = false;
            try {
                Thread.sleep(750);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            layout_graph.post(new Runnable() {
                public void run() {
                    layout_graph.removeView(graphview);
                    for (int i = 0; i < values_x.length; i++) {
                        values_x[i] = 0;
                        values_y[i] = 0;
                        values_z[i] = 0;
                    }
                    layout_graph.addView(graphview);
                }
            });
        }
        stopThread.start();
    }

   // ProgressDialog dialog;
    int flag=0;
    public void onClickDownload(View v1) {

        File folder=new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Mydatabases/downloads");
        if(!(folder.exists()))
            folder.mkdir();
        if(rad==false) {
            messageText.setText("Run and upload first");
            return;
        }

        //  dialog = ProgressDialog.show(MainActivity.this, "", "Downloading file...", true);
        new Thread(new Runnable() {
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        messageText.setText("Download started.....");
                    }
                });

                downloadFile(downloadFilePath);
                downloaddisplay();

            }
        }).start();
    }

    public void downloadFile(final String sourceFileUri) {
        final String downloadErrorMsg = "Download failed.";
            try {
//Taken From "http://androidexample.com/Download_File_To_Server_-_Android_Example/index.php?view=article_discription&aid=83"
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(
                                X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(
                                X509Certificate[] certs, String authType) {
                        }
                    }
            };

            // Install the all-trusting trust manager
            try {
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            } catch (Exception e) {
                e.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
                        messageText.setText(downloadErrorMsg);
                    }
                });
            }

            // Log.d(TAG, "downloading database");
            URL url = new URL("https://impact.asu.edu/CSE535Spring17Folder/accelerometer_data");
            /* Open a connection to that URL. */
            HttpsURLConnection ucon = (HttpsURLConnection) url.openConnection();
            /*
            * Define InputStreams to read from the URLConnection.
            */
            InputStream is = ucon.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            /*
            * Read bytes to the Buffer until there is nothing more to read(-1).
            */
            ByteArrayOutputStream buffer = new ByteArrayOutputStream(50);
            int current = 0;
            while ((current = bis.read()) != -1) {
                buffer.write((byte) current);
            }

            /* Convert the Bytes read to a String. */
            FileOutputStream fos = new FileOutputStream(new File(Environment.getExternalStorageDirectory() + "/" + "Mydatabases/downloads/accelerometer_data"));
            fos.write(buffer.toByteArray());
            fos.close();
            // Log.d(TAG, "downloaded");
            runOnUiThread(new Runnable() {
                public void run() {
                    messageText.setText("File Download Completed");
                    flag=1;
                      }
            });

        } catch (IOException e) {
            e.printStackTrace();

            runOnUiThread(new Runnable() {
                public void run() {
                    messageText.setText(downloadErrorMsg);
                         }
            });

            Log.e("Download file to server", "error: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            e.printStackTrace();

            runOnUiThread(new Runnable() {
                public void run() {
                    messageText.setText(downloadErrorMsg);
                      }
            });

            Log.e("Download file to server", "error: " + e.getMessage(), e);
        } catch (Exception e) {
            e.printStackTrace();

            runOnUiThread(new Runnable() {
                public void run() {
                    messageText.setText(downloadErrorMsg);
                         }
            });
            Log.e("Download file to server", "error: " + e.getMessage(), e);
        }
    }



    public void onClickUpload(View v1) {
        downloadButton.setEnabled(true);
        dialog = ProgressDialog.show(MainActivity.this, "", "Uploading file...", true);
        new Thread(new Runnable() {
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        messageText.setText("uploading started.....");
                    }
                });

                uploadFile(uploadFilePath + uploadFileName);

            }
        }).start();
    }

    public int uploadFile(String sourceFileUri) {
//Taken From "http://androidexample.com/Download_File_To_Server_-_Android_Example/index.php?view=article_discription&aid=83"

        String fileName = sourceFileUri;
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);

        if (!sourceFile.isFile()) {
            dialog.dismiss();
            Log.e("uploadFile", "Source File not exist :"
                    +uploadFilePath + "" + uploadFileName);
            runOnUiThread(new Runnable() {
                public void run() {
                    messageText.setText("Source File not exist :"
                            +uploadFilePath + "" + uploadFileName);
                }
            });
            return 0;
        }
        else
        {
            try {

                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                TrustManager[] trustAllCerts = new TrustManager[]{
                        new X509TrustManager() {
                            public X509Certificate[] getAcceptedIssuers() {
                                return null;
                            }

                            public void checkClientTrusted(
                                    X509Certificate[] certs, String authType) {
                            }

                            public void checkServerTrusted(
                                    X509Certificate[] certs, String authType) {
                            }
                        }
                };

// Install the all-trusting trust manager
                try {
                    SSLContext sc = SSLContext.getInstance("SSL");
                    sc.init(null, trustAllCerts, new SecureRandom());
                    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                } catch (Exception e) {
                    return 0;
                }

                URL url = new URL(upLoadServerUri);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + fileName + "\"" + lineEnd);

                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : "+ serverResponseMessage + ": " + serverResponseCode);

                if(serverResponseCode == 200){

                    runOnUiThread(new Runnable() {
                        public void run() {

                            String msg = "File Upload Completed."+uploadFileName;

                            messageText.setText(msg);
                            Toast.makeText(MainActivity.this, "File Upload Complete.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {

                dialog.dismiss();
                ex.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
                        messageText.setText("MalformedURLException Exception : check script url.");
                        Toast.makeText(MainActivity.this, "MalformedURLException",
                                Toast.LENGTH_SHORT).show();
                    }
                });

                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {

                dialog.dismiss();
                e.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
                        messageText.setText("Got Exception : see logcat ");
                        Toast.makeText(MainActivity.this, "Got Exception : see logcat ",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                Log.e("Uploadserver Exception","Exception:"+ e.getMessage(), e);
            }
            dialog.dismiss();
            return serverResponseCode;

        } // End else block
    }

    public void downloaddisplay(){

        View v1 = this.getCurrentFocus();
        if (v1 != null) {
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(v1.getWindowToken(), 0);
        }

        t = new Thread(new Runnable() {
            public void run() {
                run = false;

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            float[] values_x = new float[10];
                            float[] values_y = new float[10];
                            float[] values_z = new float[10];

                            List<table_setup> list123 = db1.getlast10data();
                            for (int i = 0; i < list123.size(); i++) {
                                table_setup entry = list123.get(i);
                                values_x[i] = ((Float)(entry.getX_values())).floatValue();
                                values_y[i] = ((Float)(entry.getY_values())).floatValue();
                                values_z[i] = ((Float)(entry.getZ_values())).floatValue();
                            }
                            graphview.setValues(values_x, values_y, values_z);
                            layout_graph.removeView(graphview);
                            layout_graph.addView(graphview);


                            EditText age_type = (EditText) findViewById(R.id.age_type);
                            EditText name_type = (EditText) findViewById(R.id.name_type);
                            EditText pat_id_type = (EditText) findViewById(R.id.pat_id_type);
                            RadioButton mal = (RadioButton) findViewById(R.id.mal);
                            RadioButton fem = (RadioButton) findViewById(R.id.fem);
                            String tabl=db1.gettablename();
                            String[] tab=tabl.split("_");
                            name_type.setText(tab[0]);
                            pat_id_type.setText(tab[1]);
                            age_type.setText(tab[2]);
                            if(tab[3].equals("male"))
                                mal.setChecked(true);
                            if(tab[3].equals("female"))
                                fem.setChecked(true);

                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                            }
                }
        );
        t.start();
    }
}


















