package com.exploringspatial.utils;

import com.exploringspatial.domain.FitActivity;
import com.exploringspatial.service.GarminFitService;
import com.exploringspatial.tcx.TcxParser;
import org.opengis.referencing.FactoryException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mitchellst on 1/7/15.
 */
public class SpecialImport {
    public static final char MAGIC_FIT_IDENTIFIER[] = {'.', 'F', 'I', 'T'};

    private List<FitActivity> fitActivities;
    private TcxParser tcxParser;
    private GarminFitService garminFitService;
    private final File importDir = new File("/Users/mitchellst/Desktop/activities/files");
    private final File outputDir = new File("/Users/mitchellst/Desktop/activities/activity");

    public static void main(String[] args) {
        SpecialImport specialImport = new SpecialImport();
        specialImport.run();
    }

    public void run() {
        fitActivities = new ArrayList<FitActivity>();
        tcxParser = new TcxParser();
        garminFitService = new GarminFitService();
        try {
            loadActivityIds();
            processFiles();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void processFiles() throws IOException, FactoryException {
        final String RUNNING = "RUNNING";
        File activityFile;
        FitActivity parsedActivity;
        List<FitActivity> parsedActivities;

        for (final FitActivity fitActivity : fitActivities) {
            parsedActivity = null;
            InputStream in = null;
            try {

                activityFile = new File(importDir, ("" + fitActivity.getActivityId()).concat(".fit"));
                if (activityFile.exists()) {
                    in = new FileInputStream(activityFile);
                    parsedActivity = garminFitService.decodeFitFile(in);
                    if (parsedActivity != null) {
                        fitActivity.setSport(parsedActivity.getSport());
                        fitActivity.setPolyline(parsedActivity.getPolyline());
                        storeActivity(fitActivity);
                    }
                } else {

                    activityFile = new File(importDir, ("" + fitActivity.getActivityId()).concat(".tcx"));
                    if (activityFile.exists()) {
                        in = new FileInputStream(activityFile);
                        parsedActivities = tcxParser.parseTcxFile(in);
                        for (FitActivity next: parsedActivities) {
                            if (RUNNING.equals(next.getSport())) {
                                parsedActivity = next;
                            }
                        }
                    }
                }
                if (parsedActivity != null &&
                        fitActivity.getActivityId() != null) {
                    fitActivity.setSport(parsedActivity.getSport());
                    fitActivity.setPolyline(parsedActivity.getPolyline());
                    storeActivity(fitActivity);
                }


            } finally {
                if (in != null) {
                    in.close();
                }
            }
        }
    }

    private void storeActivity(final FitActivity fitActivity) throws IOException, FactoryException {
        OutputStream out = null;
        try {
            final File file = new File(outputDir, fitActivity.getActivityId().toString());
            out = new BufferedOutputStream(new FileOutputStream(file));
            garminFitService.writeFeatureGeoJSON(fitActivity, out);
        } catch( RuntimeException e) {
            e.printStackTrace();
        }
        finally {
            if (out != null) {
                out.close();
            }
        }
    }

    public void loadActivityIds() throws IOException {

        final File catalog = new File("/Users/mitchellst/Desktop/activities/activities.csv");
        BufferedReader buff = null;
        String s;
        String[] columns;
        try {
            buff = new BufferedReader(new FileReader(catalog));
            buff.readLine(); // Skip headers
            s = buff.readLine();
            FitActivity fitActivity;
            int count = 0;
            while (s != null) {
                fitActivity = new FitActivity();
                System.out.println("" + count++ + s);
                columns = s.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                if (columns.length > 0 && columns[0] != null && !columns[0].isEmpty()) {
                    fitActivity.setActivityId(Long.valueOf(columns[0].trim()));
                }
                if (columns.length > 1 && columns[1] != null && !columns[1].isEmpty()) {
                    fitActivity.setStartTime(columns[1].trim());
                }
                if (columns.length > 2 && columns[2] != null && !columns[2].isEmpty()) {
                    // Strip quotes
                    fitActivity.setName(columns[2].substring(1, columns[2].length()-1));
                }
                if (columns.length > 3 && columns[3] != null && !columns[3].isEmpty()) {
                    fitActivity.setTotalMeters(Double.parseDouble(columns[3].trim()));
                }
                if (columns.length > 4 && columns[4] != null && !columns[4].isEmpty()) {
                    fitActivity.setTotalSeconds(Double.parseDouble(columns[4].trim()));
                }
                fitActivities.add(fitActivity);
                s = buff.readLine();
            }
        } finally {
            if (buff != null) {
                buff.close();
            }

        }
    }

    public boolean isFit(byte[] data) {
		boolean isFit = data.length >= 12;
		if (isFit) {
			isFit = data[8]  == MAGIC_FIT_IDENTIFIER[0]
			     && data[9]  == MAGIC_FIT_IDENTIFIER[1]
			     && data[10] == MAGIC_FIT_IDENTIFIER[2]
			     && data[11] == MAGIC_FIT_IDENTIFIER[3];
		}
		return isFit;
	}
}
