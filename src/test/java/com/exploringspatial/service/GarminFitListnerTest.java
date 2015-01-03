package com.exploringspatial.service;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Steve on 1/3/15.
 */
public class GarminFitListnerTest {
    private final String fileName = "/155155867";

    @Test
    public void testExtractFitPolyline() throws IOException {
        final InputStream in = this.getClass().getResourceAsStream(fileName.concat(".fit"));
        final GarminFitService garminFitService = new GarminFitService();
        final Coordinate[] polyline = garminFitService.extractFitPolyline(in);
        assertTrue(polyline.length > 0);
    }

    @Test
    public void testSimplifyLineString() {
        final InputStream in = this.getClass().getResourceAsStream(fileName.concat(".fit"));
        final GarminFitService garminFitService = new GarminFitService();
        final Coordinate[] polyline = garminFitService.extractFitPolyline(in);
        final Geometry geometry = garminFitService.simplifyLineString(polyline);
        assertTrue(geometry.getCoordinates().length < polyline.length/2.85D );
    }

    @Test
    public void testWriteGeoJSON() throws IOException {
        final InputStream in = this.getClass().getResourceAsStream(fileName.concat(".fit"));

        OutputStream out = null;
        try {
            out = new ByteArrayOutputStream();
            final GarminFitService garminFitService = new GarminFitService();
            garminFitService.writeGeoJSON(in, out);
        } finally {
            if (out != null) {
                out.close();
            }
        }
        final InputStream json = this.getClass().getResourceAsStream(fileName.concat(".json"));
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
        assertEquals(buf.toString(), new String(out.toString()));
    }
}
