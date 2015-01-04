package com.exploringspatial.service;

import com.exploringspatial.domain.FitActivity;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import org.junit.Test;
import org.opengis.referencing.FactoryException;

import java.io.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Steve on 1/3/15.
 */
public class GarminFitListnerTest {
    private final String fileName = "/155155867.fit";

    @Test
    public void testExtractFitPolyline() throws IOException {
        final InputStream in = this.getClass().getResourceAsStream(fileName);
        final GarminFitService garminFitService = new GarminFitService();
        final FitActivity fitActivity = garminFitService.decodeFitFile(in);
        assertNotNull(fitActivity);
        assertTrue(!fitActivity.getPolyline().isEmpty());
    }

    @Test
    public void testSimplifyLineString() {
        final InputStream in = this.getClass().getResourceAsStream(fileName);
        final GarminFitService garminFitService = new GarminFitService();
        final FitActivity fitActivity = garminFitService.decodeFitFile(in);
        final Geometry geometry = garminFitService.simplifyLineString(fitActivity.getPolyline().toArray(new Coordinate[fitActivity.getPolyline().size()]));
        assertTrue(geometry.getCoordinates().length < fitActivity.getPolyline().size()/2.85D );
    }

    @Test
    public void testWriteLineStringJSON() throws IOException {
        final InputStream in = this.getClass().getResourceAsStream(fileName);

        OutputStream out = null;
        try {
            //out = new BufferedOutputStream(new FileOutputStream("/Users/Steve/Development/garmin-fit-geojson/src/test/resources/linestring.json"));
            out = new ByteArrayOutputStream();
            final GarminFitService garminFitService = new GarminFitService();
            final FitActivity fitActivity = garminFitService.decodeFitFile(in);
            garminFitService.writeLinestringGeoJSON(fitActivity.getPolyline().toArray(new Coordinate[fitActivity.getPolyline().size()]), out);
        } finally {
            if (out != null) {
                out.close();
            }
        }
        final InputStream json = this.getClass().getResourceAsStream("/linestring.json");
        String str;
        final StringBuilder buf = new StringBuilder();
        try {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(json));
            while ((str = reader.readLine()) != null) {
                buf.append(str);
            }
        } finally {
            try { json.close(); } catch (Throwable ignore) {}
        }
        assertEquals(buf.toString(), out.toString());
    }

    @Test
    public void testWriteFeatureGeoJSON() throws IOException, FactoryException {
        final InputStream in = this.getClass().getResourceAsStream(fileName);

        OutputStream out = null;
        try {
            //out = new BufferedOutputStream(new FileOutputStream("/Users/Steve/Development/garmin-fit-geojson/src/test/resources/feature.json"));
            out = new ByteArrayOutputStream();
            final GarminFitService garminFitService = new GarminFitService();
            final FitActivity fitActivity = garminFitService.decodeFitFile(in);
            // Add some stuff not contained in the FIT file.
            fitActivity.setActivityId(155155867L);
            fitActivity.setName("2012 Little Rock Marathon");
            garminFitService.writeFeatureGeoJSON(fitActivity, out);
        } finally {
            if (out != null) {
                out.close();
            }
        }
        final InputStream json = this.getClass().getResourceAsStream("/feature.json");
        String str;
        final StringBuilder buf = new StringBuilder();
        try {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(json));
            while ((str = reader.readLine()) != null) {
                buf.append(str);
            }
        } finally {
            try { json.close(); } catch (Throwable ignore) {}
        }
        assertEquals(buf.toString(), out.toString());
    }
}
