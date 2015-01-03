Exploring Spatial FIT to GeoJSON Utility
========================================

This project contains a utility to convert Garmin activity FIT filt into a GeoJSON LineString.
It is very primitive. You may include the GarminFitService in your own project to receive FIT
files and output JSON files.

See the class GarminFitListenerTest. Specifically, the method testWriteGeoJSON() shows how to 
take in a FIT file and output a JSON file. To create the test file 155155867.json I substituted
a FileOutputStream for the ByteArrayOutputString used in the test.

* Links *
Blog: http://ExploringSpatial.WordPress.com
Site: http://www.ExploringSpatial.com
Code: https://github.com/smitchell/garmin-fit-geojson
