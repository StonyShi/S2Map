package com.stony.map;

import com.stony.reactor.jersey.MimeTypeUtil;

/**
 * <p>S2-Map
 * <p>com.stony.map
 *
 * @author stony
 * @version 上午11:17
 * @since 2018/2/22
 */
public class BaseTest {

    double earthCircumferenceMeters = 1000 * 40075.017;
    double EARTH_RADIUS = 6371;

    double earthMetersToRadians(double meters) {
        return (2 * Math.PI) * (meters / earthCircumferenceMeters);
    }


    double radius2height(double radius) {
        return 1 - Math.sqrt(1 - Math.pow((radius / EARTH_RADIUS), 2));
    }
    double height2radius (double height) {
        return Math.sqrt(1 - Math.pow(1 - height, 2)) * EARTH_RADIUS;
    }

}
