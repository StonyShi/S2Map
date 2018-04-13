package com.stony.map;

import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.admin.indices.template.get.GetIndexTemplatesResponse;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateResponse;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * <p>S2-Map
 * <p>com.stony.map
 *
 * @author stony
 * @version 上午9:58
 * @since 2018/4/4
 */
public class EsTest {
    private static final Logger logger = LoggerFactory.getLogger(EsTest.class);
    @Test
    public void test_14() throws Exception {

        Settings settings = Settings.builder()
                .put("cluster.name", "my-es")
                .put("client.transport.sniff", true)
                .put("client.transport.ignore_cluster_name", true)
                .build();
        TransportClient client = new PreBuiltTransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("10.0.11.55"), 9300))
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("10.0.11.75"), 9300))
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("10.0.11.77"), 9300));

        BulkProcessor bulkProcessor = BulkProcessor.builder(
                client,
                new BulkProcessor.Listener() {
                    @Override
                    public void beforeBulk(long executionId, BulkRequest request) {
                        logger.info("Bulk Execution going [{}] >> actions : {}", executionId, request.numberOfActions());
                    }

                    @Override
                    public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
                        String index = "";
                        if (!request.requests().isEmpty()) {
                            DocWriteRequest request1 = request.requests().get(0);
                            index = request1.index();
                        }
                        if (!response.hasFailures()) {
                            logger.info("Bulk Execution completed [{}] >> Took (ms): {}, Count: {}, Index: {}",
                                    executionId,
                                    response.getTookInMillis(),
                                    response.getItems().length, index);
                        } else {
                            logger.warn("Bulk Execution completed [{}] >> Took (ms): {}, Failures: {}, Failures Message: {}, Count: {}, Index: {}",
                                    executionId,
                                    response.getTookInMillis(),
                                    response.hasFailures(),
                                    response.buildFailureMessage(),
                                    response.getItems().length, index);
                        }
                    }
                    @Override
                    public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                        logger.error("Bulk Execution failed [{}]", executionId, failure);
                    }
                })
                .setBulkActions(500)
                .setBulkSize(new ByteSizeValue(5, ByteSizeUnit.MB))
                .setFlushInterval(TimeValue.timeValueSeconds(5))
                .setConcurrentRequests(1)
                .setBackoffPolicy(
                        BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(100), 3))
                .build();

        GetIndexTemplatesResponse existingTemplates = client.admin().indices()
                .prepareGetTemplates("pickup_location_template").execute().actionGet();
        if (existingTemplates.getIndexTemplates().isEmpty()) {
            PutIndexTemplateResponse response = client.admin().indices()
                    .preparePutTemplate("pickup_location_template")
                    .setTemplate("pickup-location*")
                    .addMapping("_default_",
                            jsonBuilder()
                                    .startObject()
                                    .startObject("_all").field("enabled", true).endObject()
                                    .startObject("properties")
                                        .startObject("name").field("type", "string").field("index", "not_analyzed").endObject()
                                        .startObject("location").field("type", "geo_point").endObject()
                                    .endObject()
                                    .endObject())
                    .execute().actionGet();

            System.out.println(" >> " + response.isAcknowledged());
        } else {
            System.out.println("the template existing");
        }

        Files.lines(Paths.get(RedisTest.class.getResource("/position_20180401.txt").toURI()))
                .map(Position::new).map(position -> {
            Coordinate coordinate = Coordinate.mars(position.lat, position.lng).covert(Coordinate.Type.WORLD);
            position.lat = coordinate.getLat();
            position.lng = coordinate.getLng();
            return position;
        }).forEach(p -> {
            System.out.printf("lat=%s,lng=%s,name=%s\n", p.lat, p.lng, p.name);
            try {
                bulkProcessor.add(new IndexRequest("pickup-location-20180401", "begin")
                        .source(
                                jsonBuilder()
                                .startObject()
                                .field("name", p.name)
                                .field("coord", "world")
                                .field("@timestamp", sdf.format(new Date()))
                                .startObject("location").field("lat", p.lat).field("lon", p.lng).endObject()
                                .endObject()
                        ));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        System.out.println("-------awaitClose-------");
        try {
            bulkProcessor.awaitClose(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {

            System.out.println("------bulkProcessor close--------");
            bulkProcessor.flush();
            bulkProcessor.close();
        }
        System.out.println("------client close--------");
        client.close();
    }

    //1522592943
    // 22.95999207102	114.1557673498
    // 22.959999634033	114.15582981958	振兴路81号
    // 22.968855470555	114.17221662847	东莞善募康科技有限公司
    class Position {
        long uid;
        double lat, lng;
        String name;

        public Position(String line) {
            String[] arry = line.split("\t");
            this.uid = Long.valueOf(arry[0]);
            this.lat = Double.valueOf(arry[3]);
            this.lng = Double.valueOf(arry[4]);
            this.name = arry[5];
        }
    }
    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.SIMPLIFIED_CHINESE);
    @Test
    public void test_140() throws IOException {
        System.out.println(
                jsonBuilder()
                .startObject()
                .field("@timestamp", sdf.format(new Date()))
                .field("name", "元大都")
                .startObject("location").field("lat", 31.22D).field("lng",116.12D).endObject()
                .endObject()
                .string());
    }

}
