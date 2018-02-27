package com.stony.map.controllers;

/**
 * <p>S2-Map
 * <p>com.stony.map.controllers
 *
 * @author stony
 * @version 下午1:56
 * @since 2018/2/22
 */
public class MapLocation {

    private double lat;
    private double lng;

    public MapLocation() {
    }

    public MapLocation(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    @Override
    public String toString() {
        return "Location{" +
                "lat=" + lat +
                ", lng=" + lng +
                '}';
    }
}
