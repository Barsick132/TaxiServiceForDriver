package com.lstu.kovalchuk.taxiservicefordriver;

import android.location.Location;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import java.util.Date;

public class DriverGPS {
    private Timestamp DT;
    private GeoPoint geoPoint;

    public DriverGPS(Date date, Location location){
        DT = new Timestamp(date);
        geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
    }

    public Timestamp getDT() {
        return DT;
    }

    public void setDT(Timestamp DT) {
        this.DT = DT;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }
}
