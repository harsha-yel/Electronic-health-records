package com.example.harsh.group12;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;

public class AccelerometerService extends Service implements SensorEventListener {
    private SensorManager accelManage;
    private Sensor senseAccel;
    float x_values = 0.0f;
    float y_values = 0.0f;
    float z_values = 0.0f;
    int index = 0;

    final static String MY_ACTION = "MY_ACTION";

    @Override
    public void onCreate() {

        accelManage = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senseAccel = accelManage.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accelManage.registerListener(this, senseAccel, 1000000);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub


        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;


        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            index++;
            x_values = event.values[0];
            y_values = event.values[1];
            z_values = event.values[2];
            Intent intent = new Intent();
            intent.setAction(MY_ACTION);

            intent.putExtra("X", x_values);
            intent.putExtra("Y", y_values);
            intent.putExtra("Z", z_values);

            sendBroadcast(intent);
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}



