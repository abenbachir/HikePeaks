package com.android.hikepeaks.Models;

import android.graphics.Bitmap;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root
public class TrailCapture {
    @Element(required = false)
    private double latitude;
    @Element(required = false)
    private double longitude;
    @Element(required = false)
    private String picturePath;
    private Bitmap pictureBitmap;


    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getPicturePath() {
        return picturePath;
    }

    public void setPicturePath(String picturePath) {
        this.picturePath = picturePath;
    }

    public Bitmap getPictureBitmap() {
        return pictureBitmap;
    }

    public void setPictureBitmap(Bitmap pictureBitmap) {
        this.pictureBitmap = pictureBitmap;
    }
}
