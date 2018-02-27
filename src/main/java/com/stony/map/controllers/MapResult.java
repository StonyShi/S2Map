package com.stony.map.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * <p>S2-Map
 * <p>com.stony.map.controllers
 *
 * @author stony
 * @version 下午1:55
 * @since 2018/2/22
 */
public class MapResult {

    private long id;

    @JsonProperty("id_signed")
    private long signed;

    private String token;

    private long pos;

    private int face;

    private int level;

    @JsonProperty("ll")
    private MapLocation center;

    @JsonProperty("shape")
    private List<MapLocation> shape ;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSigned() {
        return signed;
    }

    public void setSigned(long signed) {
        this.signed = signed;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getPos() {
        return pos;
    }

    public void setPos(long pos) {
        this.pos = pos;
    }

    public int getFace() {
        return face;
    }

    public void setFace(int face) {
        this.face = face;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public MapLocation getCenter() {
        return center;
    }

    public void setCenter(MapLocation center) {
        this.center = center;
    }

    public List<MapLocation> getShape() {
        return shape;
    }

    public void setShape(List<MapLocation> shape) {
        this.shape = shape;
    }
}
