package com.stony.map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.geometry.*;
import com.stony.map.controllers.ServiceController;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>s2-geometry-library
 * <p>google.common.geometry.test
 *
 * @author stony
 * @version 下午5:23
 * @since 2018/2/15
 */
public class RegionTest extends BaseTest{

    @Test
    public void test_s2cover(){

        String points = "39.9575435703,116.2890994424,39.9483901277,116.373948784,39.9477148497,116.4276618249";
        String[] points_vector = points.split(",");
        List<S2Point> s2points_vector = new ArrayList<>(16);
        ArrayList<S2CellId> cellids_vector = new ArrayList<>(128);

        int min_level = 1;
        int max_level = 30;
        int max_cells = 30;
        int level_mod = 1;
        if (min_level > S2CellId.MAX_LEVEL) {
            min_level = S2CellId.MAX_LEVEL;
        }
        if(min_level < 0) {
            min_level = 0;
        }
        if (max_level > S2CellId.MAX_LEVEL) {
            max_level = S2CellId.MAX_LEVEL;
        }
        if(max_level < 0) {
            max_level = 0;
        }


        for (int i = 0; i < points_vector.length; i += 2) {
            double lat = Double.valueOf(points_vector[i]);
            double lng = Double.valueOf(points_vector[i+1]);
            s2points_vector.add(S2LatLng.fromDegrees(lat, lng).toPoint());
        }
        S2PolygonBuilder.Options options = S2PolygonBuilder.Options.DIRECTED_XOR;

        S2PolygonBuilder builder = new S2PolygonBuilder(options);




        if (s2points_vector.size() == 1) {
            System.out.println("----------size 1");
            for (int i = min_level; i <= max_level; i += level_mod) {
                cellids_vector.add(S2CellId.fromPoint(s2points_vector.get(0)).parent(i));
            }
        } else {
            System.out.println("  ------------- size = " + s2points_vector.size());
            for (int i = 0; i < s2points_vector.size(); i++) {
                int d = (i + 1) % s2points_vector.size();
                builder.addEdge(s2points_vector.get(i), s2points_vector.get(d));
//                builder.addEdge(
//                        s2points_vector[i],
//                        s2points_vector[(i + 1) % s2points_vector.size()]);
            }
            S2Polygon polygon = new S2Polygon();
            List<S2Edge> unusedEdges = Lists.newArrayList();
            builder.assemblePolygon(polygon, unusedEdges);

            S2RegionCoverer coverer = new S2RegionCoverer();
            coverer.setLevelMod(level_mod);
            coverer.setMaxCells(max_cells);
            coverer.setMinLevel(min_level);
            coverer.setMaxLevel(max_level);

            coverer.getCovering(polygon, cellids_vector);
        }

        //打印json
        s2CellIdsToJson(cellids_vector);


    }
    @Test
    public void test_s2info() throws JsonProcessingException {
        String ids = "3887077838196572160,3887077830680379392,3886698661303812096";
        String[] ids_vector = ids.split(",");
        ArrayList<S2CellId> cellids_vector = new ArrayList<>(128);
        for (int i = 0; i < ids_vector.length; i++) {
            cellids_vector.add(new S2CellId(Long.parseLong(ids_vector[i])));
        }
        //打印json
        s2CellIdsToJson(cellids_vector);

        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writeValueAsString(ServiceController.getMapResult(cellids_vector)));
    }


    void s2CellIdsToJson(List<S2CellId> cellids_vector){
        System.out.printf("[");
        for (int i = 0; i < cellids_vector.size(); i++) {
            S2Cell cell = new S2Cell(cellids_vector.get(i));
            S2LatLng center = new S2LatLng(cell.getCenter());
            System.out.printf("{ id: %d, id_signed: %d, token: %s, pos: %d, face: %d, level: %d, \n",
                    cell.id().id(), cell.id().id(), cell.id().toToken(),
                    cell.id().pos(), cell.id().face(), cell.id().level()
            );
            System.out.printf("ll: \n \t { lat: %s, lng: %s },\n",center.lat().degrees(), center.lng().degrees());
            System.out.printf("shape: [\n");
            for (int k = 0; k < 4; k++) {
                S2LatLng vertex = new S2LatLng(cell.getVertex(k));
                System.out.printf("\t { lat: %s, lng: %s },\n", vertex.lat().degrees(), vertex.lng().degrees());
            }
            System.out.printf("]\n");

        }
        System.out.printf("]\n");

    }

    @Test
    public void test_face(){

        double lat = 39.9477148497, lng = 116.4276618249;

        S2LatLng s2LatLng = S2LatLng.fromDegrees(lat, lng);

        S2CellId cellId = S2CellId.fromLatLng(s2LatLng);
        S2Cell cell = new S2Cell(cellId);
        S2CellId pid = cellId.parent(11);

        System.out.println("pid = " + pid.id());
        System.out.println("pid = " + cellId.parent(12).id());
        S2CellId faceNbrs[] = new S2CellId[4];
        pid.getEdgeNeighbors(faceNbrs);

        ArrayList<S2CellId> vertex = new ArrayList<S2CellId>();
        ArrayList<S2CellId> all = new ArrayList<S2CellId>();
        List<S2CellId> edge = Arrays.<S2CellId>asList(faceNbrs);

        System.out.println("face neighbors");
        String result = edge.stream().map(S2CellId::id).map(String::valueOf).collect(Collectors.joining(","));
        System.out.println(result);


        S2Cell pcell = new S2Cell(pid);
        for (int k = 0; k < 4; ++k) {
            vertex.add(S2CellId.fromPoint(pcell.getVertex(k)));
        }
        System.out.println("vertex potins");
        result = vertex.stream().map(S2CellId::id).map(String::valueOf).collect(Collectors.joining(","));
        System.out.println(result);

        vertex.clear();
        System.out.println("vertex neighbors");
        pid.getVertexNeighbors(11, vertex);
        result = vertex.stream().map(S2CellId::id).map(String::valueOf).collect(Collectors.joining(","));
        System.out.println(result);

        System.out.println("all neighbors");
        pid.getAllNeighbors(11, all);
        result = all.stream().map(S2CellId::id).map(String::valueOf).collect(Collectors.joining(","));
        System.out.println(result);
    }


    @Test
    public void test_level() {

        System.out.println("MIN_WIDTH 800 米--------");
        System.out.println(S2Projections.MIN_WIDTH.getMinLevel(earthMetersToRadians(800)));
        System.out.println(S2Projections.MIN_WIDTH.getMaxLevel(earthMetersToRadians(800)));
        System.out.println(S2Projections.MIN_WIDTH.getClosestLevel(earthMetersToRadians(800)));

        System.out.println("AVG_WIDTH 800 米--------");
        System.out.println(S2Projections.AVG_WIDTH.getMinLevel(earthMetersToRadians(1800)));
        System.out.println(S2Projections.AVG_WIDTH.getMaxLevel(earthMetersToRadians(1800)));
        System.out.println(S2Projections.AVG_WIDTH.getClosestLevel(earthMetersToRadians(1800)));

        System.out.println("MAX_WIDTH 800 米--------");
        System.out.println(S2Projections.MAX_WIDTH.getMinLevel(earthMetersToRadians(800)));
        System.out.println(S2Projections.MAX_WIDTH.getMaxLevel(earthMetersToRadians(800)));
        System.out.println(S2Projections.MAX_WIDTH.getClosestLevel(earthMetersToRadians(800)));


        System.out.println("MAX_WIDTH2 800 米--------");
        System.out.println(S2Projections.MAX_WIDTH.getMinLevel(S2Earth.MetersToRadians(800)));
        System.out.println(S2Projections.MAX_WIDTH.getMaxLevel(S2Earth.MetersToRadians(800)));
        System.out.println(S2Projections.MAX_WIDTH.getClosestLevel(S2Earth.MetersToRadians(800)));




        System.out.println("MAX_WIDTH 1500 米--------");
        System.out.println(S2Projections.MAX_WIDTH.getMinLevel(earthMetersToRadians(1500)));
        System.out.println(S2Projections.MAX_WIDTH.getMaxLevel(earthMetersToRadians(1500)));
        System.out.println(S2Projections.MAX_WIDTH.getClosestLevel(earthMetersToRadians(1500)));


        System.out.println("MAX_WIDTH2 1500 米--------");
        System.out.println(S2Projections.MAX_WIDTH.getMinLevel(S2Earth.MetersToRadians(1500)));
        System.out.println(S2Projections.MAX_WIDTH.getMaxLevel(S2Earth.MetersToRadians(1500)));
        System.out.println(S2Projections.MAX_WIDTH.getClosestLevel(S2Earth.MetersToRadians(1500)));
    }

}