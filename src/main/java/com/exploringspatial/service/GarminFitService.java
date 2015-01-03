package com.exploringspatial.service;

import com.exploringspatial.fit.GarminFitListener;
import com.garmin.fit.*;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier;
import org.apache.log4j.Logger;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.JTSFactoryFinder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * The purpose of this class is to turn a Garmin FIT file into GeoJSON for
 * testing on www.exploringspatial.com
 *
 * @Author Steve Mitchell
 */
public class GarminFitService {
    private final Logger log = Logger.getLogger(GarminFitService.class);
    /**
     * The purpose of this method is to read a Garmin FIT file from an
     * input stream an extract the track points.
     * @param in - InputStream from a Garmin FIT file.
     * @return Coordinate array from the file.
     */
    public Coordinate[] extractFitPolyline(final InputStream in) {
        final GarminFitListener listener = new GarminFitListener();
        final Decode decode = new Decode();
        final MesgBroadcaster mesgBroadcaster = new MesgBroadcaster(decode);
        mesgBroadcaster.addListener(listener);
        try {
            mesgBroadcaster.run(in);
        } catch (FitRuntimeException e) {
            log.error("Exception decoding file: ");
            log.error(e.getMessage());

            try {
                in.close();
            } catch (java.io.IOException f) {
                throw new RuntimeException(f);
            }

            return null;
        }

        try {
            in.close();
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
        List<Coordinate> coordinates = listener.getCoordinates();
        return coordinates.toArray(new Coordinate[coordinates.size()]);
    }

    /**
     * The purpose of this method is to use the
     * Douglas-Peuker line simplification algorithm to reduce the
     * number of points in the polyline while maintaining route fidelity.
     * @param polyline - The full set of points from the device.
     * @return Simplified Geometry.
     */
    public Geometry simplifyLineString(final Coordinate[] polyline) {
        final GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        final LineString lineString = geometryFactory.createLineString(polyline);
        return DouglasPeuckerSimplifier.simplify(lineString, 0.00001);
    }

    public void writeGeoJSON(final InputStream in, final OutputStream out) {
        final Coordinate[] polyline = extractFitPolyline(in);
        final Geometry geometry = simplifyLineString(polyline);
        final GeometryJSON geometryJSON = new GeometryJSON();
        try {
            geometryJSON.write(geometry, out);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                log.error(e);
            }
        }
    }
}
