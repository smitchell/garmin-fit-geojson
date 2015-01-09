package com.exploringspatial.tcx;

import com.exploringspatial.domain.FitActivity;
import com.garmin.trainingcenter.*;
import com.vividsolutions.jts.geom.Coordinate;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mitchellst on 1/7/15.
 */
public class TcxParser {
    private final Logger log = Logger.getLogger(TcxParser.class);
    private Unmarshaller unmarshaller;

    public TcxParser() {
        super();
        init();
    }

    private void init() {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance("com.garmin.trainingcenter");
            unmarshaller = jaxbContext.createUnmarshaller();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    public List<FitActivity> parseTcxFile(final InputStream inputStream) {
        if (inputStream == null) {
            throw new IllegalArgumentException("inputStream must not be null");
        }
        final List<FitActivity> activityPolylines = new ArrayList<FitActivity>();
        if (inputStream != null) {

            ActivityListT activityList;
            TrainingCenterDatabaseT trainingCenterDatabaseT;
            try {
                final JAXBElement<TrainingCenterDatabaseT> o = (JAXBElement<TrainingCenterDatabaseT>) unmarshaller.unmarshal(inputStream);
                trainingCenterDatabaseT = o.getValue();
                if (trainingCenterDatabaseT != null) {
                    activityList = trainingCenterDatabaseT.getActivities();
                    if (activityList.getActivity() != null && !activityList.getActivity().isEmpty()) {
                        for (final ActivityT activity : activityList.getActivity()) {
                            activityPolylines.addAll(parseActivity(activity));
                        }
                    } else if (activityList.getMultiSportSession() != null && !activityList.getMultiSportSession().isEmpty()) {
                        for (int i = 0 ; i < activityList.getMultiSportSession().size(); i++) {
                            MultiSportSessionT multiSportSessionT = activityList.getMultiSportSession().get(i);
                            if (multiSportSessionT != null) {
                                FirstSportT firstSportT = multiSportSessionT.getFirstSport();
                                activityPolylines.addAll(parseActivity(firstSportT.getActivity()));
                                List<NextSportT> nextSports = multiSportSessionT.getNextSport();
                                for (NextSportT nextSport : nextSports) {
                                    activityPolylines.addAll(parseActivity(nextSport.getActivity()));
                                }
                            }
                        }
                    }
                }

            } catch (JAXBException e) {
                log.error(e,e);
            }

        }
        return activityPolylines;
    }

    private List<FitActivity> parseActivity(final ActivityT activity) {
        float distance = 0;
        double timeInSeconds = 0;
        Long previousTime = null;
        List<FitActivity> activityPolylines = new ArrayList<FitActivity>();
        FitActivity activityPolyline;
        Coordinate coordinate;
        activityPolyline = new FitActivity();
        activityPolylines.add(activityPolyline);
        activityPolyline.setSport(activity.getSport().name());
        if (activity.getLap() != null) {
            for (final ActivityLapT lap :  activity.getLap()) {
                distance += lap.getDistanceMeters();
                timeInSeconds += lap.getTotalTimeSeconds();
                if (lap.getTrack() != null) {
                    for (final TrackT track : lap.getTrack()) {
                        if (track.getTrackpoint() != null) {
                            for (final TrackpointT point : track.getTrackpoint()) {
                                if (point.getPosition() != null && (point.getPosition().getLatitudeDegrees() != 0D && point.getPosition().getLongitudeDegrees() != 0D)) {
                                    coordinate = new Coordinate();
                                    coordinate.y = point.getPosition().getLatitudeDegrees();
                                    coordinate.x = point.getPosition().getLongitudeDegrees();
                                    coordinate.z = point.getTime().toGregorianCalendar().getTime().getTime();
                                    activityPolyline.addCoordinate(coordinate);
                                }
                            }
                        }
                    }
                }
            }
        }
        activityPolyline.setTotalSeconds(timeInSeconds);
        activityPolyline.setTotalMeters(distance / 100D);
        return activityPolylines;
    }
}
