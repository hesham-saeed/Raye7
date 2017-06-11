package com.apps.hesham.raye7;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

/**
 * Created by Hesham on 06/06/2017.
 */

public class MyLocationListener implements LocationListener {
    @Override
    public void onLocationChanged(Location location) {}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}
}
