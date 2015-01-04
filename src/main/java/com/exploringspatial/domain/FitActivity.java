package com.exploringspatial.domain;

import com.vividsolutions.jts.geom.Coordinate;
import java.util.ArrayList;
import java.util.List;

/**
 * The Activity class represents an Activity from Garmin Connect.
 *
 * @author Steve Mitchell
 */
public class FitActivity {
    private Long activityId;
    private String name;
    private String description;
    /**
     * Timestamp formatted to ISO 8601
     * http://www.w3.org/TR/NOTE-datetime
     * See Apache commons-lang3 have useful constants, for example: DateFormatUtils.ISO_DATETIME_FORMAT:
     * DateFormatUtils.format(new Date(), "yyyy-MM-dd'T'HH:mm'Z'", TimeZone.getTimeZone("UTC"))
     * // e.g. 2012-04-23T18:25:43.511Z
     */
    private String startTime;
    private String sport;
    private Double totalMeters;
    private Double totalSeconds;
    private List<Coordinate> polyline = new ArrayList<Coordinate>();

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(final Long activityId) {
        this.activityId = activityId;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(final String startTime) {
        this.startTime = startTime;
    }

    public String getSport() {
        return sport;
    }

    public void setSport(String sport) {
        this.sport = sport;
    }

    public Double getTotalMeters() {
        return totalMeters;
    }

    public void setTotalMeters(Double totalMeters) {
        this.totalMeters = totalMeters;
    }

    public Double getTotalSeconds() {
        return totalSeconds;
    }

    public void setTotalSeconds(Double totalSeconds) {
        this.totalSeconds = totalSeconds;
    }

    public List<Coordinate> getPolyline() {
        return polyline;
    }

    public void setPolyline(final List<Coordinate> polyline) {
        this.polyline = polyline;
    }

    public void addCoordinate(final Coordinate coordinate) {
        if (this.polyline == null) {
            this.polyline = new ArrayList<Coordinate>(1);
        }
        this.polyline.add(coordinate);
    }
}
