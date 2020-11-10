package com.weatnet;

public class City {

    private int z;
    private int x;
    private int y;
    private int id;
    private String name;
    private double lon;
    private double lat;

    public City() {
    }

    public City(String name, int id, double lon, double lat, int z, int x, int y) {
        this.name = name;
        this.id = id;
        this.lon = lon;
        this.lat = lat;
        this.z = z;
        this.x = x;
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(int lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(int lat) {
        this.lat = lat;
    }

}
