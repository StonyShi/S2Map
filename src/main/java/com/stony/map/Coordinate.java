package com.stony.map;

import com.fasterxml.jackson.annotation.*;

/**
 * Created on 2017/11/3.
 * 各地图API坐标系统比较与转换;
 * WGS84坐标系：即地球坐标系，国际上通用的坐标系。设备一般包含GPS芯片或者北斗芯片获取的经纬度为WGS84地理坐标系,
 * 谷歌地图采用的是WGS84地理坐标系（中国范围除外）;
 * GCJ02坐标系：即火星坐标系，是由中国国家测绘局制订的地理信息系统的坐标系统。由WGS84坐标系经加密后的坐标系。
 * 谷歌中国地图和搜搜中国地图采用的是GCJ02地理坐标系; BD09坐标系：即百度坐标系，GCJ02坐标系经加密后的坐标系;
 * 搜狗坐标系、图吧坐标系等，估计也是在GCJ02基础上加密而成的。 chenhua
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Coordinate {

    @JsonProperty("lat")
    private Double lat;
    @JsonProperty("lng")
    private Double lng;
    @JsonProperty("coord_type")
    private Type type;


    public Coordinate(String lat, String lng, Type typeEnmu) {
        this.lat = Double.valueOf(lat);
        this.lng = Double.valueOf(lng);
        this.type = typeEnmu;
    }


    @JsonCreator
    public Coordinate(@JsonProperty("lat") Double lat,
                      @JsonProperty("lng") Double lng,
                      @JsonProperty("coord_type") Type typeEnmu) {
        this.lat = lat;
        this.lng = lng;
        this.type = typeEnmu;
    }

    public Coordinate(Double lat, Double lng, String coordType) {
        this.lat = lat;
        this.lng = lng;
        this.type = Type.getByName(coordType);
    }
    public static Coordinate baidu(Double lat, Double lng) {
        return new Coordinate(lat, lng, Type.BAIDU);
    }
    public static Coordinate world(Double lat, Double lng) {
        return new Coordinate(lat, lng, Type.WORLD);
    }
    public static Coordinate mars(Double lat, Double lng) {
        return new Coordinate(lat, lng, Type.MARS);
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Coordinate covert(Type type) {
        return CoordinateUtils.convert(this, type);
    }

    @Override
    public String toString() {
        return "{" +
                "lat=" + lat +
                ", lng=" + lng +
                ", type=" + type.name +
                '}';
    }

    public boolean isWorld() {
        return type == Type.WORLD;
    }
    public boolean isBaidu() {
        return type == Type.BAIDU;
    }
    public boolean isMars() {
        return type == Type.MARS;
    }

    /**
     * 各地图API坐标系统比较与转换;
     * WGS84坐标系：即地球坐标系，国际上通用的坐标系。设备一般包含GPS芯片或者北斗芯片获取的经纬度为WGS84地理坐标系,
     * 谷歌地图采用的是WGS84地理坐标系（中国范围除外）;
     * GCJ02坐标系：即火星坐标系，是由中国国家测绘局制订的地理信息系统的坐标系统。由WGS84坐标系经加密后的坐标系。
     * 谷歌中国地图和搜搜中国地图采用的是GCJ02地理坐标系;
     * BD09坐标系：即百度坐标系，GCJ02坐标系经加密后的坐标系;
     * 搜狗坐标系、图吧坐标系等，估计也是在GCJ02基础上加密而成的。 chenhua
     */
    public enum Type {
        /**
         * 坐标系类型
         */
        WORLD("world", "WGS84坐标系：即地球坐标系，国际上通用的坐标系，google"),
        MARS("mars", " GCJ02坐标系：即火星坐标系，由WGS84坐标系经加密后的坐标系，高德、腾讯"),
        BAIDU("baidu"," BD09坐标系：即百度坐标系，GCJ02坐标系经加密后的坐标系");

        private String name;
        private String desc;

        Type(String name, String desc) {
            this.name = name;
            this.desc = desc;
        }

        @JsonValue
        public String getName() {
            return name;
        }
        @JsonIgnore
        public String getDesc() {
            return this.desc;
        }

        public void setName(String name) {
            this.name = name;
        }

        @JsonCreator
        public static Type getByName(String name) {
            for (Type value : Type.values()) {
                if (value.getName().equals(name)) {
                    return value;
                }
            }
            return null;
        }
        @Override
        public String toString() {
            return this.name;
        }
    }
}