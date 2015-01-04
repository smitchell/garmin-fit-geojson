package com.exploringspatial.fit;

import com.exploringspatial.domain.FitActivity;
import com.garmin.fit.*;
import java.util.TimeZone;
import com.vividsolutions.jts.geom.Coordinate;
import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * The purpose of this class is pull just enough data out of a Garmin FIT file
 * to generate a GeoJSON payload. It currently only supports a FIT file with a
 * single session (e.g. not multi-sport).
 * @author Steve Mitchell
 */
public class GarminFitListener implements ActivityMesgListener, RecordMesgListener, SessionMesgListener {
    private final FitActivity fitActivity;

    public GarminFitListener(final FitActivity fitActivity) {
        super();
        this.fitActivity = fitActivity;
    }

    @Override
    public void onMesg(final RecordMesg recordMesg) {
        if (recordMesg.getPositionLat() != null && recordMesg.getPositionLong() != null) {
            final double lat = toDegrees(recordMesg.getPositionLat());
            final double lon = toDegrees(recordMesg.getPositionLong());
            fitActivity.addCoordinate(new Coordinate(lon, lat));
        }
    }

    @Override
    public void onMesg(ActivityMesg activityMesg) {
        if (activityMesg.getEvent() == Event.ACTIVITY) {
            final DateTime timestamp = activityMesg.getTimestamp();
            if (timestamp != null) {
                final String formatedDate = DateFormatUtils.format(timestamp.getDate(), "yyyy-MM-dd'T'HH:mm'Z'", TimeZone.getTimeZone("UTC"));
                fitActivity.setStartTime(formatedDate);
            }
        }
    }

    /**
     * This just pulls a handful of values from the session for illustration purposes.
     * @param sessionMesg
     */
    @Override
    public void onMesg(SessionMesg sessionMesg) {
        if (sessionMesg.getTotalDistance() != null) {
            fitActivity.setTotalMeters(sessionMesg.getTotalDistance().doubleValue());
        }
        if (sessionMesg.getStartTime() != null) {
            final String formatedDate = DateFormatUtils.format( sessionMesg.getStartTime().getDate(), "yyyy-MM-dd'T'HH:mm'Z'", TimeZone.getTimeZone("UTC"));
            fitActivity.setStartTime(formatedDate);
        }
        if (sessionMesg.getTotalTimerTime() != null) {
            fitActivity.setTotalSeconds(sessionMesg.getTotalTimerTime().doubleValue());
        }
        if (sessionMesg.getSport() != null) {
            fitActivity.setSport(sessionMesg.getSport().name());
        }
    }

    public FitActivity getFitActivity() {
        return fitActivity;
    }

    private double toDegrees(final Integer s) {
        return s * (180D/Math.pow(2,31)) ;
    }

}
