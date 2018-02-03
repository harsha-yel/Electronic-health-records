package com.example.harsh.group12;

import android.content.Context;

/**
 * Created by Ravi Teja Pinnaka on 2/11/2017.
 */

public class table_setup {
    private String time_stamp;
    private float x_values;
    private float y_values;
    private float z_values;

    public table_setup()
    {
    }

    public table_setup(String time_stamp,float x_values,float y_values,float z_values)
    {
        this.time_stamp=time_stamp;
        this.x_values=x_values;
        this.y_values=y_values;
        this.z_values=z_values;

    }

    public String getTime_stamp() {

        return time_stamp;
    }

    public void setTime_stamp(String time_stamp) {
        this.time_stamp = time_stamp;
    }

    public float getX_values() {
        return x_values;
    }

    public void setX_values(float x_values) {
        this.x_values = x_values;
    }

    public float getY_values() {
        return y_values;
    }

    public void setY_values(float y_values) {
        this.y_values = y_values;
    }

    public float getZ_values() {
        return z_values;
    }

    public void setZ_values(float z_values) {
        this.z_values = z_values;
    }
}
