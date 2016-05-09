package com.android.hikepeaks.Models;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root
public class TrailPoint {
    @Element(required = false)
    private double latitude;
    @Element(required = false)
    private double longitude;

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

}
