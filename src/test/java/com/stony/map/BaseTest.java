package com.stony.map;

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
    double earthMetersToRadians(double meters) {
        return (2 * Math.PI) * (meters / earthCircumferenceMeters);
    }
}
