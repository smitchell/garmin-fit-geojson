package com.exploringspatial.service;

import com.exploringspatial.domain.FitActivity;
import com.exploringspatial.fit.GarminFitListener;
import com.garmin.fit.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier;
import org.apache.log4j.Logger;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.ByteArrayOutputStream;
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
     * input stream an extract the activity data.
     * @param in - InputStream from a Garmin FIT file.
     * @return FitActivity - date from FIT file.
     */
    public FitActivity decodeFitFile(final InputStream in) {
        final GarminFitListener listener = new GarminFitListener(new FitActivity());
        final Decode decode = new Decode();
        final MesgBroadcaster mesgBroadcaster = new MesgBroadcaster(decode);
        mesgBroadcaster.addListener((RecordMesgListener)listener);
        mesgBroadcaster.addListener((SessionMesgListener)listener);
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
        return listener.getFitActivity();
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

    public void writeLinestringGeoJSON(final Coordinate[] polyline, final OutputStream out) {
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

    public void writeFeatureGeoJSON(final FitActivity fitActivity, final OutputStream out) throws FactoryException, IOException {
        try {

            final SimpleFeatureTypeBuilder tb=new SimpleFeatureTypeBuilder();
            tb.add("geom",LineString.class, DefaultGeographicCRS.WGS84);
            tb.add("name",String.class);
            tb.add("activityId",Double.class);
            tb.setName("activity");
            tb.add("activityName", String.class);
            tb.add("sport",String.class);
            tb.add("startTime",String.class);
            tb.add("totalMeters",Double.class);
            tb.add("totalSeconds",Double.class);
            final SimpleFeatureType schema=tb.buildFeatureType();
            final SimpleFeatureBuilder fb=new SimpleFeatureBuilder(schema);
            fb.set("activityId",fitActivity.getActivityId());
            fb.set("sport",fitActivity.getSport());
            fb.set("startTime",fitActivity.getStartTime());
            fb.set("totalMeters",fitActivity.getTotalMeters());
            fb.set("totalSeconds",fitActivity.getTotalSeconds());
            final Coordinate[] polyline = fitActivity.getPolyline().toArray(new Coordinate[fitActivity.getPolyline().size()]);
            final Geometry geometry = simplifyLineString(polyline);
            fb.add(geometry);
            final SimpleFeature feature=fb.buildFeature("0");
            final FeatureJSON fj=new FeatureJSON();
            fj.writeFeature(feature,out);
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                log.error(e);
            }
        }
    }

}
