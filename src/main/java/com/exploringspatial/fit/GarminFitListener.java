package com.exploringspatial.fit;

import com.garmin.fit.*;
import com.vividsolutions.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.List;

/**
 * The purpose of this class is pull just enough data out of a Garmin FIT file
 * to generate a GeoJSON payload.
 * @author Steve Mitchell
 */
public class GarminFitListener implements RecordMesgListener {
    private final List<Coordinate> coordinates = new ArrayList<Coordinate>();

    @Override
    public void onMesg(final RecordMesg recordMesg) {
        if (recordMesg.getPositionLat() != null && recordMesg.getPositionLong() != null) {
            final double lat = toDegrees(recordMesg.getPositionLat());
            final double lon = toDegrees(recordMesg.getPositionLong());
            coordinates.add(new Coordinate(lon, lat));
        }
    }
    public List<Coordinate> getCoordinates() {
        return coordinates;
    }

    private double toDegrees(final Integer s) {
        return s * (180D/Math.pow(2,31)) ;
    }

}
