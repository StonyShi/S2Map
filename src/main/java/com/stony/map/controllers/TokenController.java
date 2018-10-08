package com.stony.map.controllers;

import com.stony.token.DynamicToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import java.util.Map;


/**
 * <p>S2-Map
 * <p>com.stony.map.controllers
 *
 * @author stony
 * @version 下午3:11
 * @since 2018/10/8
 */
public class TokenController {
    private static final Logger logger = LoggerFactory.getLogger(TokenController.class);

    @GET
    @Path("/token")
    @Produces("text/plain")
    public String getPoints(@QueryParam("key") String key)  {
        logger.info("key={}", key);

        DynamicToken dynamicToken = new DynamicToken(key);
        try {
            return dynamicToken.getDynamicCode();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}