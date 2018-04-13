package com.stony.map;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * 坐标转换的工具类
 * WGS84，GCJ02， BD09 坐标转换
 */
public class CoordinateUtils {

    private static final double PI = 3.1415926535897932384626;
    /**
     * 地图半径
     */
    private static final double EARTH_R = 6378245.0;
    /**
     * 第一偏心率平方
     */
    private static final double EE = 0.00669342162296594323;

    private static final double X_PI = PI * 3000.0 / 180.0;

    public static Map<Pair<Coordinate.Type, Coordinate.Type>, Function<Coordinate, Coordinate>> trans = new HashMap<>(8, 0.9F);

    static {

        // trans to mars
        trans.put(new Pair<>(Coordinate.Type.BAIDU, Coordinate.Type.MARS),
                coordinate -> baidu2Mars(coordinate.getLat(), coordinate.getLng()));
        trans.put(new Pair<>(Coordinate.Type.WORLD, Coordinate.Type.MARS),
                coordinate -> world2Mars(coordinate.getLat(), coordinate.getLng()));

        // trans to world
        trans.put(new Pair<>(Coordinate.Type.BAIDU, Coordinate.Type.WORLD),
                coordinate -> baidu2World(coordinate.getLat(), coordinate.getLng()));
        trans.put(new Pair<>(Coordinate.Type.MARS, Coordinate.Type.WORLD),
                coordinate -> mars2World(coordinate.getLat(), coordinate.getLng()));

        // trans to baidu
        trans.put(new Pair<>(Coordinate.Type.MARS, Coordinate.Type.BAIDU),
                coordinate -> mars2Baidu(coordinate.getLat(), coordinate.getLng()));
        trans.put(new Pair<>(Coordinate.Type.WORLD, Coordinate.Type.BAIDU),
                coordinate -> mars2Baidu(coordinate.getLat(), coordinate.getLng()));
    }

    /**
     * 坐标转换
     *
     * @param coordinate    lat 纬度,lng 经度
     * @param type 目标坐标类型
     * @return 坐标数据
     */
    public static Coordinate convert(Coordinate coordinate, Coordinate.Type type) {
        if (coordinate.getType() == type) {
            return coordinate;
        }
        Pair<Coordinate.Type, Coordinate.Type> key = new Pair<>(coordinate.getType(), type);
        return trans.get(key).apply(coordinate);
    }

    /**
     * 国际坐标转为火星坐标
     *
     * @param lat 纬度
     * @param lon 经度
     * @return 火星坐标
     */
    public static Coordinate world2Mars(double lat, double lon) {
        if (outOfChina(lat, lon)) {
            return new Coordinate(lat, lon, Coordinate.Type.MARS);
        }
        double dLat = transformLat(lon - 105.0, lat - 35.0);
        double dLon = transformLon(lon - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * PI;
        double magic = Math.sin(radLat);
        magic = 1 - EE * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((EARTH_R * (1 - EE)) / (magic * sqrtMagic) * PI);
        dLon = (dLon * 180.0) / (EARTH_R / sqrtMagic * Math.cos(radLat) * PI);
        double marsLat = lat + dLat;
        double marsLon = lon + dLon;

        return new Coordinate(marsLat, marsLon, Coordinate.Type.MARS);
    }

    /**
     * 火星坐标系转换为世界坐标系
     *
     * @param lat 纬度
     * @param lon 经度
     * @return 世界坐标
     */
    public static Coordinate mars2World(double lat, double lon) {
        Coordinate gps = transform(lat, lon);
        double longitude = lon * 2 - gps.getLng();
        double latitude = lat * 2 - gps.getLat();
        return new Coordinate(latitude, longitude, Coordinate.Type.WORLD);
    }

    /**
     * 火星坐标系(GCJ-02) 转换为 百度坐标系(BD-09)
     *
     * @param marsLat 纬度
     * @param marsLon 经度
     * @return 百度坐标
     */
    public static Coordinate mars2Baidu(double marsLat, double marsLon) {
        if (outOfChina(marsLat, marsLon)) {
            return new Coordinate(marsLat, marsLon, Coordinate.Type.BAIDU);
        }

        double x = marsLon, y = marsLat;
        double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * X_PI);
        double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * X_PI);
        double bdLon = z * Math.cos(theta) + 0.0065;
        double bdLat = z * Math.sin(theta) + 0.006;

        return new Coordinate(bdLat, bdLon, Coordinate.Type.BAIDU);
    }

    /**
     * 百度坐标转换为火星坐标
     *
     * @param bdLat 纬度
     * @param bdLon 经度
     * @return 火星坐标
     */
    public static Coordinate baidu2Mars(double bdLat, double bdLon) {
        if (outOfChina(bdLat, bdLon)) {
            return new Coordinate(bdLat, bdLon, Coordinate.Type.BAIDU);
        }

        double x = bdLon - 0.0065, y = bdLat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * X_PI);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * X_PI);
        double marsLon = z * Math.cos(theta);
        double marsLat = z * Math.sin(theta);

        return new Coordinate(marsLat, marsLon, Coordinate.Type.MARS);
    }

    /**
     * 百度坐标转换为世界坐标
     *
     * @param bdLat 纬度
     * @param bdLon 经度
     * @return 世界坐标
     */
    public static Coordinate baidu2World(double bdLat, double bdLon) {
        Coordinate marsCoord = baidu2Mars(bdLat, bdLon);
        Coordinate worldCoord = mars2World(marsCoord.getLat(), marsCoord.getLng());

        return worldCoord;
    }

    /**
     * 世界坐标转换为百度坐标
     *
     * @param worldLat 纬度
     * @param worldLon 经度
     * @return 百度坐标
     */
    public static Coordinate world2Baidu(double worldLat, double worldLon) {
        Coordinate marsCoord = world2Mars(worldLat, worldLon);
        Coordinate baiduCoord = mars2Baidu(marsCoord.getLat(), marsCoord.getLng());

        return baiduCoord;
    }

    /**
     * 判断地区是否不属于中国
     *
     * @param lat 纬度
     * @param lon 经度
     * @return 是否不属于中国
     */
    public static boolean outOfChina(double lat, double lon) {
        if (lon < 72.004 || lon > 137.8347) {
            return true;
        }

        if (lat < 0.8293 || lat > 55.8271) {
            return true;
        }

        return false;
    }

    /**
     * 火星坐标转世界坐标依赖
     *
     * @param lat 火星坐标的纬度
     * @param lon 火星坐标的经度
     */
    private static Coordinate transform(double lat, double lon) {
        if (outOfChina(lat, lon)) {
            return Coordinate.mars(lat, lon);
        }
        double dLat = transformLat(lon - 105.0, lat - 35.0);
        double dLon = transformLon(lon - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * PI;
        double magic = Math.sin(radLat);
        magic = 1 - EE * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((EARTH_R * (1 - EE)) / (magic * sqrtMagic) * PI);
        dLon = (dLon * 180.0) / (EARTH_R / sqrtMagic * Math.cos(radLat) * PI);
        double mgLat = lat + dLat;
        double mgLon = lon + dLon;
        return Coordinate.world(mgLat, mgLon);
    }

    /**
     * 世界坐标转火星坐标的纬度
     */
    private static double transformLat(double x, double y) {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * PI) + 40.0 * Math.sin(y / 3.0 * PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * PI) + 320 * Math.sin(y * PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    /**
     * 世界坐标转火星坐标的经度
     */
    private static double transformLon(double x, double y) {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * PI) + 40.0 * Math.sin(x / 3.0 * PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * PI) + 300.0 * Math.sin(x / 30.0 * PI)) * 2.0 / 3.0;
        return ret;
    }

    public static class Pair<F, S> {
        /**
         * first element
         */
        private final F first;
        /**
         * second element
         */
        private final S second;

        public Pair(F first, S second) {
            this.first = first;
            this.second = second;
        }

        public F getFirst() {
            return first;
        }

        public S getSecond() {
            return second;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pair<?, ?> pair = (Pair<?, ?>) o;
            return Objects.equals(first, pair.first) &&
                    Objects.equals(second, pair.second);
        }

        @Override
        public int hashCode() {
            return Objects.hash(first, second);
        }

        public static <A, B> Pair<A, B> create(A a, B b) {
            return new Pair<A, B>(a, b);
        }
    }

}
