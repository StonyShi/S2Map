package com.stony.map;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.geometry.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>S2-Map
 * <p>com.stony.map
 *
 * @author stony
 * @version 上午11:01
 * @since 2018/2/22
 */
public class RegionCovererTest extends BaseTest{

    @Test
    public void buildQueryContents(){
        S2LatLngRect rect = new S2LatLngRect(S2LatLng.fromDegrees(22.538464,113.9496),
                S2LatLng.fromDegrees(22.539783,113.94891));
        S2RegionCoverer coverer = new S2RegionCoverer();
        coverer.setMinLevel(8);
        coverer.setMaxLevel(15);
        coverer.setMaxCells(500);
        coverer.getCovering(rect);

        S2LatLng s2LatLng = S2LatLng.fromDegrees(22.539782, 113.94890);
        System.out.println(rect.contains(s2LatLng.toPoint()));
    }

    /**
     * 区域
     */
    @Test
    public void testQuery(){
        String locCoordinates1 = "112.030500,27.970271";
        String coordinates2 = "113.630500,28.670271";

        String[] split = locCoordinates1.split(",");
        String[] coord = coordinates2.split(",");
        S2LatLngRect rect = new S2LatLngRect(S2LatLng.fromDegrees(Double.valueOf(split[1]),Double.valueOf(split[0])),
                S2LatLng.fromDegrees(Double.valueOf(coord[1]),Double.valueOf(coord[0])));
        S2RegionCoverer coverer = new S2RegionCoverer();
        coverer.setMinLevel(8);
        coverer.setMaxLevel(15);
        coverer.setMaxCells(500);

        S2CellUnion covering = coverer.getCovering(rect);
        covering.cellIds();


        S2LatLng s2LatLng = S2LatLng.fromDegrees(28.170271 ,113.030500);
        S2Cell s2Cell = new S2Cell(S2CellId.fromPoint(s2LatLng.toPoint()));
        boolean contains = covering.contains(s2Cell);
        System.out.println(contains);

        boolean b = rect.contains(s2LatLng);
        System.out.println(b);
    }

    @Test
    public void testPolyLine(){
        String routePath = "114.025914,22.629364;114.027745,22.623408;114.028944,22.619712;114.030112,22.617669;114.033967,22.612966;114.042343,22.602816;114.046114,22.603997;114.048353,22.604289;114.049606,22.604455;114.050796,22.604898;114.051728,22.605412;114.053186,22.606647;114.054171,22.608369";
        routePath = "114.025914,22.629364;114.027745,22.623408;114.028944,22.619712;114.030112,22.617669;114.033967,22.612966;114.042343,22.602816;114.046114,22.603997;114.048353,22.604289;114.049606,22.604455;114.050796,22.604898;114.051728,22.605412;114.053186,22.606647;114.054171,22.608369;114.025514,22.629164";

        List<S2Point> s2Points = new ArrayList<>();
        parseVertices(routePath,s2Points);

        S2Polyline polyline = new S2Polyline(s2Points);
        double lat = 22.629164;
        double lgt =114.025514 ;
        S2LatLng s2LatLng = S2LatLng.fromDegrees(lat, lgt);
        S2Point targetPoint = s2LatLng.toPoint();
        System.out.println("-----!!!-----");

        int neer = polyline.getNearestEdgeIndex(targetPoint);
        System.out.println(neer);
        S2Point vertex = polyline.vertex(neer);
        S2LatLng s2LatLng1 = new S2LatLng(vertex);
        double earthDistance = s2LatLng.getEarthDistance(s2LatLng1);
        System.out.println(earthDistance);

//        System.out.println(distance.radians() * S2LatLng.EARTH_RADIUS_METERS);


        //=====
        System.out.println("-----!!!-----");
        S2Point s2Point1 = polyline.projectToEdge(targetPoint,8);
        System.out.println(s2LatLng.getEarthDistance(new S2LatLng(s2Point1)));

        //====
        System.out.println("-----!!!-----");
        int nearestEdgeIndex = polyline.getNearestEdgeIndex(vertex);
        System.out.println(nearestEdgeIndex);//第三个
        S2Point vertex1 = polyline.vertex(nearestEdgeIndex);
        S2LatLng s2LatLng2 = new S2LatLng(vertex1);
        System.out.println(s2LatLng2.getEarthDistance(new S2LatLng(vertex1)));


        System.out.println("====== get mine distance =======");
        int minIndex = polyline.getSpaceDistance(targetPoint);
        System.out.println(minIndex);

        double earthDistance1 = new S2LatLng(polyline.vertex(minIndex)).getEarthDistance(s2LatLng);
        System.out.println(earthDistance1);

    }


    @Test
    public void testDistance(){
        double lat = 22.629164;
        double lgt =114.025514 ;
        S2LatLng s2LatLng = S2LatLng.fromDegrees(lat, lgt);
        S2Point s2Point = s2LatLng.toPoint();
//        s2LatLng.getDistance()
        double earthDistance = s2LatLng.getEarthDistance(new S2LatLng(s2Point)); //单位为m
        System.out.println(earthDistance);
    }

    @Test
    public void testPolyg(){
        String str = "114.025914,22.629364;114.027745,22.623408;114.028944,22.619712;114.030112,22.617669;114.033967,22.612966;114.042343,22.602816;114.046114,22.603997;114.048353,22.604289;114.049606,22.604455;114.050796,22.604898;114.051728,22.605412;114.053186,22.606647;114.054171,22.608369";
        double lat = 22.629164;
        double lgt =114.025514 ;

        List<S2Point> vertices = Lists.newArrayList();

        for (String token : Splitter.on(';').split(str)) {
            int colon = token.indexOf(',');
            if (colon == -1) {
                throw new IllegalArgumentException(
                        "Illegal string:" + token + ". Should look like '114.139312,22.551337;114.120260,22.535537'");
            }
            double lngY = Double.parseDouble(token.substring(0, colon));
            double latX = Double.parseDouble(token.substring(colon + 1));
            vertices.add(S2LatLng.fromDegrees(latX, lngY).toPoint());
        }
        S2Loop s2Loop = new S2Loop(vertices);
        S2Polygon polygon = new S2Polygon(s2Loop);
        S2Point s2Point = S2LatLng.fromDegrees(lat, lgt).toPoint();
        boolean contains = polygon.contains(s2Point);
        System.out.println(contains);
    }

    @Test
    public void testRegionUnion(){
        String str = "114.025914,22.629364;114.027745,22.623408;114.028944,22.619712;114.030112,22.617669;114.033967,22.612966;114.042343,22.602816;114.046114,22.603997;114.048353,22.604289;114.049606,22.604455;114.050796,22.604898;114.051728,22.605412;114.053186,22.606647;114.054171,22.608369";
        double lat = 22.629164;
        double lgt =114.025514 ;

        List<S2Point> vertices = Lists.newArrayList();

        for (String token : Splitter.on(';').split(str)) {
            int colon = token.indexOf(',');
            if (colon == -1) {
                throw new IllegalArgumentException(
                        "Illegal string:" + token + ". Should look like '114.139312,22.551337;114.120260,22.535537'");
            }
            double lngY = Double.parseDouble(token.substring(0, colon));
            double latX = Double.parseDouble(token.substring(colon + 1));
            vertices.add(S2LatLng.fromDegrees(latX, lngY).toPoint());
        }

        S2Loop s2Loop = new S2Loop(vertices);
        S2Polygon polygon = new S2Polygon(s2Loop);
        S2Point s2Point = S2LatLng.fromDegrees(lat, lgt).toPoint();
        boolean contains = polygon.contains(s2Point);
        System.out.println(contains);

        String[] split = "114.025914,22.629364".split(",");
        String[] coord = "114.027745,22.623408".split(",");
        S2LatLngRect rect = new S2LatLngRect(S2LatLng.fromDegrees(Double.valueOf(split[1]),Double.valueOf(split[0])),
                S2LatLng.fromDegrees(Double.valueOf(coord[1]),Double.valueOf(coord[0])));
        S2RegionCoverer coverer = new S2RegionCoverer();
        //设置cell
        coverer.setMinLevel(8);
        coverer.setMaxLevel(15);
        coverer.setMaxCells(500);
        S2CellUnion covering = coverer.getCovering(rect);
        for (S2CellId s2CellId : covering.cellIds()) {
            boolean b = polygon.mayIntersect(new S2Cell(s2CellId));
            if (b){
                System.out.println("两个区域之间含有交集.....");
            }
        }


    }


    @Test
    public void testRect(){
        String[] split = "114.025914,22.629364".split(",");
        String[] coord = "114.027745,22.623408".split(",");
        S2LatLngRect rect = new S2LatLngRect(S2LatLng.fromDegrees(Double.valueOf(split[1]),Double.valueOf(split[0])),
                S2LatLng.fromDegrees(Double.valueOf(coord[1]),Double.valueOf(coord[0])));
        S2RegionCoverer coverer = new S2RegionCoverer();
        //设置cell
//        coverer.setMinLevel(8);
//        coverer.setMaxLevel(15);
//        coverer.setMaxCells(500);
//        S2CellUnion covering = coverer.getCovering(rect);

        double lat = 22.629164;
        double lgt =114.025514 ;

        S2LatLng s2LatLng = S2LatLng.fromDegrees(lat, lgt);
        boolean contains = rect.contains(s2LatLng.toPoint());
        System.out.println(contains);
    }

    private void parseVertices(String str, List<S2Point> vertices) {
        if (str == null) {
            return;
        }

        for (String token : Splitter.on(';').split(str)) {
            int colon = token.indexOf(',');
            if (colon == -1) {
                throw new IllegalArgumentException(
                        "Illegal string:" + token + ". Should look like '114.139312,22.551337;114.120260,22.535537'");
            }
            double lng = Double.parseDouble(token.substring(0, colon));
            double lat = Double.parseDouble(token.substring(colon + 1));
            vertices.add(S2LatLng.fromDegrees(lat, lng).toPoint());
        }
    }

    public List<S2CellId> SearchCells(double lat, double lng, double radius_meters,
                               int min_level, int max_level, int max_cells) {
        final double radius_radians = earthMetersToRadians(radius_meters);
        final S2Cap region =
                S2Cap.fromAxisHeight(S2LatLng.fromDegrees(lat, lng).normalized().toPoint(),
                        (radius_radians * radius_radians) / 2);

        S2RegionCoverer coverer = new S2RegionCoverer();
        coverer.setMinLevel(min_level);
        coverer.setMaxLevel(max_level);
        coverer.setMaxCells(max_cells);

        ArrayList<S2CellId> covering = new ArrayList<>(64);
        coverer.getCovering(region, covering);

        return covering;
    }
    public List<S2CellId> SearchCells2(double lat, double lng, double radius_meters,
                                         int min_level, int max_level, int max_cells) {

        S2Cap region = S2Cap.fromAxisHeight(S2LatLng.fromDegrees(lat, lng).normalized().toPoint(),
                radius2height(radius_meters/1000));
        S2RegionCoverer coverer = new S2RegionCoverer();
        coverer.setMinLevel(min_level);
        coverer.setMaxLevel(max_level);
        coverer.setMaxCells(max_cells);

        ArrayList<S2CellId> covering = new ArrayList<>(64);
        coverer.getCovering(region, covering);

        return covering;
    }
    public List<S2CellId> SearchCells3(double lat, double lng, double radius_meters,
                                       int min_level, int max_level, int max_cells) {


        S2Cap region = S2Cap.fromAxisAngle(S2LatLng.fromDegrees(lat, lng).normalized().toPoint(),
                S1Angle.radians(S2Earth.MetersToRadians(radius_meters)));

        S2RegionCoverer coverer = new S2RegionCoverer();
        coverer.setMinLevel(min_level);
        coverer.setMaxLevel(max_level);
        coverer.setMaxCells(max_cells);

        ArrayList<S2CellId> covering = new ArrayList<>(64);
        coverer.getCovering(region, covering);

        return covering;
    }
    @Test
    public void test_search(){
        double lat = 39.9034051944, lng = 116.4210182366;
        for(S2CellId cellId : SearchCells(lat, lng, 1000,1, 30, 200)) {
            System.out.printf("id=%s, min=%s, max=%s\n", cellId.id(), cellId.rangeMin().id(), cellId.rangeMax().id());
        }
        System.out.println("----------------");
        for(S2CellId cellId : SearchCells2(lat, lng, 1000,1, 30, 200)) {
            System.out.printf("id=%s, min=%s, max=%s\n", cellId.id(), cellId.rangeMin().id(), cellId.rangeMax().id());
        }
        System.out.println("\n\n");
        String result = SearchCells(lat, lng, 1000,1, 30, 200).stream().map(S2CellId::id).map(String::valueOf).collect(Collectors.joining(","));
        System.out.println(result);


        System.out.println("\n\n");
        result = SearchCells2(lat, lng, 1000,1, 30, 200).stream().map(S2CellId::id).map(String::valueOf).collect(Collectors.joining(","));
        System.out.println(result);

        System.out.println("\n\n");
        result = SearchCells3(lat, lng, 1000,1, 30, 200).stream().map(S2CellId::id).map(String::valueOf).collect(Collectors.joining(","));
        System.out.println(result);
    }
    @Test
    public void test_baidu_world(){
        double lat = 39.9034051944, lng = 116.4210182366;


        printCells(39.9034051944,116.4210182366); //world
        printCells(39.9108766058,116.4337317581); //baidu


        printNeighbors(39.9108766058,116.4337317581, 11, 16); //baidu

    }
    void printCells(double lat, double lng) {
        S2LatLng s2LatLng = S2LatLng.fromDegrees(lat, lng);
        S2CellId cellId = S2CellId.fromLatLng(s2LatLng);
        System.out.println(cellId.parent(18).id());
        List<S2CellId> SearchCells = SearchCells(lat, lng, 800, 14, 18, 10);
        System.out.println("search cells");
        String result = SearchCells.stream().map(S2CellId::id).map(String::valueOf).collect(Collectors.joining(","));
        System.out.println(result + "," + cellId.parent(18).id());
        System.out.println("");
    }
    void printNeighbors(double lat, double lng, int level, int showLevel) {
        S2LatLng s2LatLng = S2LatLng.fromDegrees(lat, lng);

        S2CellId cellId = S2CellId.fromLatLng(s2LatLng);
        S2CellId pid = cellId.parent(level);

        System.out.println("pid = " + pid.id());
        System.out.println("pid = " + cellId.parent(level).id());

        S2CellId faceNbrs[] = new S2CellId[4];
        pid.getEdgeNeighbors(faceNbrs);

        ArrayList<S2CellId> vertex = new ArrayList<S2CellId>();
        ArrayList<S2CellId> all = new ArrayList<S2CellId>();
        List<S2CellId> edge = Arrays.<S2CellId>asList(faceNbrs);

        System.out.println("face neighbors");
        String result = edge.stream().map(S2CellId::id).map(String::valueOf).collect(Collectors.joining(","));
        System.out.println(result + "," + cellId.parent(showLevel).id());
        System.out.println("");

        S2Cell pcell = new S2Cell(pid);
        for (int k = 0; k < 4; ++k) {
            vertex.add(S2CellId.fromPoint(pcell.getVertex(k)));
        }
        System.out.println("vertex potins");
        result = vertex.stream().map(cellId1 -> cellId1.parent(showLevel).id()).map(String::valueOf).collect(Collectors.joining(","));
        System.out.println(result + "," + cellId.parent(showLevel).id());
        System.out.println("");

        vertex.clear();
        System.out.println("vertex neighbors");
        pid.getVertexNeighbors(level, vertex);
        result = vertex.stream().map(S2CellId::id).map(String::valueOf).collect(Collectors.joining(","));
        System.out.println(result + "," + cellId.parent(showLevel).id());
        System.out.println("");

        System.out.println("all neighbors");
        pid.getAllNeighbors(level, all);
        result = all.stream().map(S2CellId::id).map(String::valueOf).collect(Collectors.joining(","));
        System.out.println(result + "," + cellId.parent(showLevel).id());
        System.out.println("");
    }
}
