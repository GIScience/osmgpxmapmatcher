package osmgpxtool.mapmatching.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.GeodeticCalculator;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.densify.Densifier;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Utility class for different geometric or non-geometric operations
 *
 */
public class Util {
	static Logger LOGGER = LoggerFactory.getLogger(Util.class);

	/**
	 * Parses an hstore object, retrieved from PostgresQL database to a map. It
	 * return null is the hstore object is null as well.
	 * 
	 * @param hstore
	 * @returns
	 */
	public static Map<String, String> hstoreToMap(Object hstore) {
		if (hstore == null) {
			return null;
		} else {
			Map<String, String> tags = new HashMap<String, String>();

			String[] kvpairs = hstore.toString().split("\", \"");

			for (String kvpair : kvpairs) {
				try {
					String key = kvpair.split("=>")[0].replaceAll("\"", "");
					String value = kvpair.split("=>")[1].replaceAll("\"", "");
					tags.put(key, value);
				}
				catch (Exception e) {
					LOGGER.info("Skipped corrupt hstore entry:"+kvpair);
				}
			}
			return tags;
		}
	}

	/**
	 * This method buffers a line in the CRS WGS84 with a buffer distance given
	 * in meters. To create the buffer-geometry, the input linestring is
	 * transformed to google mercator projection. Be aware that the given buffer
	 * distance will be applied to the objekt in Google Mercator Projection and
	 * might differ from the orthometric distance.
	 * 
	 * @param geom
	 * @param buffer_distance
	 * @returns null, if SRID is != EPSG:4326 or linestring is null
	 */
	public static Polygon bufferWGS84WithMeters(LineString geom, double buffer_distance) {
		if (geom == null) {
			return null;
		} else if (geom.getSRID() == 4326) {
			try {
				CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326");
				CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:3857");
				MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);

				Polygon targetGeometry = (Polygon) JTS.transform(geom, transform).buffer(buffer_distance);

				MathTransform transform2 = CRS.findMathTransform(targetCRS, sourceCRS);

				return (Polygon) JTS.transform(targetGeometry, transform2);

			} catch (MismatchedDimensionException e) {
				LOGGER.error("input geometry is not in SRID 4326 or could be transformed");
				e.printStackTrace();
				return null;
			} catch (TransformException e) {
				LOGGER.error("input geometry is not in SRID 4326 or could be transformed");
				e.printStackTrace();
				return null;
			} catch (NoSuchAuthorityCodeException e) {
				LOGGER.error("input geometry is not in SRID 4326 or could be transformed");
				e.printStackTrace();
				return null;
			} catch (FactoryException e) {
				LOGGER.error("input geometry is not in SRID 4326 or could be transformed");
				e.printStackTrace();
				return null;
			}
		} else {
			LOGGER.error("input geometry is not in SRID 4326. SRID = " + geom.getSRID());
			return null;
		}
	}

	/**
	 * This method calculates the orthometric length in meters of a LineString
	 * given in SRID EPSG:4326. LineString must be in WGS84 (EPSG:4326). If no
	 * SRID defined or other SRID defined than EPSG:4326 the method will return
	 * 0. Furthermore 0 is returned, if the LineString is null.
	 * 
	 * @param line
	 * @param calc
	 * @return
	 */
	public static double calculateOrthometricLength(LineString line) {
		double distance = 0;
		if (line != null) {
			if (line.getSRID() == 4326) {
				GeodeticCalculator calc = new GeodeticCalculator();
				for (int i = 0; i < line.getCoordinates().length - 1; i++) {
					Coordinate p1 = line.getCoordinates()[i];
					Coordinate p2 = line.getCoordinates()[i + 1];
					calc.setStartingGeographicPoint(p1.x, p2.y);
					calc.setDestinationGeographicPoint(p2.x, p2.y);
					distance += calc.getOrthodromicDistance();
				}
			}
		}
		return distance;
	}

	/**
	 * This method computes the average heading of a LineString. The heading is
	 * returned in a range between 0 and 180°. returns 0, if LineString is null.
	 * 
	 * @param geom
	 * @param calc
	 * @return
	 */
	public static double calculateHeading(LineString geom) {
		// compute heading between the nodes of line segment and calculate the
		// mean
		double sumAzimuth = 0;
		if (geom != null) {
			GeodeticCalculator calc=null;
			
			try {
				calc = new GeodeticCalculator(CRS.decode("EPSG:4326"));
			} catch (NoSuchAuthorityCodeException e) {
				e.printStackTrace();
			} catch (FactoryException e) {
				e.printStackTrace();
			}
			for (int i = 0; i < geom.getCoordinates().length - 1; i++) {
				Coordinate p1 = geom.getCoordinates()[i];
				Coordinate p2 = geom.getCoordinates()[i + 1];
				calc.setStartingGeographicPoint(p1.x, p2.y);
				calc.setDestinationGeographicPoint(p2.x, p2.y);
				sumAzimuth += calc.getAzimuth();

			}
			double meanAzimuth = sumAzimuth / (geom.getCoordinates().length - 1);
			// only return azimuth between 0 and 180 degrees since the direction
			// does not matter
			if (meanAzimuth < 0) {
				return meanAzimuth + 180;
			} else {
				return meanAzimuth;
			}
		} else {
			return 0;
		}
	}

	/**
	 * This method computes perpendicular profile lines, crossing each node of a
	 * LineString. The profile line is perpendicular to the segment between node
	 * n and node n+1. It returns null of either length is 0 or the LineString
	 * is null.
	 * 
	 * 
	 * @param length
	 * @param geom
	 * @return
	 */
	public static List<LineString> computesProfileLines(double length, LineString geom) {
		if (length == 0 || geom == null) {
			return null;
		} else {
			GeodeticCalculator calc=null;
			try {
				calc = new GeodeticCalculator(CRS.decode("EPSG:4326"));
			} catch (NoSuchAuthorityCodeException e1) {
				e1.printStackTrace();
			} catch (FactoryException e1) {
				e1.printStackTrace();
			}
			// get nodes of street segment, densify street segment
			
			Coordinate[] nodes = Densifier.densify(geom, 0.00069444425).getCoordinates();
			List<LineString> profiles = new ArrayList<LineString>();

			/*
			 * for each node, create a perpendicular line crossing the node and
			 * being perpendicular to the line between node N and node N+1
			 */
			
			try {
				for (int i = 0; i < nodes.length; i++) {
					Coordinate node = nodes[i];
					Coordinate next_node;
					// if node is last element
					if (i == (nodes.length - 1)) {
						next_node = nodes[i - 1];
					} else {
						next_node = nodes[i + 1];
					}
					double direction = getDirection(node, next_node);
					Coordinate[] profileNodes = new Coordinate[2];
					// calculate start node of new profile line
					calc.setStartingGeographicPoint(node.x, node.y);
				
					// ifdirection <-180 oder > +180 add / substract 360°
					double newDirection = direction + 90;
					if (newDirection > 180) {
						calc.setDirection(newDirection - 360, length / 2);
					} else {
						calc.setDirection(newDirection, length / 2);
					}
					profileNodes[0] = new Coordinate(calc.getDestinationPosition().getCoordinate()[1], calc
							.getDestinationPosition().getCoordinate()[0]);
					// calculate end node of new profile line
					// if direction <-180 oder > +180 add / substract 360°
					newDirection = direction - 90;
					if (newDirection < -180) {
						calc.setDirection(newDirection + 360, length / 2);
					} else {
						calc.setDirection(newDirection, length / 2);
					}
					profileNodes[1] = new Coordinate(calc.getDestinationPosition().getCoordinate()[1], calc
							.getDestinationPosition().getCoordinate()[0]);
					
					GeometryFactory geomF = new GeometryFactory();
					profiles.add(geomF.createLineString(profileNodes));
				}

			} catch (TransformException e) {
				e.printStackTrace();
			}
			return profiles;
		}
	}

	private static double getDirection(Coordinate node, Coordinate next_node) {
		GeodeticCalculator calc=null;
		try {
			calc = new GeodeticCalculator(CRS.decode("EPSG:4326"));
		} catch (NoSuchAuthorityCodeException e) {
			e.printStackTrace();
		} catch (FactoryException e) {
			e.printStackTrace();
		}
		calc.setStartingGeographicPoint(node.x, node.y);
		calc.setDestinationGeographicPoint(next_node.x, next_node.y);
		return calc.getAzimuth();
	}
}