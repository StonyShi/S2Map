package com.stony.map;

import com.stony.reactor.jersey.JacksonProvider;
import com.stony.reactor.jersey.JerseyBasedHandler;
import reactor.core.publisher.Mono;
import reactor.ipc.netty.http.server.HttpServer;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * <p>S2-Map
 * <p>com.stony.map
 *
 * @author stony
 * @version 上午10:28
 * @since 2018/2/22
 */
public class NettyServer {

    public static void main(String[] args) throws URISyntaxException {
        int _port = 9037;
        if(args.length > 0) {
            String temp = args[0];
            try {
                _port = Integer.parseInt(temp);
            } catch (NumberFormatException e) {
                _port = 9037;
                System.out.println("format port error : " + temp);
            }
        }
        final Path resource = Paths.get(NettyServer.class.getResource("/WEBAPP").toURI());

        System.out.println("resource = " + resource.toString());

        final int port = _port;
        HttpServer.create(opt -> {
            opt.host("0.0.0.0").port(port);
        }).startAndAwait(JerseyBasedHandler.builder()
            .withClassPath("com.stony.map.controllers")
            .addValueProvider(JacksonProvider.class)
            .withRouter(routes -> {
                routes.get("/get", (req, resp) -> resp.header("Content-Type", "text/plan; charset=UTF-8").sendString(Mono.just("测试")))
                .directory("/s", resource);
            }).build()
        );
    }
}
