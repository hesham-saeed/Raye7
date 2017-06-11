package com.apps.hesham.raye7.DirectionsAPI;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;


public class Route {
    private Distance distance;
    private Duration duration;
    private String endAddress;
    private LatLng endLocation;
    private String startAddress;
    private LatLng startLocation;
    private String RouteName;
    private List<LatLng> points;
    private boolean chosen = false;

    public Distance getDistance() {
        return distance;
    }

    public void setDistance(Distance distance) {
        this.distance = distance;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public String getEndAddress() {
        return endAddress;
    }

    public void setEndAddress(String endAddress) {
        this.endAddress = endAddress;
    }

    public LatLng getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(LatLng endLocation) {
        this.endLocation = endLocation;
    }

    public String getStartAddress() {
        return startAddress;
    }

    public void setStartAddress(String startAddress) {
        this.startAddress = startAddress;
    }

    public LatLng getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(LatLng startLocation) {
        this.startLocation = startLocation;
    }

    public String getRouteName() {
        return RouteName;
    }

    public void setRouteName(String routeName) {
        RouteName = routeName;
    }

    public List<LatLng> getPoints() {
        return points;
    }

    public void setPoints(List<LatLng> points) {
        this.points = points;
    }

    public boolean isChosen() {
        return chosen;
    }

    public void setChosen(boolean chosen) {
        this.chosen = chosen;
    }
}
