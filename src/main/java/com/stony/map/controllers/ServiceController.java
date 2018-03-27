package com.stony.map.controllers;

import com.google.common.collect.Lists;
import com.google.common.geometry.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * <p>S2-Map
 * <p>com.stony.map.controllers
 *
 * @author stony
 * @version 下午1:45
 * @since 2018/2/22
 */
@Path("/api")
public class ServiceController {
    private static final Logger logger = LoggerFactory.getLogger(ServiceController.class);
    final String APPLICATION_JSON_UTF8 =  "application/json; charset=UTF-8";

    @GET
    @Path("/s2neighbors")
    @Produces(APPLICATION_JSON_UTF8)
    public List<MapResult> neighbors(@QueryParam("points") String points,
                                     @QueryParam("models") String models,
                                     @QueryParam("level") int level,
                                     @QueryParam("showLevel") int showLevel) {

        logger.info("points={}, level={}, showLevel={}, models={}", points, level, showLevel, models);

        String[] points_vector = points.split(",");
        String[] models_vector = models.split(",");

        List<S2LatLng> s2points_vector = new ArrayList<>(64);
        List<Neighbor> neighbor_vector = new ArrayList<>(8);
        ArrayList<S2CellId> cellids_vector = new ArrayList<>(64);
        if(showLevel > S2CellId.MAX_LEVEL) {
            showLevel = S2CellId.MAX_LEVEL;
        }
        if(showLevel < 0) {
            showLevel = 0;
        }
        for (int i = 0; i < models_vector.length; i++) {
            Neighbor neighbor = Neighbor.of(models_vector[i]);
            if(neighbor != null) {
                neighbor_vector.add(neighbor);
            }
        }
        for (int i = 0; i < points_vector.length; i += 2) {
            double lat = Double.valueOf(points_vector[i]);
            double lng = Double.valueOf(points_vector[i+1]);
            s2points_vector.add(S2LatLng.fromDegrees(lat, lng));
        }
        for (S2LatLng s2LatLng : s2points_vector) {
            for (Neighbor neighbor : neighbor_vector) {
                cellids_vector.addAll(getNeighborCell(s2LatLng, level, neighbor,showLevel));
            }
            if(showLevel < S2CellId.MAX_LEVEL) {
                cellids_vector.add(S2CellId.fromLatLng(s2LatLng).parent(showLevel));
            }
        }
        return getMapResult(cellids_vector);
    }

    List<S2CellId> getNeighborCell(S2LatLng s2LatLng, int level, Neighbor neighbor,int showLevel) {
        ArrayList<S2CellId> neighbors = new ArrayList<S2CellId>(16);
        S2CellId cellId = S2CellId.fromLatLng(s2LatLng);
        S2CellId pid = cellId.parent(level);
        if (neighbor == Neighbor.ALL) {
            pid.getAllNeighbors(level, neighbors);
        } else if (neighbor == Neighbor.EDGE) {
            S2CellId edgeNeighbors[] = new S2CellId[4];
            pid.getEdgeNeighbors(edgeNeighbors);
            for (S2CellId edgeNeighbor : edgeNeighbors) {
                neighbors.add(edgeNeighbor);
            }
        } else if (neighbor == Neighbor.VERTEX) {
            pid.getVertexNeighbors(level, neighbors);
        } else if (neighbor == Neighbor.POINTS) {
            S2Cell pcell = new S2Cell(pid);
            for (int k = 0; k < 4; ++k) {
                neighbors.add(S2CellId.fromPoint(pcell.getVertex(k)).parent(showLevel));
            }
        }
        return neighbors;
    }

    /**
     * points :39.9833094999,116.4758164788
     * @param min_level :15
     * @param max_level :30
     * @param max_cells :200
     * @param level_mod :1
     * @return
     */
    @GET
    @Path("/s2cover")
    @Produces(APPLICATION_JSON_UTF8)
    public List<MapResult> cover(
            @QueryParam("points") String points,
            @QueryParam("min_level") int min_level,
            @QueryParam("max_level") int max_level,
            @QueryParam("max_cells") int max_cells,
            @QueryParam("level_mod") int level_mod){

        logger.info("points={}, min_level={}, max_level={}, max_cells={}, level_mod={}",
                points, min_level, max_level, max_cells, level_mod);
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

        String[] points_vector = points.split(",");
        List<S2Point> s2points_vector = new ArrayList<>(64);
        ArrayList<S2CellId> cellids_vector = new ArrayList<>(64);

        for (int i = 0; i < points_vector.length; i += 2) {
            double lat = Double.valueOf(points_vector[i]);
            double lng = Double.valueOf(points_vector[i+1]);
            s2points_vector.add(S2LatLng.fromDegrees(lat, lng).toPoint());
        }
        S2PolygonBuilder.Options options = S2PolygonBuilder.Options.DIRECTED_XOR;

        S2PolygonBuilder builder = new S2PolygonBuilder(options);


        if (s2points_vector.size() == 1) {
            logger.info("----------size 1");
            for (int i = min_level; i <= max_level; i += level_mod) {
                cellids_vector.add(S2CellId.fromPoint(s2points_vector.get(0)).parent(i));
            }
        } else {
            logger.info("  ------------- size = {}", s2points_vector.size());
            for (int i = 0; i < s2points_vector.size(); i++) {
                int d = (i + 1) % s2points_vector.size();
                builder.addEdge(s2points_vector.get(i), s2points_vector.get(d));
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
        return getMapResult(cellids_vector);
    }

    @GET
    @Path("/s2info")
    @Produces(APPLICATION_JSON_UTF8)
    public List<MapResult> info(@QueryParam("id") String ids){
        logger.info("info ids = {}", ids);
        String[] ids_vector = ids.split(",");
        ArrayList<S2CellId> cellids_vector = new ArrayList<>(64);
        for (int i = 0; i < ids_vector.length; i++) {
            cellids_vector.add(new S2CellId(Long.parseLong(ids_vector[i])));
        }
        return getMapResult(cellids_vector);
    }

    public static List<MapResult> getMapResult(ArrayList<S2CellId> cellids_vector) {
        List<MapResult> results = new ArrayList<>(32);
        for (int i = 0; i < cellids_vector.size(); i++) {
            S2Cell cell = new S2Cell(cellids_vector.get(i));

            S2CellId cellId = cell.id();
            S2LatLng center = new S2LatLng(cell.getCenter());
            logger.debug("{ id: {}, id_signed: {}, token: %s, pos: {}, face: {}, level: {}",
                    cellId.id(), cellId.id(), cellId.toToken(),
                    cellId.pos(), cellId.face(), cellId.level()
            );
            logger.debug("center: { lat: {}, lng: {} }", center.lat().degrees(), center.lng().degrees());
            MapResult result = new MapResult();
            result.setId(cellId.id());
            result.setSigned(cellId.id());
            result.setToken(cellId.toToken());
            result.setPos(cellId.pos());
            result.setFace(cellId.face());
            result.setLevel(cellId.level());
            result.setCenter(new MapLocation(center.lat().degrees(), center.lng().degrees()));
            List<MapLocation> shape = new ArrayList<>(8);
            for (int k = 0; k < 4; k++) {
                S2LatLng vertex = new S2LatLng(cell.getVertex(k));
                shape.add(new MapLocation(vertex.lat().degrees(), vertex.lng().degrees()));
            }
            result.setShape(shape);
            logger.debug("shape : {}", shape);
            results.add(result);
        }
        return results;
    }

    enum Neighbor {
        EDGE("edge"), VERTEX("vertex"), ALL("all"), POINTS("points");
        String name;
        Neighbor(String name) {
            this.name = name;
        }
        static Neighbor of(String name) {
            for(Neighbor neighbor : Neighbor.values()) {
                if(neighbor.name.equals(name)){
                    return neighbor;
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
