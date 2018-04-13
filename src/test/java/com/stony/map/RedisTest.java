package com.stony.map;

import com.google.common.geometry.S2CellId;
import com.google.common.geometry.S2LatLng;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * <p>S2-Map
 * <p>com.stony.map
 *
 * @author stony
 * @version 下午2:58
 * @since 2018/4/2
 */
public class RedisTest {

    @Test
    public void test_14() {
        Jedis jedis = new Jedis("localhost", 6379);
        jedis.set("xxa", "123");
        System.out.println(jedis.get("xxa"));

        jedis.del("xxa");
        jedis.close();

    }
    @Test
    public void test_reader() throws Exception {
        Files.lines(Paths.get(RedisTest.class.getResource("/position_20180401.txt").toURI()))
        .map(Position::new).map(position -> {
            Coordinate coordinate = Coordinate.mars(position.lat, position.lng).covert(Coordinate.Type.WORLD);
            position.lat = coordinate.getLat();
            position.lng = coordinate.getLng();
            return position;
        })
        .forEach(System.out::println);
    }
    @Test
    public void test_import() throws IOException {
        Jedis jedis = new Jedis("localhost", 6379);
        String key_list = "pos_list";
        BufferedReader br = new BufferedReader(new InputStreamReader(
                RedisTest.class.getResourceAsStream("/pos.txt"), StandardCharsets.UTF_8));

        br.lines().onClose(() -> {
            try {
                br.close();
                jedis.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }).map(line -> {
            String[] arr = line.split(",");
            if (arr.length == 2) {
                return S2LatLng.fromDegrees(Double.valueOf(arr[0]), Double.valueOf(arr[1]));
            }
            return null;
        }).filter(s -> s != null).forEach(latlng -> {
            System.out.println(new Point(latlng).toString());
            jedis.zadd(key_list, S2CellId.fromLatLng(latlng).id(), new Point(latlng).toString());
        });
    }

    class Position {
        long uid;
        double lat, lng;
        String name;

        public Position(String line) {
            String[] arry = line.split("\t");
            this.uid = Long.valueOf(arry[0]);
            this.lat = Double.valueOf(arry[6]);
            this.lng = Double.valueOf(arry[7]);
            this.name = arry[8];
        }

        @Override
        public String toString() {
            return "Position{" +
                    "uid=" + uid +
                    ", lat=" + lat +
                    ", lng=" + lng +
                    ", name='" + name + '\'' +
                    '}';
        }
    }
    class Point {
        double lat, lng;
        long id;

        public Point(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
        }
        public Point(S2LatLng latlng) {
            this.lat = latlng.latDegrees();
            this.lng = latlng.lngDegrees();
            this.id = S2CellId.fromLatLng(latlng).id();
        }

        @Override
        public String toString() {
            return "{" +
                    "\"lat\" : " + lat +
                    ", \"lng\" : " + lng +
                    ", \"id\" : " + id +
                    '}';
        }
    }
}
