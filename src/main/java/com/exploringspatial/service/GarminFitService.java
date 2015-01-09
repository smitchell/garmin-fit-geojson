package com.exploringspatial.service;

import com.exploringspatial.domain.FitActivity;
import com.exploringspatial.fit.GarminFitListener;
import com.garmin.fit.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier;
import org.apache.log4j.Logger;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.FactoryException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The purpose of this class is to turn a Garmin FIT file into GeoJSON for
 * testing on www.exploringspatial.com
 *
 * @Author Steve Mitchell
 */
public class GarminFitService {
    private final Logger log = Logger.getLogger(GarminFitService.class);
    private final GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();

    /**
     * The purpose of this method is to read a Garmin FIT file from an
     * input stream an extract the activity data.
     *
     * @param in - InputStream from a Garmin FIT file.
     * @return FitActivity - date from FIT file.
     */
    public FitActivity decodeFitFile(final InputStream in) {
        final GarminFitListener listener = new GarminFitListener(new FitActivity());
        final Decode decode = new Decode();
        final MesgBroadcaster mesgBroadcaster = new MesgBroadcaster(decode);
        mesgBroadcaster.addListener((RecordMesgListener) listener);
        mesgBroadcaster.addListener((SessionMesgListener) listener);
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
     *
     * @param polyline - The full set of points from the device.
     * @return Simplified Geometry.
     */
    public Geometry simplifyLineString(final Coordinate[] polyline) {

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

    /**
     * The purpose of this method is to generate the schema definition for the GeoJSON.
     * @return SimpleFeatureType defining the the feature properties.
     */
    public SimpleFeatureType getFeatureSchema() {
        final SimpleFeatureTypeBuilder simpleFeatureType = new SimpleFeatureTypeBuilder();
        simpleFeatureType.add("geom", LineString.class, DefaultGeographicCRS.WGS84);
        simpleFeatureType.add("name", String.class);
        simpleFeatureType.add("activityId", Long.class);
        simpleFeatureType.setName("activity");
        simpleFeatureType.add("name", String.class);
        simpleFeatureType.add("sport", String.class);
        simpleFeatureType.add("startTime", String.class);
        simpleFeatureType.add("totalMeters", Double.class);
        simpleFeatureType.add("totalSeconds", Double.class);
        simpleFeatureType.add("minLat", Double.class);
        simpleFeatureType.add("minLon", Double.class);
        simpleFeatureType.add("maxLat", Double.class);
        simpleFeatureType.add("maxLon", Double.class);
        return simpleFeatureType.buildFeatureType();
    }

    /**
     * The purpose of this method is to build a SimpleFeature by assigning
     * properties from the FitActivity to the properties defined in the SimpleFeatureType.
     * @param fitActivity
     * @return
     */
    public SimpleFeature buildSimpleFeature(final FitActivity fitActivity) {
        final SimpleFeatureType featureSchema = getFeatureSchema();
        final SimpleFeatureBuilder builder = new SimpleFeatureBuilder(featureSchema);
        builder.set("activityId", fitActivity.getActivityId());
        builder.set("name", fitActivity.getName());
        builder.set("sport", fitActivity.getSport());
        builder.set("startTime", fitActivity.getStartTime());
        builder.set("totalMeters", fitActivity.getTotalMeters());
        builder.set("totalSeconds", fitActivity.getTotalSeconds());
        final Coordinate[] polyline = fitActivity.getPolyline().toArray(new Coordinate[fitActivity.getPolyline().size()]);
        final Geometry geometry = simplifyLineString(polyline);
        builder.add(geometry);
        final Coordinate[] boundingBox = generateBoundingBox(geometry);
        builder.set("minLat", boundingBox[0].y);
        builder.set("minLon", boundingBox[0].x);
        builder.set("maxLat", boundingBox[1].y);
        builder.set("maxLon", boundingBox[1].x);
        return builder.buildFeature(fitActivity.getActivityId().toString());
    }

    public void writeFeatureGeoJSON(final FitActivity fitActivity, final OutputStream out) throws FactoryException, IOException {
        final SimpleFeature feature = buildSimpleFeature(fitActivity);
        final FeatureJSON fj = new FeatureJSON();
        try {
            fj.writeFeature(feature, out);
        } catch(RuntimeException e) {
            log.error(e);
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                log.error(e);
            }
        }
    }

    /**
     * The purpose of this method is to find the minimum/maximum lat/lon bounds.
     *
     * @param geometry - The geometry for which the southwest and northeast corners will be found.
     * @return the minimum lat/lon and maximum lat/lon.
     */
    private Coordinate[] generateBoundingBox(final Geometry geometry) {
        if (geometry == null || geometry.getCoordinates() == null || geometry.getCoordinates().length == 0) {
            return new Coordinate[]{new Coordinate(0,0),new Coordinate(0,0)};
        }
        final Coordinate firstPoint = geometry.getCoordinates()[0];
        double minLat = firstPoint.y;
        double minLon = firstPoint.x;
        double maxLat = firstPoint.y;
        double maxLon = firstPoint.x;
        for (final Coordinate coordinate : geometry.getCoordinates()) {
            if (coordinate.x < minLon) {
                minLon = coordinate.x;
            }
            if (coordinate.y < minLat) {
                minLat = coordinate.y;
            }
            if (coordinate.x > maxLon) {
                maxLon = coordinate.x;
            }
            if (coordinate.y > maxLat) {
                maxLat = coordinate.y;
            }
        }
        return new Coordinate[]{
                new Coordinate(minLon, minLat),
                new Coordinate(maxLon, maxLat)
        };
    }

}
